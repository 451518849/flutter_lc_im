//
//  LCCKExampleConstants.h
//  ChatKit-OC
//
//  v0.8.5 Created by ElonChan on 16/8/13.
//  Copyright © 2016年 LeanCloud. All rights reserved.
//

#ifndef LCCKConstants_h
#define LCCKConstants_h

#pragma mark - 用以产生Demo中的联系人数据的宏定义
///=============================================================================
/// @name 用以产生Demo中的联系人数据的宏定义
///=============================================================================

#define LCCKProfileKeyPeerId        @"peerId"
#define LCCKProfileKeyName          @"username"
#define LCCKProfileKeyAvatarURL     @"avatarURL"
#define LCCKDeveloperPeerId @"571dae7375c4cd3379024b2f"


#define LCCKContactPeerIds \
    [LCCKContactProfiles valueForKeyPath:LCCKProfileKeyPeerId]

#define LCCKTestPeerIds \
    [LCCKTestPersonProfiles valueForKeyPath:LCCKProfileKeyPeerId]
#define __LCCKContactsOfDevelopers \
@[                                 \
    LCCKDeveloperPeerId,           \
]

#define __LCCKContactsOfSections \
@[                               \
    LCCKTestPeerIds,             \
    __LCCKContactsOfDevelopers,  \
]

#pragma mark - UI opera
///=============================================================================
/// @name UI opera
///=============================================================================

#define localize(key, default) LCCKLocalizedStrings(key)

#pragma mark - Message Bars

#define kStringMessageBarErrorTitle localize(@"message.bar.error.title")
#define kStringMessageBarErrorMessage localize(@"message.bar.error.message")
#define kStringMessageBarSuccessTitle localize(@"message.bar.success.title")
#define kStringMessageBarSuccessMessage localize(@"message.bar.success.message")
#define kStringMessageBarInfoTitle localize(@"message.bar.info.title")
#define kStringMessageBarInfoMessage localize(@"message.bar.info.message")

#pragma mark - Buttons

#define kStringButtonLabelSuccessMessage localize(@"button.label.success.message")
#define kStringButtonLabelErrorMessage localize(@"button.label.error.message")
#define kStringButtonLabelInfoMessage localize(@"button.label.info.message")
#define kStringButtonLabelHideAll localize(@"button.label.hide.all")

#pragma mark - Dict or UserDefaults Key
///=============================================================================
/// @name Dict or UserDefaults Key
///=============================================================================

static NSString *const LCCK_KEY_USERNAME = @"LCCK_KEY_USERNAME";
static NSString *const LCCK_KEY_USERID = @"LCCK_KEY_USERID";


#pragma mark - Other
///=============================================================================
/// @name Other
///=============================================================================

#define     LCCK_DEFAULT_AVATAR_PATH    @"lcck_conversation_placeholder_avatar"
#define     LCCKURL(urlString)    [NSURL URLWithString:urlString]
#define     LCCKNoNilString(str)  (str.length > 0 ? str : @"")
#define     LCCKTimeStamp(date)   ([NSString stringWithFormat:@"%lf", [date timeIntervalSince1970]])
#define     LCCKColor(r, g, b, a) [UIColor colorWithRed:(r)/255.0f green:(g)/255.0f blue:(b)/255.0f alpha:a]

#endif /* LCCKConstants_h */
