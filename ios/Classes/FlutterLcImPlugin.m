#import "LCChatKit.h"
#import "LCCKUtil.h"
#import "LCChatKitHelper+Setting.h"
#import "FlutterLcImPlugin.h"
#import <UserNotifications/UserNotifications.h>
#import "LCCKUser.h"

static BOOL isRegister = false;

@interface FlutterLcImPlugin()<UNUserNotificationCenterDelegate>

@end

@implementation FlutterLcImPlugin

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"flutter_lc_im"
            binaryMessenger:[registrar messenger]];
  FlutterLcImPlugin* instance = [[FlutterLcImPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"getPlatformVersion" isEqualToString:call.method]) {
    result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  } else if ([@"register" isEqualToString:call.method]){
      
      if (!isRegister) {
          NSString *appId    = call.arguments[@"app_id"];
          NSString *appKey   = call.arguments[@"app_key"];
          NSString *clientId = call.arguments[@"user_id"];

          [FlutterLcImPlugin registerConversationWithAppId:appId
                                                    appKey:appKey
                                                  clientId:clientId
                                                    result:result];
          isRegister = true;
      }

      result(nil);
  }
  else if ([@"pushToConversationView" isEqualToString:call.method]) {
      [FlutterLcImPlugin loginWithUser:call.arguments[@"user"]
                                  peer:call.arguments[@"peer"]];
      result(nil);
  }
  else if ([@"getConversationList" isEqualToString:call.method]) {
      [FlutterLcImPlugin getConversationList:result];
  }
  else {
    result(FlutterMethodNotImplemented);
  }
}

+ (void)registerConversationWithAppId:(NSString *)appId
                               appKey:(NSString *)appKey
                             clientId:(NSString *)clientId
                               result:(FlutterResult)result{
    
    NSLog(@"register conversation");
    [FlutterLcImPlugin registerForRemoteNotification];

    [LCChatKit setAppId:appId appKey:appKey];
    // 启用未读消息
    [AVIMClient setUnreadNotificationEnabled:true];
    [AVIMClient setTimeoutIntervalInSeconds:20];
    //    //添加输入框底部插件，如需更换图标标题，可子类化，然后调用 `+registerSubclass`
    [LCCKInputViewPluginTakePhoto registerSubclass];
    [LCCKInputViewPluginPickImage registerSubclass];
    [LCCKInputViewPluginLocation registerSubclass];
    
    [LCCKUtil showProgressText:@"连接中..." duration:10.0f];
    [LCChatKitHelper invokeThisMethodAfterLoginSuccessWithClientId:clientId success:^{
         NSLog(@"login success@");
        [LCCKUtil hideProgress];
        result(nil);
    } failed:^(NSError *error) {
        [LCCKUtil hideProgress];
        NSLog(@"login error");
        [LCCKUtil hideProgress];
        result(@"login error");
    }];
}

+(void)getConversationList:(FlutterResult)result{
    
    NSMutableArray *userIds = [NSMutableArray array];
    [[LCCKConversationListService sharedInstance] findRecentConversationsWithBlock:^(NSArray *conversations, NSInteger totalUnreadCount, NSError *error) {
        
        [conversations enumerateObjectsUsingBlock:^(AVIMConversation *obj, NSUInteger idx, BOOL * _Nonnull stop) {
            [userIds addObject:obj.clientId];
        }];
        result(userIds);
    }];
    
}

+ (void)loginWithUser:(NSDictionary *)userDic peer:(NSDictionary *)peerDic{
    
    LCCKUser *user = [[LCCKUser alloc] initWithUserId:userDic[@"user_id"] name:userDic[@"name"] avatarURL:userDic[@"avatar_url"]];
    LCCKUser *peer = [[LCCKUser alloc] initWithUserId:peerDic[@"user_id"] name:peerDic[@"name"] avatarURL:peerDic[@"avatar_url"]];

    NSMutableArray *users = [NSMutableArray arrayWithCapacity:2];
    [users addObject:user];
    [users addObject:peer];
    
    [[LCChatKitHelper sharedInstance] lcck_settingWithUsers:users];
    [LCChatKitHelper openConversationViewControllerWithPeerId:peer.userId];
    
}
//+ (void)loginWithUserId:(NSString *)userId peerId:(NSString *)peerId appUrl:(NSString *)url{
//
//    [FlutterLcImPlugin registerForRemoteNotification];
//
//    [LCCKUtil showProgressText:@"open client ..." duration:10.0f];
//    [LCChatKitHelper invokeThisMethodAfterLoginSuccessWithClientId:userId appUrl:url success:^{
//        [LCCKUtil hideProgress];
//        [LCChatKitHelper openConversationViewControllerWithPeerId:peerId];
//    } failed:^(NSError *error) {
//        [LCCKUtil hideProgress];
//        NSLog(@"%@",error);
//    }];
//}
//


/**
 * 初始化UNUserNotificationCenter
 */
+ (void)registerForRemoteNotification {
    // iOS 10 兼容
    if (SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"10.0")) {
        
#if XCODE_VERSION_GREATER_THAN_OR_EQUAL_TO_8
        
        // 使用 UNUserNotificationCenter 来管理通知
        UNUserNotificationCenter *uncenter = [UNUserNotificationCenter currentNotificationCenter];
        // 监听回调事件
        [uncenter setDelegate:self];
        //iOS 10 使用以下方法注册，才能得到授权
        [uncenter requestAuthorizationWithOptions:(UNAuthorizationOptionAlert+UNAuthorizationOptionBadge+UNAuthorizationOptionSound)
                                completionHandler:^(BOOL granted, NSError * _Nullable error)
         {
             dispatch_async(dispatch_get_main_queue(), ^{
                 [[UIApplication sharedApplication] registerForRemoteNotifications];
             });
             //TODO:授权状态改变
             NSLog(@"%@" , granted ? @"授权成功" : @"授权失败");
         }];
        // 获取当前的通知授权状态, UNNotificationSettings
        [uncenter getNotificationSettingsWithCompletionHandler:^(UNNotificationSettings * _Nonnull settings) {
            
            NSLog(@"%s\nline:%@\n-----\n%@\n\n", __func__, @(__LINE__), settings);
            /*
             UNAuthorizationStatusNotDetermined : 没有做出选择
             UNAuthorizationStatusDenied : 用户未授权
             UNAuthorizationStatusAuthorized ：用户已授权
             */
            if (settings.authorizationStatus == UNAuthorizationStatusNotDetermined) {
                NSLog(@"未选择");
            } else if (settings.authorizationStatus == UNAuthorizationStatusDenied) {
                NSLog(@"未授权");
            } else if (settings.authorizationStatus == UNAuthorizationStatusAuthorized) {
                NSLog(@"已授权");
            }
        }];
        
#endif
        
    }
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wdeprecated-declarations"
    else if (SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"8.0")) {
        UIUserNotificationType types = UIUserNotificationTypeAlert |
        UIUserNotificationTypeBadge |
        UIUserNotificationTypeSound;
        UIUserNotificationSettings *settings = [UIUserNotificationSettings settingsForTypes:types categories:nil];
        
        [[UIApplication sharedApplication] registerUserNotificationSettings:settings];
        [[UIApplication sharedApplication] registerForRemoteNotifications];
    } else {
        UIRemoteNotificationType types = UIRemoteNotificationTypeBadge |
        UIRemoteNotificationTypeAlert |
        UIRemoteNotificationTypeSound;
        [[UIApplication sharedApplication] registerForRemoteNotificationTypes:types];
    }
#pragma clang diagnostic pop
}


#pragma mark UNUserNotificationCenterDelegate
#pragma mark - 添加处理 APNs 通知回调方法
///=============================================================================
/// @name 添加处理APNs通知回调方法
///=============================================================================

#pragma mark -
#pragma mark - UNUserNotificationCenterDelegate Method

#if XCODE_VERSION_GREATER_THAN_OR_EQUAL_TO_8

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

/**
 * Required for iOS 10+
 * 在后台和启动之前收到推送内容, 点击推送后执行的方法
 */
- (void)userNotificationCenter:(UNUserNotificationCenter *)center
didReceiveNotificationResponse:(UNNotificationResponse *)response
         withCompletionHandler:(void (^)(void))completionHandler
{
    NSDictionary *userInfo = response.notification.request.content.userInfo;
    if([response.notification.request.trigger isKindOfClass:[UNPushNotificationTrigger class]]) {
        //TODO:处理远程推送内容
        NSLog(@"%@", userInfo);
    }
    completionHandler();
}

#endif




@end
