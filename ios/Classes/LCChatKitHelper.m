//
//  LCChatKitHelper.m
//  AVOSCloud
//
//  Created by 小发工作室 on 2019/5/14.
//

#import "LCChatKitHelper.h"
#import "LCChatKitHelper+Setting.h"
#import "LCCKUtil.h"
#import "LCCKConst.h"
#import "NSObject+LCCKHUD.h"
#import "ConverationViewController.h"

@implementation LCChatKitHelper

#pragma mark - SDK Life Control

/**
 * create a singleton instance of LCChatKitHelper
 */
+ (instancetype)sharedInstance {
    static LCChatKitHelper *_shareInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _shareInstance = [[self alloc] init];
    });
    return _shareInstance;
}

#pragma -
#pragma mark - init Method

+ (void)invokeThisMethodBeforeLogoutSuccess:(LCCKVoidBlock)success failed:(LCCKErrorBlock)failed {
    //    [AVOSCloudIM handleRemoteNotificationsWithDeviceToken:nil];
    [[LCChatKit sharedInstance] removeAllCachedProfiles];
    [[LCChatKit sharedInstance] closeWithCallback:^(BOOL succeeded, NSError *error) {
        if (succeeded) {
            [self lcck_clearLocalClientInfo];
            [LCCKUtil showNotificationWithTitle:@"退出成功"
                                       subtitle:nil
                                           type:LCCKMessageNotificationTypeSuccess];
            !success ?: success();
        } else {
            [LCCKUtil showNotificationWithTitle:@"退出失败"
                                       subtitle:nil
                                           type:LCCKMessageNotificationTypeError];
            !failed ?: failed(error);
        }
    }];
}

+ (void)invokeThisMethodAfterLoginSuccessWithClientId:(NSString *)clientId
                                               appUrl:(NSString *)url
                                              success:(LCCKVoidBlock)success
                                               failed:(LCCKErrorBlock)failed {
    [[self sharedInstance] lcck_settingWithAppUrl:url];
    [[LCChatKit sharedInstance] openWithClientId:clientId
                                        callback:^(BOOL succeeded, NSError *error) {
                                            if (succeeded) {
                                                [self saveLocalClientInfo:clientId];
                                                !success ?: success();
                                            } else {
                                                [LCCKUtil showNotificationWithTitle:@"登陆失败"
                                                                           subtitle:nil
                                                                               type:LCCKMessageNotificationTypeError];
                                                !failed ?: failed(error);
                                            }
                                        }];
    // 
}

/**
 *  打开单聊页面
 */
+ (void)openConversationViewControllerWithPeerId:(NSString *)peerId {
    
    ConverationViewController *conversationViewController =
    [[ConverationViewController alloc] initWithPeerId:peerId];
    [conversationViewController
     setViewWillDisappearBlock:^(LCCKBaseViewController *viewController, BOOL aAnimated) {
         [self lcck_hideHUDForView:viewController.view];
     }];
    [LCChatKitHelper lcck_pushToViewController:conversationViewController];
}


+ (void)saveLocalClientInfo:(NSString *)clientId {
    // 在系统偏好保存信息
    NSUserDefaults *defaultsSet = [NSUserDefaults standardUserDefaults];
    [defaultsSet setObject:clientId forKey:LCCK_KEY_USERID];
    [defaultsSet synchronize];
    NSString *subtitle = [NSString stringWithFormat:@"User Id 是 : %@", clientId];
    [LCCKUtil showNotificationWithTitle:@"登陆成功"
                               subtitle:subtitle
                                   type:LCCKMessageNotificationTypeSuccess];
}

@end
