
#import "FlutterLcImPlugin.h"
#import <AVOSCloud/AVOSCloud.h>
#import <AVOSCloudIM/AVOSCloudIM.h>
#import "XFMessage.h"
#import "AVIMConversation+Send.h"
#import "AVIMTypedMessage+Send.h"

static BOOL isRegister                            = false;
static NSObject<FlutterBinaryMessenger>* messager = nil;

NSString *FLUTTER_IM_NAME                         = @"flutter_lc_im";
NSString *FLUTTER_CHANNEL_MESSAGE                 = @"flutter_lc_im/messages";
NSString *FLUTTER_CHANNEL_CONVERSATION            = @"flutter_lc_im/conversations";
NSString *FLUTTER_CHANNEL_NOTIFICATION            = @"flutter_lc_im/notifications";

FlutterEventSink conversationEventBlock;
FlutterEventSink messageEventBlock;
FlutterEventSink notificationEventBlock;

typedef NS_ENUM(NSUInteger, LCCKConversationType){
    LCCKConversationTypeSingle = 0/**< 单人聊天,不显示nickname */,
    LCCKConversationTypeGroup /**< 群组聊天,显示nickname */,
};

@interface FlutterLcImPlugin()<FlutterStreamHandler,AVIMClientDelegate>

@property (nonatomic,strong) AVIMClient       *client;
@property (nonatomic,strong) AVIMConversation *conversation;

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
                                     methodChannelWithName:FLUTTER_IM_NAME
                                     binaryMessenger:[registrar messenger]];
    messager                      = [registrar messenger];
    FlutterLcImPlugin* instance   = [[FlutterLcImPlugin alloc] init];
    
    [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    
    if ([@"register" isEqualToString:call.method]){
        
        if (!isRegister) {

        NSString *appId  = call.arguments[@"app_id"];
        NSString *appKey = call.arguments[@"app_key"];
        NSString *url    = call.arguments[@"api"];
        BOOL debug    = [call.arguments[@"debug"] boolValue];

        isRegister       = true;

        [self registerConversationWithAppId:appId
                                     appKey:appKey
                                     apiUrl:url debug:debug];

        }
        
        result(nil);
        
    }else if([@"login" isEqualToString:call.method]){
        
        NSString *clientId = call.arguments[@"client_id"];
        [self loginWithClientId:clientId];
        
    }else if([@"createConversation" isEqualToString:call.method]){

        NSString *peerId   = call.arguments[@"peer_id"];
        int  limit          = [call.arguments[@"limit"] intValue];

        [self createConversationWithPeerId:peerId limit:limit];
        
    }else if([@"sendTextMessage" isEqualToString:call.method]){
        
        NSString *text                       = call.arguments[@"text"];
        NSDictionary *attributes = call.arguments[@"attributes"];

        XFMessage *message = [[XFMessage alloc] initWithText:text
                                                   timestamp:0
                                                   messageId:nil
                                                  attributes:attributes];
        [self.conversation sendMessage:message];

    }else if([@"sendImageMessage" isEqualToString:call.method]){
        
        NSString *path                       = call.arguments[@"path"];
        NSDictionary *attributes = call.arguments[@"attributes"];

        XFMessage *message = [[XFMessage alloc] initWithPhoto:nil
                                                    photoPath:path
                                                    timestamp:0
                                                    messageId:nil
                                                   attributes:attributes];
        [self.conversation sendMessage:message];

    }else if([@"sendVoiceMessage" isEqualToString:call.method]){
        
        NSString *path           = call.arguments[@"path"];
        NSString *duration       = call.arguments[@"duration"];
        NSDictionary *attributes = call.arguments[@"attributes"];

        XFMessage *message = [[XFMessage alloc] initWithVoicePath:path
                                                    voiceDuration:duration
                                                        timestamp:0
                                                        messageId:nil
                                                       attributes:attributes];

        [self.conversation sendMessage:message];

    }else if([@"sendVideoMessage" isEqualToString:call.method]){
        
        NSString *path           = call.arguments[@"path"];
        NSDictionary *attributes = call.arguments[@"attributes"];
        NSString *duration       = call.arguments[@"duration"];

        XFMessage *message = [[XFMessage alloc] initWithVideoPath:path
                                                    videoDuration:duration
                                                        timestamp:0
                                                        messageId:nil
                                                       attributes:attributes];
        
        [self.conversation sendMessage:message];


    }else if([@"queryHistoryConversationMessages" isEqualToString:call.method]){
        
        int  limit          = [call.arguments[@"limit"] intValue];
        NSString *messageId = call.arguments[@"message_id"];
        int64_t timestamp   = [call.arguments[@"timestamp"] integerValue];
        
        [AVIMTypedMessage queryHistoryMessagesWithConversation:self.conversation
                                                         limit:limit messageId:messageId
                                                     timestamp:timestamp
                                                      callback:messageEventBlock];
        
    }else if([@"queryHistoryConversations" isEqualToString:call.method]){
        
        int limit  = [call.arguments[@"limit"] intValue];
        int offset = [call.arguments[@"offset"] intValue];
        
        [AVIMConversation findConversationsWithClient:self.client
                                                limit:limit
                                               offset:offset
                                             callback:conversationEventBlock];

    }
    else {
        result(FlutterMethodNotImplemented);
    }
}

# pragma mark custom method
/**
 注册
 */
- (void)registerConversationWithAppId:(NSString *)appId
                               appKey:(NSString *)appKey
                               apiUrl:(NSString *)url
                                debug:(BOOL) debug{
    [AVOSCloud setAllLogsEnabled:debug];

    [AVOSCloud setApplicationId:appId
          clientKey:appKey
    serverURLString:url];

    [AVIMClient setUnreadNotificationEnabled:YES];

    [self setFlutterChannels];
}

/**
 登陆im
 */
- (void)loginWithClientId:(NSString *)clientId{
    self.client          = [[AVIMClient alloc] initWithClientId:clientId];
    self.client.delegate = self;
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
      if(succeeded) {
          NSLog(@"ccc");
      }
    }];
}

/**
  建立单聊会话
 */
- (void)createConversationWithPeerId:(NSString *)peerId limit:(int)limit{
    __weak __typeof(self) weakSelf = self;
    
    [self.client createConversationWithName:[NSString stringWithFormat:@"%@&%@",self.client.clientId,peerId]
                             clientIds:@[peerId]
                                 attributes:@{@"type":@(LCCKConversationTypeSingle)}
                                    options:AVIMConversationOptionUnique
                              callback:^(AVIMConversation * _Nullable conversation, NSError * _Nullable error) {
        if (error == nil) {
            //会话建立成功
            NSLog(@"会话创建成功");
            weakSelf.conversation = conversation;
            [weakSelf.conversation readInBackground];
            [AVIMTypedMessage queryHistoryMessagesWithConversation:conversation
                                                             limit:limit
                                                         messageId:nil
                                                         timestamp:0
                                                          callback:messageEventBlock];
            
        }
    }];
}



#pragma mark delegate

/*!
 当前用户被邀请加入对话的通知。
 @param conversation － 所属对话
 @param clientId - 邀请者的 id
 */
- (void)conversation:(AVIMConversation *)conversation invitedByClientId:(NSString * _Nullable)clientId{
    self.conversation = conversation;
}
/*!
 接收到新的普通消息。
 @param conversation － 所属对话
 @param message - 具体的消息
 */
- (void)conversation:(AVIMConversation *)conversation didReceiveCommonMessage:(AVIMMessage *)message{
    
    NSLog(@"接收到消息：%@",message);
    self.conversation = conversation;
    if (message != nil) {
        [AVIMTypedMessage convertMessagesToFlutterMessages:@[message] callback:messageEventBlock];
    }
}

/*!
 接收到新的富媒体消息。
 @param conversation － 所属对话
 @param message - 具体的消息
 */
- (void)conversation:(AVIMConversation *)conversation didReceiveTypedMessage:(AVIMTypedMessage *)message{
    self.conversation = conversation;
    NSLog(@"接收到消息：%@",message);
    self.conversation = conversation;
    if (message != nil) {
        [AVIMTypedMessage convertMessagesToFlutterMessages:@[message] callback:messageEventBlock];
    }
}


- (void)imClientClosed:(nonnull AVIMClient *)imClient error:(NSError * _Nullable)error {
    
}


- (void)imClientPaused:(nonnull AVIMClient *)imClient {
    
}


- (void)imClientResumed:(nonnull AVIMClient *)imClient {
    
}


- (void)imClientResuming:(nonnull AVIMClient *)imClient {
    
}


# pragma mark flutter channel


-(void)setFlutterChannels{
    [self setConversationEventToFlutter];
    [self setNotificationEventToFlutter];
    [self setMessageEventToFlutter];

}
- (void)setConversationEventToFlutter {
    
    FlutterEventChannel *evenChannal = [FlutterEventChannel eventChannelWithName:FLUTTER_CHANNEL_CONVERSATION
                                                                 binaryMessenger:messager];
    [evenChannal setStreamHandler:self];
    
}

- (void)setMessageEventToFlutter {
    
    FlutterEventChannel *evenChannal = [FlutterEventChannel eventChannelWithName:FLUTTER_CHANNEL_MESSAGE
                                                                 binaryMessenger:messager];
    [evenChannal setStreamHandler:self];
    
}

- (void)setNotificationEventToFlutter {
    
    FlutterEventChannel *evenChannal = [FlutterEventChannel eventChannelWithName:FLUTTER_CHANNEL_NOTIFICATION
                                                                 binaryMessenger:messager];
    [evenChannal setStreamHandler:self];
    
}

- (FlutterError* _Nullable)onListenWithArguments:(id _Nullable)arguments
                                       eventSink:(FlutterEventSink)events{
    NSLog(@"listen channel:%@",arguments);
    if([arguments isEqual:FLUTTER_CHANNEL_CONVERSATION]){
        if (events) {
            conversationEventBlock = events;
        }
    }else if ([arguments isEqual:FLUTTER_CHANNEL_NOTIFICATION]){
        if (events) {
            notificationEventBlock = events;
        }

    }else if ([arguments isEqual:FLUTTER_CHANNEL_MESSAGE]){
        if (events) {
            messageEventBlock      = events;
        }
        
    }
    return nil;
}

- (FlutterError* _Nullable)onCancelWithArguments:(id _Nullable)arguments{
    return nil;
}


@end
