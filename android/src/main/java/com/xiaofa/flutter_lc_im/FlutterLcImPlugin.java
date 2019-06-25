package com.xiaofa.flutter_lc_im;

import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.PushService;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import cn.leancloud.chatkit.LCChatKit;
import cn.leancloud.chatkit.LCChatKitUser;
import cn.leancloud.chatkit.activity.LCIMContactFragment;
import cn.leancloud.chatkit.activity.LCIMConversationActivity;
import cn.leancloud.chatkit.activity.LCIMConversationListFragment;
import cn.leancloud.chatkit.cache.LCIMConversationItemCache;
import cn.leancloud.chatkit.utils.LCIMConstants;

import cn.leancloud.chatkit.LCChatKit;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** FlutterLcImPlugin */
public class FlutterLcImPlugin implements MethodCallHandler {

  static boolean isRegister = false;
  private Context context;
  private Activity activity;

  private FlutterLcImPlugin(Registrar registrar){
    context = registrar.context();
    activity = registrar.activity();
  }
  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_lc_im");
    FlutterLcImPlugin instance = new FlutterLcImPlugin(registrar);
    channel.setMethodCallHandler(instance);
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    }
    else if(call.method.equals("register")){
      if (!isRegister){
        String appId = call.argument("app_id");
        String appKey = call.argument("app_key");
        this.initChatView(appId,appKey,result);
        isRegister = true;
      } else
      {
        result.success("success");
      }
    }
    else if (call.method.equals("login")){
      String userId = call.argument("user_id");
      this.login(userId,result);
    }
    else if (call.method.equals("pushToConversationView")){
      Map user = call.argument("user");
      Map peer = call.argument("peer");
      this.pushToConversationView(user,peer);
      result.success("");
    } else if (call.method.equals("getRecentConversationUsers")){
      getRecentConversationUsers(result);
    }
    else {
      result.notImplemented();
    }
  }

  private void initChatView(String appId, String appKey, final Result result){
    //配置临时域名
    // 配置 SDK 储存
    AVOSCloud.setServer(AVOSCloud.SERVER_TYPE.API, "https://avoscloud.com");
    // 配置 SDK 云引擎
    AVOSCloud.setServer(AVOSCloud.SERVER_TYPE.ENGINE, "https://avoscloud.com");
    // 配置 SDK 推送
    AVOSCloud.setServer(AVOSCloud.SERVER_TYPE.PUSH, "https://avoscloud.com");
    // 配置 SDK 即时通讯
    AVOSCloud.setServer(AVOSCloud.SERVER_TYPE.RTM, "https://router-g0-push.avoscloud.com");
    LCChatKit.getInstance().init(context, appId, appKey);
    AVIMClient.setAutoOpen(true);
    PushService.setAutoWakeUp(true);
    PushService.setDefaultChannelId(context, "default");
    AVInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
      public void done(AVException e) {
        if (e == null) {
          // 保存成功
          String installationId = AVInstallation.getCurrentInstallation().getInstallationId();
          System.out.println("---  " + installationId);
          result.success("success");
        } else {
          // 保存失败，输出错误信息
          System.out.println("failed to save installation.");
          result.error("ailed to save installation","","");
        }
      }
    });
  }

  private void login(String userId, final Result result){
    LCChatKit.getInstance().open(userId, new AVIMClientCallback() {
      @Override
      public void done(AVIMClient avimClient, AVIMException e) {
        if (null == e) {
          System.out.println("lc 用户登录成功");
          result.success("lc 用户登录成功");
        } else {
          result.error("lc 用户登录失败","","");
        }
      }
    });
  }

  private void pushToConversationView(Map<String,String> user, final Map<String,String> peer){

    List users = new ArrayList();
    users.add(user);
    users.add(peer);
    CustomUserProvider.getInstance().setAllUsers(users);
    LCChatKit.getInstance().setProfileProvider(CustomUserProvider.getInstance());
    AVOSCloud.setDebugLogEnabled(true);

    Intent intent = new Intent(activity, LCIMConversationActivity.class);
    intent.putExtra(LCIMConstants.PEER_ID, peer.get("user_id"));
    activity.startActivity(intent);
  }

  public void getRecentConversationUsers(final Result result){
    List<String> convIdList = LCIMConversationItemCache.getInstance().getSortedConversationList();
    String clientId =  LCChatKit.getInstance().getClient().getClientId();

    List conversationList = new ArrayList<>();
          for (String convId : convIdList) {

            Map<String,Object> con = new HashMap<>();
            AVIMConversation conversation =  LCChatKit.getInstance().getClient().getConversation(convId);

            // 获取peerId
            String peerId = "";
            if (conversation.getMembers().size() == 2){
              if (conversation.getMembers().get(0) == clientId){
                peerId = conversation.getMembers().get(1);
              }else {
                peerId = conversation.getMembers().get(0);
              }
            }

            //获取最新消息的时间
            SimpleDateFormat sdf= new SimpleDateFormat("MM-dd HH:mm");
            String lastMessageAt = sdf.format(conversation.getLastMessageAt());

            // json to map
            Map<String,Object> content = JSON.parseObject(conversation.getLastMessage().getContent());
            Map<String,Object> attrs = (Map<String,Object>)content.get("_lcattrs");
            String message = "";
            Integer messageType = Integer.valueOf(content.get("_lctype").toString());
            if (messageType == -1){
              message = content.get("_lctext").toString();
            }else if(messageType == -2){
              message = "[图片]";
            }else if(messageType == -3){
              message = "[语音]";
            }else if(messageType == -4){
              message = "[视频]";
            }else if(messageType == -5){
              message = "[位置]";
            }else if(messageType == -6){
              message = "[文件]";
            }else {
              message ="[暂不支持格式]";
            }
            con.put("clientId",clientId);
            con.put("peerId",peerId);
            con.put("unreadMessagesCount",conversation.getUnreadMessagesCount());
            con.put("lastMessageAt",lastMessageAt);
            con.put("peerName",attrs.get("username"));
            con.put("lastMessageContent",message);

            System.out.println("conversation :"+ con);

        conversationList.add(con);
      }
          result.success(conversationList);
  }
}
