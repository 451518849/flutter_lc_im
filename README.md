# flutter_lc_im

简单封装LeanCloud的IM功能，目前处于研发阶段，只封装了iOS端.

## Getting Started

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
	  // Override point for customization after application launch.
	    [GeneratedPluginRegistrant registerWithRegistry:self];
	
	    [LCChatKit setAppId:@"xxxxx" appKey:@"xxxxxx"];
	    // 启用未读消息
	    
	    [AVIMClient setUnreadNotificationEnabled:true];
	    [AVIMClient setTimeoutIntervalInSeconds:20];
	    //    //添加输入框底部插件，如需更换图标标题，可子类化，然后调用 `+registerSubclass`
	    [LCCKInputViewPluginTakePhoto registerSubclass];
	    [LCCKInputViewPluginPickImage registerSubclass];
	    [LCCKInputViewPluginLocation registerSubclass];
	    
	  return [super application:application didFinishLaunchingWithOptions:launchOptions];
	}
	
	- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
	    [AVOSCloud handleRemoteNotificationsWithDeviceToken:deviceToken];
	}
	
	- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo {
	    if (application.applicationState == UIApplicationStateActive) {
	    } else {
	
	        [[LCChatKit sharedInstance] didReceiveRemoteNotification:userInfo];
	    }
	}

	- (BOOL)application:(UIApplication *)app openURL:(NSURL *)url options:(NSDictionary<NSString *,id> *)options {
	    return YES;
	}
	
	- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation {
	    return YES;
	}


	/**
	 * Required for iOS 10+
	 * 在前台收到推送内容, 执行的方法
	 */
	- (void)userNotificationCenter:(UNUserNotificationCenter *)center
	       willPresentNotification:(UNNotification *)notification
	         withCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler {
	    NSDictionary *userInfo = notification.request.content.userInfo;
	    if([notification.request.trigger isKindOfClass:[UNPushNotificationTrigger class]]) {
	        //TODO:处理远程推送内容
	        NSLog(@"%@", userInfo);
	    }
	    // 需要执行这个方法，选择是否提醒用户，有 Badge、Sound、Alert 三种类型可以选择设置
	    completionHandler(UNNotificationPresentationOptionAlert);
	}

	/*!
	 * Required for iOS 7+
	 */
	- (void)application:(UIApplication *)application
	didReceiveRemoteNotification:(NSDictionary *)userInfo
	fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler {
	    //TODO:处理远程推送内容
	    NSLog(@"%@", userInfo);
	    // Must be called when finished
	    completionHandler(UIBackgroundFetchResultNewData);
	}
	
	#pragma mark - 实现注册APNs失败接口（可选）
	///=============================================================================
	/// @name 实现注册APNs失败接口（可选）
	///=============================================================================
	
	/**
	 * also used in iOS10
	 */
	- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error {
	    NSLog(@"%s\n[无法注册远程提醒, 错误信息]\nline:%@\n-----\n%@\n\n", __func__, @(__LINE__), error);
	}

#### 第三步 Flutter项目中加入以下代码：

	FlutterLcIm.pushToChatView("userId","peerId", "baseInfoUrl");

FlutterLcIm.pushToChatView中第一个参数userId指的是当前用户的ID，第二个参数peerId是聊天对象的ID，第三个参数baseInfoUrl指的是获取用户信息的接口，格式如下：
http://www.example.com/im/users,接口内参数格式如下：

	baseInfoUrl = http://www.example.com/im/users
	user info url = http://www.example.com/im/users/1
	user info url返回的数据如下:
	{
	    "success": true,
	    "errors": "",
	    "result": {
	        "username": "vector",
	        "avatar_url": "http://thirdqq.qlogo.cn/g?b=oidb&k=h22EA0NsicnjEqG4OEcqKyg&s=100",
	        "user_id": 1
	    }
	}

