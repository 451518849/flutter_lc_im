package com.xiaofa.flutter_lc_im;

import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.PushService;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;

import java.util.ArrayList;
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
    else if (call.method.equals("pushToConversationView")){
      Map user = call.argument("user");
      Map peer = call.argument("peer");
      this.pushToConversationView(user,peer);
      result.success("");
    } else if (call.method.equals("getConversationList")){
      getConversationList(result);
    }
    else {
      result.notImplemented();
    }
  }

  private void initChatView(String appId, String appKey, final Result result){
//    AVOSCloud.useAVCloudUS();
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

  private void pushToConversationView(Map<String,String> user, final Map<String,String> peer){

    List users = new ArrayList();
    users.add(user);
    users.add(peer);
    CustomUserProvider.getInstance().setAllUsers(users);
    LCChatKit.getInstance().setProfileProvider(CustomUserProvider.getInstance());
    AVOSCloud.setDebugLogEnabled(true);

    LCChatKit.getInstance().open(user.get("user_id"), new AVIMClientCallback() {
      @Override
      public void done(AVIMClient avimClient, AVIMException e) {
        if (null == e) {

          Intent intent = new Intent(activity, LCIMConversationActivity.class);
          intent.putExtra(LCIMConstants.PEER_ID, peer.get("user_id"));
          activity.startActivity(intent);
        } else {

        }
      }
    });
  }

  public void getConversationList(final Result result){
      List<String> convIdList = LCIMConversationItemCache.getInstance().getSortedConversationList();
      List conversationList = new ArrayList<>();
          for (String convId : convIdList) {
        conversationList.add(LCChatKit.getInstance().getClient().getClientId());
      }
          result.success(conversationList);
  }
}
