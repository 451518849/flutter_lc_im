#import "LCChatKit.h"
#import "LCCKUtil.h"
#import "LCChatKitHelper+Setting.h"
#import "FlutterLcImPlugin.h"
#import <UserNotifications/UserNotifications.h>
#import "LCCKUser.h"
#import "NSDate+Extension.h"
#import "AVIMConversation+LCCKExtension.h"

static BOOL isRegister = false;
static NSObject<FlutterBinaryMessenger>* messager = nil;
FlutterEventSink conversationEventBlock;
FlutterEventSink notificationEventBlock;

@interface FlutterLcImPlugin()<FlutterStreamHandler>

@end

@implementation FlutterLcImPlugin

+(void)sendNotification:(id)msg{
    if(notificationEventBlock != nil){
        if([msg objectForKey:@"data"] != nil){
            notificationEventBlock([msg objectForKey:@"data"]);
        }
        
    }
}
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    FlutterMethodChannel* channel = [FlutterMethodChannel
                                     methodChannelWithName:@"flutter_lc_im"
                                     binaryMessenger:[registrar messenger]];
    messager = [registrar messenger];
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
            NSString *url   = call.arguments[@"api"];
            
            [self registerConversationWithAppId:appId
                                         appKey:appKey
                                         apiUrl:url];
            isRegister = true;
        }
        
        result(nil);
    }else if([@"login" isEqualToString:call.method]){
        NSString *userId = call.arguments[@"user_id"];
        [self loginImWithUserId:userId result:result];
    }
    else if ([@"pushToConversationView" isEqualToString:call.method]) {
        [self chatWithUser:call.arguments[@"user"]
                      peer:call.arguments[@"peer"]];
        result(nil);
    }
    else if ([@"getRecentConversationUsers" isEqualToString:call.method]) {
        [self getRecentConversationUsers:result];
    }
    else {
        result(FlutterMethodNotImplemented);
    }
}

- (void)registerConversationWithAppId:(NSString *)appId
                               appKey:(NSString *)appKey
                               apiUrl:(NSString *)url{
    
    [LCChatKit setAppId:appId appKey:appKey];
    // 启用未读消息
    [AVIMClient setUnreadNotificationEnabled:true];
    [AVIMClient setTimeoutIntervalInSeconds:20];
    //    //添加输入框底部插件，如需更换图标标题，可子类化，然后调用 `+registerSubclass`
    [LCCKInputViewPluginTakePhoto registerSubclass];
    [LCCKInputViewPluginPickImage registerSubclass];
    [LCCKInputViewPluginLocation registerSubclass];
    
    // 配置 SDK 储存
    [AVOSCloud setServerURLString:url forServiceModule:AVServiceModuleAPI];
    // 配置 SDK 推送
    [AVOSCloud setServerURLString:url forServiceModule:AVServiceModulePush];
    // 配置 SDK 云引擎
    [AVOSCloud setServerURLString:url forServiceModule:AVServiceModuleEngine];
    // 配置 SDK 即时通讯
    [AVOSCloud setServerURLString:url forServiceModule:AVServiceModuleRTM];
    
    [self setFlutterChannel];
}

/**
 * Lean Cloud 获取最近联系的策略：当用户发起聊天时，lc会缓存聊天对象，并在服务器上为聊天对象设置一个有效时间，
 * 每次获取联系人时，如果联系人还在有效时间内，则从服务器上获取联系人列表，如果联系人已经长时间没有和当前用户聊天则失效，则从本地缓存中获取数据，
 * 所有聊天对象都会缓存到本地的FMDatabase中。因此，换设备后，只能获取服务器上还没有过期的聊天对象。
 * 因为是单聊，不是组聊，只要获取到用户的user_id即可，不需要其他数据。
 */
- (void)getRecentConversationUsers:(FlutterResult)result{
    
    [self reloadMessage:result];
}

- (void)reloadMessage:(FlutterResult)result {
    [[LCChatKitHelper sharedInstance] lcck_settingWithUsers:@[]];
    NSMutableArray *messages = [NSMutableArray array];
    __block NSUInteger badgeCount = 0;
    
    [[LCCKConversationListService sharedInstance] findRecentConversationsWithBlock:^(NSArray *conversations, NSInteger totalUnreadCount, NSError *error) {
        NSLog(@"totalUnreadCount :%ld",totalUnreadCount);
        
        [conversations enumerateObjectsUsingBlock:^(AVIMConversation *obj, NSUInteger idx, BOOL * _Nonnull stop) {
            NSString *peerId = @"";
            if (obj.members.count == 2) {
                if (obj.members[0] == obj.clientId){
                    peerId = obj.members[1];
                }else {
                    peerId = obj.members[0];
                }
                
                NSString *text = @"";
                if (obj.lastMessage.content == nil) {
                    if (obj.lcck_lastMessage.mediaType == kAVIMMessageMediaTypeText) {
                        text = obj.lcck_lastMessage.text;
                    } else if(obj.lcck_lastMessage.mediaType == kAVIMMessageMediaTypeImage){
                        text = @"[图片]";
                    }else if(obj.lcck_lastMessage.mediaType == kAVIMMessageMediaTypeAudio){
                        text = @"[语音]";
                    }else if(obj.lcck_lastMessage.mediaType == kAVIMMessageMediaTypeVideo){
                        text = @"[视频]";
                    }else if(obj.lcck_lastMessage.mediaType == kAVIMMessageMediaTypeLocation){
                        text = @"[位置]";
                    }else if(obj.lcck_lastMessage.mediaType == kAVIMMessageMediaTypeFile){
                        text = @"[文件]";
                    }else {
                        text =@"[暂不支持格式]";
                    }
                    NSDictionary *message = @{
                                              @"clientId":obj.clientId,
                                              @"peerId":peerId,
                                              @"unreadMessagesCount":@(0),
                                              @"lastMessageAt":[NSDate timeInfoWithDate:obj.lcck_lastMessageAt],
                                              @"peerName":obj.lcck_displayName,
                                              @"lastMessageContent":text,
                                              };
                    [messages addObject:message];
                    return;
                } else {
                    NSData *jsonData = [obj.lastMessage.content dataUsingEncoding:NSUTF8StringEncoding];
                    NSDictionary *content = [NSJSONSerialization JSONObjectWithData:jsonData
                                                                            options:NSJSONReadingMutableContainers
                                                                              error:nil];
                    NSLog(@"content:%@",content);
                    /**
                     *
                     kAVIMMessageMediaTypeNone = 0,
                     kAVIMMessageMediaTypeText = -1,
                     kAVIMMessageMediaTypeImage = -2,
                     kAVIMMessageMediaTypeAudio = -3,
                     kAVIMMessageMediaTypeVideo = -4,
                     kAVIMMessageMediaTypeLocation = -5,
                     kAVIMMessageMediaTypeFile = -6,
                     kAVIMMessageMediaTypeRecalled = -127
                     */
                    
                    if ([content[@"_lctype"] isEqual:@(-1)]) {
                        text = content[@"_lctext"];
                    } else if([content[@"_lctype"] isEqual:@(-2)]){
                        text = @"[图片]";
                    } else if([content[@"_lctype"] isEqual:@(-3)]){
                        text = @"[语音]";
                    } else if([content[@"_lctype"] isEqual:@(-4)]){
                        text = @"[视频]";
                    } else if([content[@"_lctype"] isEqual:@(-5)]){
                        text = @"[位置]";
                    }else if([content[@"_lctype"] isEqual:@(-6)]){
                        text = @"[文件]";
                    }else {
                        text =@"[暂不支持格式]";
                    }
                    
                    badgeCount += obj.unreadMessagesCount;
                    
                    NSDictionary *message = @{
                                              @"clientId":obj.clientId,
                                              @"peerId":peerId,
                                              @"unreadMessagesCount":@(obj.unreadMessagesCount),
                                              @"lastMessageAt":[NSDate timeInfoWithDate:obj.lastMessageAt],
                                              @"lastMessageContent":text,
                                              };
                    [messages addObject:message];
                    
                }
                
            }
        }];
        NSLog(@"recent conversation users:%@",messages);
        NSLog(@"badgeCount :%ld",badgeCount);
        [[UIApplication sharedApplication] setApplicationIconBadgeNumber:badgeCount];
        result(messages);
    }];
    
}

- (void)pushMessageToFlutter{
    [[LCChatKitHelper sharedInstance] lcck_settingWithUsers:@[]];
    NSMutableArray *messages = [NSMutableArray array];
    __block NSUInteger badgeCount = 0;
    [[LCCKConversationListService sharedInstance] findRecentConversationsWithBlock:^(NSArray *conversations, NSInteger totalUnreadCount, NSError *error) {
        
        NSLog(@"totalUnreadCount :%ld",totalUnreadCount);
        
        [conversations enumerateObjectsUsingBlock:^(AVIMConversation *obj, NSUInteger idx, BOOL * _Nonnull stop) {
            NSString *peerId = @"";
            if (obj.members.count == 2) {
                if (obj.members[0] == obj.clientId){
                    peerId = obj.members[1];
                }else {
                    peerId = obj.members[0];
                }
                
                NSLog(@"obj.members:%@",obj.members);
                NSLog(@"conversation name :%@",obj.name);
                NSLog(@"conversation attributes :%@",obj.attributes);
                
                NSString *text = @"";
                if (obj.lastMessage.content == nil) {
                    if (obj.lcck_lastMessage.mediaType == kAVIMMessageMediaTypeText) {
                        text = obj.lcck_lastMessage.text;
                    } else if(obj.lcck_lastMessage.mediaType == kAVIMMessageMediaTypeImage){
                        text = @"[图片]";
                    }else if(obj.lcck_lastMessage.mediaType == kAVIMMessageMediaTypeAudio){
                        text = @"[语音]";
                    }else if(obj.lcck_lastMessage.mediaType == kAVIMMessageMediaTypeVideo){
                        text = @"[视频]";
                    }else if(obj.lcck_lastMessage.mediaType == kAVIMMessageMediaTypeLocation){
                        text = @"[位置]";
                    }else if(obj.lcck_lastMessage.mediaType == kAVIMMessageMediaTypeFile){
                        text = @"[文件]";
                    }else {
                        text =@"[暂不支持格式]";
                    }
                    
                    NSDictionary *message = @{
                                              @"clientId":obj.clientId,
                                              @"peerId":peerId,
                                              @"unreadMessagesCount":@(0),
                                              @"lastMessageAt":[NSDate timeInfoWithDate:obj.lcck_lastMessageAt],
                                              @"peerName":obj.lcck_displayName,
                                              @"lastMessageContent":text,
                                              };
                    [messages addObject:message];
                    return;
                } else {
                    NSData *jsonData = [obj.lastMessage.content dataUsingEncoding:NSUTF8StringEncoding];
                    NSDictionary *content = [NSJSONSerialization JSONObjectWithData:jsonData
                                                                            options:NSJSONReadingMutableContainers
                                                                              error:nil];
                    NSLog(@"content:%@",content);
                    /**
                     *
                     kAVIMMessageMediaTypeNone = 0,
                     kAVIMMessageMediaTypeText = -1,
                     kAVIMMessageMediaTypeImage = -2,
                     kAVIMMessageMediaTypeAudio = -3,
                     kAVIMMessageMediaTypeVideo = -4,
                     kAVIMMessageMediaTypeLocation = -5,
                     kAVIMMessageMediaTypeFile = -6,
                     kAVIMMessageMediaTypeRecalled = -127
                     */
                    if ([content[@"_lctype"] isEqual:@(0)]) {
                        return;
                    } else if ([content[@"_lctype"] isEqual:@(-1)]) {
                        text = content[@"_lctext"];
                    } else if([content[@"_lctype"] isEqual:@(-2)]){
                        text = @"[图片]";
                    } else if([content[@"_lctype"] isEqual:@(-3)]){
                        text = @"[语音]";
                    } else if([content[@"_lctype"] isEqual:@(-4)]){
                        text = @"[视频]";
                    } else if([content[@"_lctype"] isEqual:@(-5)]){
                        text = @"[位置]";
                    }else if([content[@"_lctype"] isEqual:@(-6)]){
                        text = @"[文件]";
                    }else {
                        text =@"[暂不支持格式]";
                    }
                    NSLog(@"obj.unreadMessagesCount:%ld",obj.unreadMessagesCount);
                    badgeCount += obj.unreadMessagesCount;
                    NSDictionary *message = @{
                                              @"clientId":obj.clientId,
                                              @"peerId":peerId,
                                              @"unreadMessagesCount":@(obj.unreadMessagesCount),
                                              @"lastMessageAt":[NSDate timeInfoWithDate:obj.lastMessageAt],
                                              @"lastMessageContent":text,
                                              };
                    [messages addObject:message];
                    
                }
                
            }
        }];
        if (conversationEventBlock != nil) {
            NSLog(@"badgeCount :%ld",badgeCount);
            [[UIApplication sharedApplication] setApplicationIconBadgeNumber:badgeCount];
            conversationEventBlock(messages);
        }
    }];
    
}

- (void)chatWithUser:(NSDictionary *)userDic peer:(NSDictionary *)peerDic{
    
    LCCKUser *user = [[LCCKUser alloc] initWithUserId:userDic[@"user_id"] name:userDic[@"name"] avatarURL:userDic[@"avatar_url"]];
    LCCKUser *peer = [[LCCKUser alloc] initWithUserId:peerDic[@"user_id"] name:peerDic[@"name"] avatarURL:peerDic[@"avatar_url"]];
    
    NSMutableArray *users = [NSMutableArray arrayWithCapacity:2];
    [users addObject:user];
    [users addObject:peer];
    
    [[LCChatKitHelper sharedInstance] lcck_settingWithUsers:users];
    [LCChatKitHelper openConversationViewControllerWithPeerId:peer.userId];
    
}

- (void)loginImWithUserId:(NSString *)userId result:(FlutterResult)result{
    
    [LCCKUtil showProgressText:@"连接中..." duration:10.0f];
    [LCChatKitHelper invokeThisMethodAfterLoginSuccessWithClientId:userId success:^{
        NSLog(@"login success@");
        [LCCKUtil hideProgress];
        result(nil);
    } failed:^(NSError *error) {
        [LCCKUtil hideProgress];
        NSLog(@"login error");
        [LCCKUtil hideProgress];
        result(@"login error");
    }];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(pushMessageToFlutter) name:LCCKNotificationMessageReceived object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(pushMessageToFlutter) name:LCCKNotificationMessageUpdated object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(pushMessageToFlutter) name:LCCKNotificationUnreadsUpdated object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(pushMessageToFlutter) name:LCCKNotificationConversationListDataSourceUpdated object:nil];
}

-(void)setFlutterChannel{
    [self setConversationEventToFlutter];
    [self setNotificationEventToFlutter];
    
}
- (void)setConversationEventToFlutter {
    
    NSString *channelName = @"flutter_lc_im/conversation";
    FlutterEventChannel *evenChannal = [FlutterEventChannel eventChannelWithName:channelName binaryMessenger:messager];
    // 代理FlutterStreamHandler
    [evenChannal setStreamHandler:self];
    
    NSLog(@"print log=========================");
}

- (void)setNotificationEventToFlutter {
    
    NSString *channelName = @"flutter_lc_im/notification";
    FlutterEventChannel *evenChannal = [FlutterEventChannel eventChannelWithName:channelName binaryMessenger:messager];
    // 代理FlutterStreamHandler
    [evenChannal setStreamHandler:self];
    
    NSLog(@"print log=========================");
}

- (FlutterError* _Nullable)onListenWithArguments:(id _Nullable)arguments
                                       eventSink:(FlutterEventSink)events{
    if([arguments isEqual:@"flutter_lc_im/conversation"]){
        if (events) {
            conversationEventBlock = events;
        }
    }else if ([arguments isEqual:@"flutter_lc_im/notification"]){
        if (events) {
            notificationEventBlock = events;
        }
        
    }
    return nil;
}

- (FlutterError* _Nullable)onCancelWithArguments:(id _Nullable)arguments{
    return nil;
}


@end
