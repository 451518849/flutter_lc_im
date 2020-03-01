package com.example.flutter_lc_im;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.leancloud.AVLogger;
import cn.leancloud.AVOSCloud;
import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.AVIMConversation;
import cn.leancloud.im.v2.AVIMConversationsQuery;
import cn.leancloud.im.v2.AVIMException;
import cn.leancloud.im.v2.AVIMMessage;
import cn.leancloud.im.v2.AVIMMessageManager;
import cn.leancloud.im.v2.callback.AVIMClientCallback;
import cn.leancloud.im.v2.callback.AVIMConversationCreatedCallback;
import cn.leancloud.im.v2.callback.AVIMConversationQueryCallback;
import cn.leancloud.im.v2.callback.AVIMMessagesQueryCallback;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** FlutterLcImPlugin */
public class FlutterLcImPlugin implements FlutterPlugin, MethodCallHandler {

  public static final String FLUTTER_IM_NAME = "flutter_lc_im";
  public static final String FLUTTER_CHANNEL_MESSAGE = "flutter_lc_im/messages";
  public static final String FLUTTER_CHANNEL_CONVERSATION = "flutter_lc_im/conversations";
  public static final String FLUTTER_CHANNEL_NOTIFICATION = "flutter_lc_im/notifications";

  static boolean isRegister = false;

  private AVIMClient client;
  private AVIMConversation conversation;

  private Context context;
  private Activity activity;

  static BinaryMessenger messenger;
  static EventChannel.EventSink conversationEventCallback;
  static EventChannel.EventSink messageEventCallback;
  public static EventChannel.EventSink notificationEventCallback;

  public FlutterLcImPlugin() {
  }
  private FlutterLcImPlugin(Registrar registrar) {
    context = registrar.context();
    activity = registrar.activity();
  }

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    final MethodChannel channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "flutter_lc_im");
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
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_lc_im");
    messenger = registrar.messenger();
    FlutterLcImPlugin instance = new FlutterLcImPlugin(registrar);
    channel.setMethodCallHandler(instance);

  }
  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
  }

//  @Override
//  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
//    if (call.method.equals("getPlatformVersion")) {
//      result.success("Android " + android.os.Build.VERSION.RELEASE);
//    } else {
//      result.notImplemented();
//    }
//  }


  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {

    switch (call.method) {
      case "register":
        if (!isRegister) {

          String appId = call.argument("app_id");
          String appKey = call.argument("app_key");
          String api = call.argument("api");

          this.initSetting(appId, appKey, api, result);
          isRegister = true;

        } else {

          result.success("success");

        }
        break;
      case "login":

        String clientId = call.argument("client_id");
        this.login(clientId, result);
        break;

      case "createConversation":

        clientId = call.argument("client_id");
        String peerId = call.argument("peer_id");
        this.createConversation(clientId, peerId);
        break;

      case "queryHistoryConversations":

        int limit = call.argument("limit");
        int offset = call.argument("offset");
        this.queryHistoryConversations(limit, offset);
        break;

      case "queryHistoryConversationMessages":

        limit = call.argument("limit");
        String messageId = call.argument("message_id");
        long timestamp = call.argument("timestamp");
        this.queryHistoryConversationMessages(limit,messageId,timestamp);
        break;

      default:
        result.notImplemented();
        break;
    }
  }

  /*
   *  开启聊天
   * */
  private void initSetting(String appId, String appKey, String api, final Result result) {

    AVOSCloud.setLogLevel(AVLogger.Level.DEBUG);
    AVOSCloud.initialize(context, appId, appKey, api);

    initFlutterChannels();
  }

  /*
   *  初始化聊天信息
   * */
  private void login(String userId, final Result result) {

    this.client = AVIMClient.getInstance(userId);
    this.client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (e == null) {
          // 成功打开连接
          System.out.println("聊天功能建立成功！");
          setConversationEventHandler();;
        }
      }
    });
  }

  /*
   * 接收消息
   */
  private void setConversationEventHandler(){
    AVIMMessageManager.setConversationEventHandler(new LCConversationEventHandler(conversationEventCallback));
    AVIMMessageManager.registerDefaultMessageHandler(new LCMessageHandler(messageEventCallback));
  }


  /*
   * 初始化flutter和android之间的信道
   * */
  private void initFlutterChannels() {
    setConversationEventToFlutter();
    setNotificationEventToFlutter();
    setConversationMessageEventToFlutter();
  }


//  private void initPushService(String userId, final Result result){
//
//    PushService.setAutoWakeUp(true);
//    PushService.setDefaultChannelId(context, "default");//这个channel和订阅的channel不一样，只能为default
//    PushService.subscribe(context,userId,activity.getClass()); //订阅频道,这一步必须，否则无法收到推送
//    PushService.setDefaultPushCallback(context,activity.getClass());
//    AVOSCloud.setDebugLogEnabled(true);
//
//    //每次登录后设置推送
//    AVIMMessageManager.registerMessageHandler(AVIMTypedMessage.class,new CustomMessageHandler(context));
//    AVInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
//      public void done(AVException e) {
//        if (e == null) {
//          // 保存成功
//          String installationId = AVInstallation.getCurrentInstallation().getInstallationId();
//          System.out.println("---  " + installationId);
//        } else {
//          // 保存失败，输出错误信息
//          System.out.println("failed to save installation.");
//        }
//      }
//    });
//  }

  private void createConversation(String clientId, String peerId) {
    this.client.createConversation(Arrays.asList(peerId), clientId + "&" + peerId, null, false, true,
            new AVIMConversationCreatedCallback() {
              @Override
              public void done(AVIMConversation con, AVIMException e) {
                if (e == null) {
                  // 创建成功
                  System.out.println("会话创建成功");
                  conversation = con;
                }
              }
            });
  }

  private void queryHistoryConversationMessages(int limit, String messageId, long timestamp) {

    if (messageId == null) {
      this.conversation.queryMessages(limit, new AVIMMessagesQueryCallback() {
        @Override
        public void done(List<AVIMMessage> messages, AVIMException e) {
          if (e == null) {
            convertConversationMessagesToFlutterArray(messages);
          }
        }
      });
    } else {
      this.conversation.queryMessages(messageId, timestamp, limit,
              new AVIMMessagesQueryCallback() {
                @Override
                public void done(List<AVIMMessage> messagesInPage, AVIMException e) {
                  if (e == null) {
                    // 查询成功返回
                    convertConversationMessagesToFlutterArray(messagesInPage);

                  }
                }
              });
    }
  }

  private void queryHistoryConversations(int limit, int offset) {
    AVIMConversationsQuery query = this.client.getConversationsQuery();
    query.limit(limit);
    query.skip(offset);

    query.findInBackground(new AVIMConversationQueryCallback() {
      @Override
      public void done(List<AVIMConversation> convs, AVIMException e) {
        if (e == null) {
          // convs 就是想要的结果
          System.out.println("AVIMConversation list:"+convs);
          convertConversationsToFlutterArray(convs);
        }
      }
    });
  }

  private void convertConversationsToFlutterArray(List<AVIMConversation> convs) {
    ArrayList conversations = new ArrayList();
    for (AVIMConversation con : convs) {

      if(con.getLastMessage() == null){
        continue;
      }
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      String dateString = formatter.format(con.getLastMessageAt());

      Map<String, Object> dic = new HashMap<>();
      dic.put("conversationId", con.getConversationId());
      dic.put("clientId", con.getCreator());
      dic.put("members", con.getMembers());
      dic.put("unreadMessagesCount", con.getUnreadMessagesCount());
      dic.put("lastMessage", con.getLastMessage().getContent());
      dic.put("lastMessageAt", dateString);
      conversations.add(dic);
    }
    conversationEventCallback.success(conversations);
  }

  private void convertConversationMessagesToFlutterArray(List<AVIMMessage> mess) {
    ArrayList messages = new ArrayList();
    for (AVIMMessage message : mess) {
      Map<String, Object> dic = new HashMap<>();
      dic.put("messageId", message.getMessageId());
      dic.put("clientId", message.getFrom());
      dic.put("conversationId", message.getConversationId());
      dic.put("content", message.getContent());
      dic.put("timestamp", message.getTimestamp());
      dic.put("ioType", message.getMessageIOType());
      messages.add(dic);
    }
    messageEventCallback.success(messages);
  }


  void setConversationEventToFlutter() {

    new EventChannel(messenger, FLUTTER_CHANNEL_CONVERSATION).setStreamHandler(
            new EventChannel.StreamHandler() {
              @Override
              // 这个onListen是Flutter端开始监听这个channel时的回调，第二个参数 EventSink是用来传数据的载体。
              public void onListen(Object arguments, EventChannel.EventSink events) {
                if (events != null) {
                  conversationEventCallback = events;
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
