//
//  LCChatKitHelper+Setting.h
//  AVOSCloud
//
//  Created by 小发工作室 on 2019/5/14.
//

#import "LCChatKitHelper.h"

NS_ASSUME_NONNULL_BEGIN

@interface LCChatKitHelper (Setting)

/**
 *  初始化需要的设置
 */
- (void)lcck_settingWithAppUrl:(NSString *)url;
- (void)lcck_settingWithUsers:(NSArray<LCCKUser *> *)users;

+ (void)lcck_pushToViewController:(UIViewController *)viewController;
+ (void)lcck_tryPresentViewControllerViewController:(UIViewController *)viewController;
+ (void)lcck_clearLocalClientInfo;
//+ (void)lcck_changeGroupAvatarURLsForConversationId:(NSString *)conversationId
//                                              shouldInsert:(BOOL)shouldInsert;

@end

NS_ASSUME_NONNULL_END
