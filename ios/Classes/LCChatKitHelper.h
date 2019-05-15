//
//  LCChatKitHelper.h
//  AVOSCloud
//
//  Created by 小发工作室 on 2019/5/14.
//

#import <Foundation/Foundation.h>
#if __has_include(<ChatKit/LCChatKit.h>)
#import <ChatKit/LCChatKit.h>
#else
#import "LCChatKit.h"
#endif

NS_ASSUME_NONNULL_BEGIN

@interface LCChatKitHelper : NSObject

+ (instancetype)sharedInstance;

/*!
 *  入口胶水函数：登入入口函数
 *
 *  用户登录时调用
 */
+ (void)invokeThisMethodAfterLoginSuccessWithClientId:(NSString *)clientId
                                               appUrl:(NSString *)url
                                              success:(LCCKVoidBlock)success
                                               failed:(LCCKErrorBlock)failed;

/*!
 *  入口胶水函数：登出入口函数
 *
 *  用户即将退出登录时调用
 */
+ (void)invokeThisMethodBeforeLogoutSuccess:(LCCKVoidBlock)success
                                     failed:(LCCKErrorBlock)failed;

/*!
 *  打开单聊页面
 */
+ (void)openConversationViewControllerWithPeerId:(NSString *)peerId;
@end

NS_ASSUME_NONNULL_END
