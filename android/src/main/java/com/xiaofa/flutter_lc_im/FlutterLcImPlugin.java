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
import com.avos.avoscloud.im.v2.AVIMConversationEventHandler;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessageHandler;
import com.avos.avoscloud.im.v2.AVIMMessageManager;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.AVIMTypedMessageHandler;
import com.avos.avoscloud.im.v2.Conversation;
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
import cn.leancloud.chatkit.handler.LCIMMessageHandler;
import cn.leancloud.chatkit.utils.LCIMConstants;


import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;


/** FlutterLcImPlugin */
public class FlutterLcImPlugin implements MethodCallHandler {

  public interface FlutterImCallback {
    void refreshMessage(Object msg);
  }

  static FlutterImCallback flutterImCallback;

  static boolean isRegister = false;
  private Context context;
  private Activity activity;
  static BinaryMessenger messenger;
  static EventChannel.EventSink eventCallback;
  public static EventChannel.EventSink notificationCallback;

  private FlutterLcImPlugin(Registrar registrar){
    context = registrar.context();
    activity = registrar.activity();
  }

  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_lc_im");
    messenger = registrar.messenger();
    FlutterLcImPlugin instance = new FlutterLcImPlugin(registrar);
    channel.setMethodCallHandler(instance);
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {

    switch (call.method){
      case "getPlatformVersion":
        result.success("Android " + android.os.Build.VERSION.RELEASE);
        break;
      case "register":
        if (!isRegister){
          String appId = call.argument("app_id");
          String appKey = call.argument("app_key");
          String api = call.argument("api");

          this.initSetting(appId,appKey,api,result);
          isRegister = true;
        } else
        {
          result.success("success");
        }
        break;
      case "login":
        String userId = call.argument("user_id");
        this.login(userId,result);
        break;
      case "pushToConversationView":
        Map user = call.argument("user");
        Map peer = call.argument("peer");
        this.pushToConversationView(user,peer);
        result.success("");
        break;
      case "getRecentConversationUsers":
        getRecentConversationUsers(result);
        break;
        default:
          result.notImplemented();
          break;
    }
  }

 /*
 *  开启聊天
 * */
  private void initSetting(String appId, String appKey, String api,final Result result){

    AVOSCloud.setServer(AVOSCloud.SERVER_TYPE.API, api);
    // 配置 SDK 云引擎（用于访问云函数，使用 API 自定义域名，而非云引擎自定义域名）
    AVOSCloud.setServer(AVOSCloud.SERVER_TYPE.ENGINE, api);
    // 配置 SDK 推送
    AVOSCloud.setServer(AVOSCloud.SERVER_TYPE.PUSH, api);
    // 配置 SDK 即时通讯
    AVOSCloud.setServer(AVOSCloud.SERVER_TYPE.RTM, api);

    LCChatKit.getInstance().init(context, appId, appKey);
    AVIMClient.setAutoOpen(true);
    AVIMClient.setUnreadNotificationEnabled(true);

    initFlutterChannel();

  }
/*
*  初始化推送和聊天信息
* */
  private void login(String userId, final Result result){

   initPushService(userId,result);

   initChatService(userId,result);

  }

  private void initPushService(String userId, final Result result){

    PushService.setAutoWakeUp(true);
    PushService.setDefaultChannelId(context, "default");//这个channel和订阅的channel不一样，只能为default
    PushService.subscribe(context,userId,activity.getClass()); //订阅频道,这一步必须，否则无法收到推送
    PushService.setDefaultPushCallback(context,activity.getClass());
    AVOSCloud.setDebugLogEnabled(true);

    //每次登录后设置推送
    AVIMMessageManager.registerMessageHandler(AVIMTypedMessage.class,new CustomMessageHandler(context));
    AVInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
      public void done(AVException e) {
        if (e == null) {
          // 保存成功
          String installationId = AVInstallation.getCurrentInstallation().getInstallationId();
          System.out.println("---  " + installationId);
        } else {
          // 保存失败，输出错误信息
          System.out.println("failed to save installation.");
        }
      }
    });
  }

  private void initChatService(String userId, final Result result){
    //初始化聊天登录
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

    LCChatKit.getInstance().setProfileProvider(CustomUserProvider.getInstance());
  }


  /*
  * 初始化flutter和android之间的信道
  * */
  private void initFlutterChannel(){

    //native to flutter by channel flutter_lc_im_native
    setConversationEventToFlutter();
    //
    setNotificationEventToFlutter();

    flutterImCallback = new FlutterLcImPlugin.FlutterImCallback(){

      @Override
      public void refreshMessage(Object msg) {
        System.out.println(msg);
        refreshRecentConversationUsers();
      }

    };
  }

  private void pushToConversationView(Map<String,String> user, final Map<String,String> peer){

    List users = new ArrayList();
    users.add(user);
    users.add(peer);
    CustomUserProvider.getInstance().setAllUsers(users);
    LCChatKit.getInstance().setProfileProvider(CustomUserProvider.getInstance());
//    AVOSCloud.setDebugLogEnabled(true); //不开启调试模式

    LCIMConversationActivity.flutterImCallback = flutterImCallback;

    Intent intent = new Intent(activity, LCIMConversationActivity.class);
    intent.putExtra(LCIMConstants.PEER_ID, peer.get("user_id"));
    activity.startActivity(intent);
  }

  public void getRecentConversationUsers(final Result result){

    List conversationList = getConverstionList();
    result.success(conversationList);
  }

  public void refreshRecentConversationUsers(){

    List conversationList = getConverstionList();
    if (eventCallback != null){
      eventCallback.success(conversationList);
    }

  }

  private List getConverstionList(){

    List<String> convIdList = LCIMConversationItemCache.getInstance().getSortedConversationList();
    String clientId =  LCChatKit.getInstance().getClient().getClientId();

    List conversationList = new ArrayList<>();

    for (String convId : convIdList) {

      Map<String,Object> con = new HashMap<>();
      AVIMConversation conversation =  LCChatKit.getInstance().getClient().getConversation(convId);

      System.out.println("getMembers :"+ conversation.getMembers());
      // 获取peerId
      String peerId = "";
      if (conversation.getMembers().size() == 2){
        if (conversation.getMembers().get(0).toString().equals(clientId)){
          peerId = conversation.getMembers().get(1);
        }else {
          peerId = conversation.getMembers().get(0);
        }
      }else {
        continue;
      }

      // json to map
      Map<String,Object> content = null;

      if (conversation.getLastMessage() != null){

        //获取最新消息的时间
        SimpleDateFormat sdf= new SimpleDateFormat("MM-dd HH:mm");
        String lastMessageAt = sdf.format(conversation.getLastMessageAt());

        content = JSON.parseObject(conversation.getLastMessage().getContent());
        System.out.println("content :"+ content);

        Map<String,Object> attrs = (Map<String,Object>)content.get("_lcattrs");

        String peerName = "";

        //attrs为null表示最后一句话是当前用户说的
        if (attrs == null || attrs.get("username") == null) {
          peerName = peerId;
        }else {
          peerName = attrs.get("username").toString();
        }
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
        con.put("peerName",peerName);
        con.put("lastMessageContent",message);

      }else {
        con.put("clientId",clientId);
        con.put("peerId",peerId);
        con.put("unreadMessagesCount",conversation.getUnreadMessagesCount());
        con.put("lastMessageAt","");
        con.put("peerName",peerId);
        con.put("lastMessageContent","");

      }


      System.out.println("conversation :"+ con);

      conversationList.add(con);
    }
    return conversationList;
  }

  void setConversationEventToFlutter (){

    new EventChannel(messenger, "flutter_lc_im/conversation").setStreamHandler(
            new EventChannel.StreamHandler() {
              @Override
              // 这个onListen是Flutter端开始监听这个channel时的回调，第二个参数 EventSink是用来传数据的载体。
              public void onListen(Object arguments, EventChannel.EventSink events) {
                if (events != null){
                  eventCallback = events;
                }
              }

              @Override
              public void onCancel(Object arguments) {
                // 对面不再接收

              }
            }
    );
  }

  void setNotificationEventToFlutter (){

    new EventChannel(messenger, "flutter_lc_im/notification").setStreamHandler(
            new EventChannel.StreamHandler() {
              @Override
              // 这个onListen是Flutter端开始监听这个channel时的回调，第二个参数 EventSink是用来传数据的载体。
              public void onListen(Object arguments, EventChannel.EventSink events) {
                if (events != null){
                  notificationCallback = events;
                }
              }

              @Override
              public void onCancel(Object arguments) {
                // 对面不再接收

              }
            }
    );
  }
}