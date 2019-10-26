# flutter_lc_im
![](https://img.shields.io/badge/build-passing-brightgreen)
![](https://img.shields.io/badge/version-0.2.5-orange)
![](https://img.shields.io/badge/platform-ios%7Candroid-lightgrey)
![](https://img.shields.io/badge/license-MIT-blue)


![](index.jpeg)
![](list.jpeg)

**超级简单、轻量的Flutter聊天插件,支持ios和android两个平台。关键是免费！！！强势推荐一波LeanCloud！！! 这可能就是你要找的免费又好用的 flutter im。好用请给star，后续将提供更多的功能。**

**框架地址：
[flutter_lc_im 0.2.5](https://pub.dev/packages/flutter_lc_im#-readme-tab-)**

### 封装的功能有

	1. 一对一聊天
	2. 获取聊天列表（可自定义列表UI）
	3. 根据聊天情况刷新聊天列表
	4. 给出聊天列表上的未读消息数
	5. 即时消息推送（推送提示）
	6. 远程消息推送（推送提示）
    7. 推送消息可点击进入指定flutter页面
### 安装方式
Add this to your package's pubspec.yaml file:

	dependencies:
		flutter_lc_im: ^0.2.5
		  
	flutter packages get

### Flutter中的实现如下：

#### 1. 单聊功能

        FlutterLcIm.register("appId", "appKey"，"api");
        FlutterLcIm.login("当前用户的userId");
        Map user = {'name':'jason1','user_id':"1",'avatar_url':"http://thirdqq.qlogo.cn/g?b=oidb&k=h22EA0NsicnjEqG4OEcqKyg&s=100"};
        Map peer = {'name':'jason2','user_id':"3",'avatar_url':"http://thirdqq.qlogo.cn/g?b=oidb&k=h22EA0NsicnjEqG4OEcqKyg&s=100"};
        //跳转到聊天界面
        FlutterLcIm.pushToConversationView(user,peer);
        
        
#### 2. 获取聊天列表功能
        
        // 第一步需要实现一下channel
	     EventChannel eventChannel = const EventChannel('flutter_lc_im/conversation');
	  eventChannel.receiveBroadcastStream('flutter_lc_im/conversation').listen(
	      (Object event) {
	    Conversations conversations = Conversations.fromJson(event);
	    ctx.state.conversations = conversations.conversations;
	    _fetchImUsers(action, ctx);
	  }, onError: _onError);
	  
        //最近联系人列表
        FlutterLcIm.getRecentConversationUsers().then((res) {
          if (res != [] && res != null) {
            Conversations conversations = Conversations.fromJson(res);
                      print('conversations:${conversations.conversations}');

            Navigator.of(context).push(MaterialPageRoute(
                builder: (BuildContext context) => ConversationListPage(conversations: conversations,)));
          }        
FlutterLcIm.pushToConversationView中第一个参数user指的是当前用户，第二个参数peer是聊天对象.

#### 3. 推送点击跳转

    //高级功能，请选择性使用
	//点击消息进行跳转，
	typedef NotificationCallback = void Function(Map<String, dynamic> msg);
	void iMNotification(NotificationCallback notificationCallback) {
	  EventChannel eventChannel = const EventChannel('flutter_lc_im/notification');
	  eventChannel.receiveBroadcastStream('flutter_lc_im/notification').listen(
	      (Object event) {
	    print('通知回调消息:$event');
	    Map<String, dynamic> msg;
	    if (event is String) {
	      msg = json.decode(event);
	    } else {
	      msg = Map<String, dynamic>.from(event);
	    }
	    if (msg['target'] != null) {
	      notificationCallback(msg['target'].cast<String, dynamic>());
	    }
	  }, onError: (Object error) {});
	}
	
	 推送消息格式大致如下，请自行大了LC文档上学习：
	 ios   {"data":{"alert":"你好。",target:{xxxxxxxxx}},"where":{"deviceType":"ios"},"prod":"dev","channels":[ "6"]}
	 android {"data":{"title":"你好。",target:{xxxxxxxxx}},"where":{"deviceType":"android"},"prod":"dev","channels":[ "6"]}

## Getting Andriod Started
#### 第一步 AndroidManifest.xml加入以下配置
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.RECORD_VIDEO" />
    <uses-permission android:name="android.permission.READ_LOGS" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.VIBRATE" />
    
    <application
       android:theme="@style/LCIMKitTheme" >  //加入这个
		<activity
		xxxxxxxxx
		</activity>
	</application>
#### 第二步 Flutter项目中加入想要的功能

## Getting iOS Started

#### 第一步 info.plist加入以下配置
	<?xml version="1.0" encoding="UTF-8"?>
	<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
	<plist version="1.0">
	<dict>
		<key>NSAllowsArbitraryLoads</key>
		<true/>
	</dict>
	</plist>
	
	<?xml version="1.0" encoding="UTF-8"?>
	<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
	<plist version="1.0">
	<array>
		<string>fetch</string>
		<string>remote-notification</string>
	</array>
	</plist>
	
#### 第二步 AppDelegate.m中加入以下代码：

	#import <ChatKit/LCChatKit.h>
	#import <UserNotifications/UserNotifications.h>
	
	- (BOOL)application:(UIApplication *)application
    didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    
    [GeneratedPluginRegistrant registerWithRegistry:self];
    // Override point for customization after application launch.
        
    [UIApplication sharedApplication].statusBarStyle = UIStatusBarStyleLightContent;
    
    [self registerForRemoteNotification];
    
    if(launchOptions != nil){
        //这个方法用于收到推送后发给原生处理，用于收到推送后打开指定页面
        [FlutterLcImPlugin sendNotification:launchOptions];
    }
    return [super application:application didFinishLaunchingWithOptions:launchOptions];
    }

	- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {

	    // ios 13.0前的处理方式
	    // [AVOSCloud handleRemoteNotificationsWithDeviceToken:deviceToken];
	    
	    // ios 13.0之后的处理方式
	        AVInstallation *installation = [AVInstallation defaultInstallation];
           [installation setDeviceTokenFromData:deviceToken teamId:@"xxxxxx"];
    	   [installation saveInBackground];
	}
	
	- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo {
	    if (application.applicationState == UIApplicationStateActive) {
	    } else {
	
	        [[LCChatKit sharedInstance] didReceiveRemoteNotification:userInfo];
	    }
	}
	- (void)registerForRemoteNotification {.........}
	..........
	..........
	代理方法较多这里就不粘出来了，直接参考exmaple中的AppDelegate.m中的代码，可以直接复制使用。
	
传送门：[AppDelegate.m](https://github.com/451518849/flutter_lc_im/blob/master/example/ios/Runner/AppDelegate.m)。



#### 第三步 Flutter项目加入想要的功能
       
### QQ技术交流：
群聊号：853797155，欢迎交流问题和技术！

### 欢迎PR