package com.xiaofa.flutter_lc_im;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import cn.leancloud.AVInstallation;
import cn.leancloud.AVLogger;
import cn.leancloud.AVOSCloud;
import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import cn.leancloud.im.AVIMOptions;
import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.AVIMConversation;
import cn.leancloud.im.v2.AVIMException;
import cn.leancloud.im.v2.AVIMMessageManager;
import cn.leancloud.im.v2.annotation.AVIMMessageType;
import cn.leancloud.im.v2.callback.AVIMClientCallback;
import cn.leancloud.im.v2.callback.AVIMConversationCreatedCallback;
import cn.leancloud.push.PushService;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/** FlutterLcImPlugin */
public class FlutterLcImPlugin implements FlutterPlugin, ActivityAware, MethodCallHandler {

  public static final String FLUTTER_IM_NAME = "flutter_lc_im";
  public static final String FLUTTER_CHANNEL_MESSAGE = "flutter_lc_im/messages";
  public static final String FLUTTER_CHANNEL_CONVERSATION = "flutter_lc_im/conversations";
  public static final String FLUTTER_CHANNEL_NOTIFICATION = "flutter_lc_im/notifications";

  static boolean isRegister = false;

  private AVIMClient client;
  private LCConversation conversation;

  static Context context;
  static Activity activity;
  static BinaryMessenger messenger;

  private EventChannel.EventSink conversationEventCallback;
  private EventChannel.EventSink messageEventCallback;

  private LCConversationEventHandler conversationEventHandler;
  private LCMessageHandler messageHandler;

  //留给客户端做数据交互
  public static EventChannel.EventSink notificationEventCallback;

  public FlutterLcImPlugin() {
  }

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    context = flutterPluginBinding.getApplicationContext();
    messenger = flutterPluginBinding.getBinaryMessenger();
    final MethodChannel channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), FLUTTER_IM_NAME);
    channel.setMethodCallHandler(new FlutterLcImPlugin());
  }


  // This static function is optional and equivalent to onAttachedToEngine. It supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both be defined
  // in the same class.
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), FLUTTER_IM_NAME);
    FlutterLcImPlugin im = new FlutterLcImPlugin();
    context = registrar.context();
    activity = registrar.activity();
    messenger = registrar.messenger();
    channel.setMethodCallHandler(im);
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
  }

  @Override
  public void onAttachedToActivity(ActivityPluginBinding binding) {
    activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {

  }

  @Override
  public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {

  }

  @Override
  public void onDetachedFromActivity() {

  }


  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {

    switch (call.method) {
      case "register":
        if (!isRegister) {

          String appId = call.argument("app_id");
          String appKey = call.argument("app_key");
          String api = call.argument("api");
          boolean debug = call.argument("debug");
          this.initSetting(appId, appKey, api, debug);
          isRegister = true;

        } else {

          result.success("success");

        }
        break;
      case "login":

        String clientId = call.argument("client_id");
        LCPushService.isOpen = call.argument("notification");

        this.login(clientId, result);
        break;

      case "createConversation":

        String peerId = call.argument("peer_id");
        int limit = call.argument("limit");

        this.createConversation(peerId,limit,call.argument("attributes"));
        break;

      case "sendTextMessage":

        String text = call.argument("text");
        Map attributes = call.argument("attributes");
        LCMessage message = new LCMessage();
        message.setText(text);
        message.setAttributes(attributes);
        message.setMessageType(AVIMMessageType.TEXT_MESSAGE_TYPE);
        try {
          this.conversation.sendLcMessage(message);
        } catch (IOException e) {
          e.printStackTrace();
        }
        break;

      case "sendImageMessage":

        String path = call.argument("path");
        attributes = call.argument("attributes");
        message = new LCMessage();
        message.setPhotoPath(path);
        message.setAttributes(attributes);
        message.setMessageType(AVIMMessageType.IMAGE_MESSAGE_TYPE);
        try {
          this.conversation.sendLcMessage(message);
        } catch (IOException e) {
          e.printStackTrace();
        }
        break;

      case "sendVoiceMessage":

        path = call.argument("path");
        String duration = call.argument("duration");
        attributes = call.argument("attributes");

        message = new LCMessage();
        message.setVoicePath(path);
        message.setVoiceDuration(duration);
        message.setAttributes(attributes);
        message.setMessageType(AVIMMessageType.AUDIO_MESSAGE_TYPE);
        try {
          this.conversation.sendLcMessage(message);
        } catch (IOException e) {
          e.printStackTrace();
        }
        break;

      case "sendVideoMessage":

        path = call.argument("path");
        duration = call.argument("duration");
        attributes = call.argument("attributes");

        message = new LCMessage();
        message.setVideoPath(path);
        message.setVideoDuration(duration);
        message.setAttributes(attributes);
        message.setMessageType(AVIMMessageType.VIDEO_MESSAGE_TYPE);
        try {
          this.conversation.sendLcMessage(message);
        } catch (IOException e) {
          e.printStackTrace();
        }
        break;

      case "queryHistoryConversations":

        limit = call.argument("limit");
        int offset = call.argument("offset");
        LCConversation.queryHistoryConversations(this.client,limit, offset,conversationEventCallback);
        break;

      case "queryHistoryConversationMessages":

        limit = call.argument("limit");
        String messageId = call.argument("message_id");
        long timestamp = call.argument("timestamp");
        this.conversation.queryHistoryConversationMessages(limit,messageId,timestamp,messageEventCallback);
        break;

      case "logout":
        this.logout();
        break;

      default:
        result.notImplemented();
        break;
    }
  }

  /*
   *  开启聊天
   * */
  private void initSetting(String appId, String appKey, String api,boolean debug) {

    if (debug){
      AVOSCloud.setLogLevel(AVLogger.Level.DEBUG);
    }
    AVOSCloud.initialize(context, appId, appKey, api);
    AVIMOptions.getGlobalOptions().setUnreadNotificationEnabled(true);
    this.setConversationEventHandler();
    initFlutterChannels();
  }

  /*
   *  初始化聊天信息
   * */
  private void login(String userId, final Result result) {

    //初始化推送
    setPushSetting(userId);
    LCConvertUtils.clientId = userId;

    this.client = AVIMClient.getInstance(userId);
    this.client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (e == null) {
          // 成功打开连接
          System.out.println("聊天功能建立成功！");
        }
      }
    });
  }

  /**
   * 退出登录
   */
  private void logout(){
    this.client.close(new AVIMClientCallback(){
      @Override
      public void done(AVIMClient client,AVIMException e){
        if(e==null){
          // 登出成功
          System.out.println("退出登录！");
        }
      }
    });
  }

  /**
   * 初始化推送，订阅clientId所在的频道
   * @param clientId
   */
  private void setPushSetting(String clientId){
    LCPushService.setDefaultChannelId(context,activity,"default");
    PushService.setDefaultChannelId(context, "default");//这个channel和订阅的channel不一样，只能为default
    PushService.subscribe(context,clientId,activity.getClass()); //订阅频道,这一步必须，否则无法收到推送
    PushService.setDefaultPushCallback(context,activity.getClass());

    AVInstallation.getCurrentInstallation().saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable d) {
      }
      @Override
      public void onNext(AVObject avObject) {
        // 关联 installationId 到用户表等操作。
        String installationId = AVInstallation.getCurrentInstallation().getInstallationId();
        System.out.println("保存成功：" + installationId );
      }
      @Override
      public void onError(Throwable e) {
        System.out.println("保存失败，错误信息：" + e.getMessage());
      }
      @Override
      public void onComplete() {
      }
    });
  }
  /*
   * 接收会话handler
   * 接收消息handle
   */
  private void setConversationEventHandler(){
    conversationEventHandler = new LCConversationEventHandler();
    messageHandler = new LCMessageHandler();
    AVIMMessageManager.setConversationEventHandler(conversationEventHandler);
    AVIMMessageManager.registerDefaultMessageHandler(messageHandler);

  }


  /*
   * 初始化flutter和android之间的信道
   * */
  private void initFlutterChannels() {
    setConversationMessageEventToFlutter();
    setConversationEventToFlutter();
    setNotificationEventToFlutter();
  }


  private void createConversation(String peerId, final int limit, Map<String, Object> attributes) {
    this.client.createConversation(Arrays.asList(peerId), this.client.getClientId() + "&" + peerId, attributes, false, true,
            new AVIMConversationCreatedCallback() {
              @Override
              public void done(AVIMConversation con, AVIMException e) {
                if (e == null) {
                  // 创建成功
                  System.out.println("会话创建成功");
                  conversation = new LCConversation(client,con);
                  conversation.read();
                  conversation.queryHistoryConversationMessages(limit,null,0,messageEventCallback);
                }
              }
            });
  }


  void setConversationEventToFlutter() {

    new EventChannel(messenger, FLUTTER_CHANNEL_CONVERSATION).setStreamHandler(
            new EventChannel.StreamHandler() {
              @Override
              // 这个onListen是Flutter端开始监听这个channel时的回调，第二个参数 EventSink是用来传数据的载体。
              public void onListen(Object arguments, EventChannel.EventSink events) {
                if (events != null) {
                  conversationEventCallback = events;
                  conversationEventHandler.conversationEventCallback = events;
                  messageHandler.conversationEventCallback = events;
                }
              }

              @Override
              public void onCancel(Object arguments) {
                // 对面不再接收

              }
            }
    );
  }

  void setConversationMessageEventToFlutter() {

    new EventChannel(messenger, FLUTTER_CHANNEL_MESSAGE).setStreamHandler(
            new EventChannel.StreamHandler() {
              @Override
              // 这个onListen是Flutter端开始监听这个channel时的回调，第二个参数 EventSink是用来传数据的载体。
              public void onListen(Object arguments, EventChannel.EventSink events) {
                if (events != null) {
                  messageEventCallback = events;
                  messageHandler.messageEventCallback = events;
                }
              }

              @Override
              public void onCancel(Object arguments) {
                // 对面不再接收

              }
            }
    );
  }

  void setNotificationEventToFlutter() {

    new EventChannel(messenger, FLUTTER_CHANNEL_NOTIFICATION).setStreamHandler(
            new EventChannel.StreamHandler() {
              @Override
              // 这个onListen是Flutter端开始监听这个channel时的回调，第二个参数 EventSink是用来传数据的载体。
              public void onListen(Object arguments, EventChannel.EventSink events) {
                if (events != null) {
                  notificationEventCallback = events;
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
