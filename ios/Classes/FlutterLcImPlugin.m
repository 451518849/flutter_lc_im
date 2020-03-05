
#import "FlutterLcImPlugin.h"
#import <AVOSCloud/AVOSCloud.h>
#import <AVOSCloudIM/AVOSCloudIM.h>

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
        isRegister       = true;

        [self registerConversationWithAppId:appId
                                     appKey:appKey
                                     apiUrl:url];

        }
        
        result(nil);
        
    }else if([@"login" isEqualToString:call.method]){
        
        NSString *clientId = call.arguments[@"client_id"];
        [self loginWithClientId:clientId];
        
    }else if([@"createConversation" isEqualToString:call.method]){

        NSString *clientId = call.arguments[@"client_id"];
        NSString *peerId   = call.arguments[@"peer_id"];
        
        [self createConversationWithClientId:clientId
                                      peerId:peerId];
        
    }else if([@"sendMessage" isEqualToString:call.method]){
        
        NSString *text                       = call.arguments[@"text"];
        int messageType                      = [call.arguments[@"messageType"] intValue];
        FlutterStandardTypedData* fileBuffer = (FlutterStandardTypedData*)call.arguments[@"file"];
        
        if ((NSNull *)fileBuffer ==  [NSNull null]) {
            [self sendMessage:text file:nil messageType:messageType];
        }else {
            [self sendMessage:text file:fileBuffer.data messageType:messageType];
        }

    }else if([@"queryHistoryConversationMessages" isEqualToString:call.method]){
        
        int  limit          = [call.arguments[@"limit"] intValue];
        NSString *messageId = call.arguments[@"message_id"];
        int64_t timestamp   = [call.arguments[@"timestamp"] integerValue];
        
        [self queryHistoryConversationMessages:limit
                                     messageId:messageId
                                     timestamp:timestamp];
        
    }else if([@"queryHistoryConversations" isEqualToString:call.method]){
        
        int limit  = [call.arguments[@"limit"] intValue];
        int offset = [call.arguments[@"offset"] intValue];
        
        [self findClientConversations:limit
                               offset:offset];
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
                               apiUrl:(NSString *)url{
    [AVOSCloud setAllLogsEnabled:YES];

    [AVOSCloud setApplicationId:appId
          clientKey:appKey
    serverURLString:url];

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
          NSLog(@"聊天功能建立成功！");
      }
    }];
}

/**
  建立单聊会话
 */
- (void)createConversationWithClientId:(NSString *)clientId peerId:(NSString *)peerId {
    __weak __typeof(self) weakSelf = self;
    
    [self.client createConversationWithName:[NSString stringWithFormat:@"%@&%@",clientId,peerId]
                             clientIds:@[peerId]
                                 attributes:@{@"type":@(LCCKConversationTypeSingle)}
                                    options:AVIMConversationOptionUnique
                              callback:^(AVIMConversation * _Nullable conversation, NSError * _Nullable error) {
        if (error == nil) {
            //会话建立成功
            NSLog(@"会话创建成功");
            weakSelf.conversation = conversation;
            [weakSelf queryHistoryConversationMessages:10 messageId:nil timestamp:0];
        }
    }];
}

-(void)findClientConversations:(int)limit offset:(int)offset {
    AVIMConversationQuery *query = [self.client conversationQuery];
    query.limit       = limit;
    query.skip        = offset;
    query.option      = AVIMConversationQueryOptionWithMessage;
    query.cacheMaxAge = kAVIMCachePolicyIgnoreCache;
    
    [query findConversationsWithCallback:^(NSArray<AVIMConversation *> * _Nullable conversations,
                                           NSError * _Nullable error) {
        
//        NSLog(@"消息列表:%@",conversations);
//        NSLog(@"error:%@",error);
        [self sendConversationsToFlutter:conversations];
    }];
}

/**
  发送消息
 */
- (void)sendMessage:(NSString *)text file:(NSData *)data messageType:(int)messageType{
    
    if (messageType == kAVIMMessageMediaTypeText) {
        [self sendMessage:text];
    }else if(messageType == kAVIMMessageMediaTypeImage){
        [self sendMessage:text image:data];
    }else if(messageType == kAVIMMessageMediaTypeAudio){
        [self sendMessage:text audio:data];
    }else if(messageType == kAVIMMessageMediaTypeVideo){
        [self sendMessage:text video:data];
    }
}

/**
  发送文本消息
 */
- (void)sendMessage:(NSString *)text{
    AVIMTextMessage *message = [AVIMTextMessage messageWithText:text attributes:nil];
    [self.conversation sendMessage:message callback:^(BOOL succeeded, NSError *error) {
      if (succeeded) {
        NSLog(@"发送成功！");
          
      }
    }];
}

/**
  发送图片消息
 */
- (void)sendMessage:(NSString *)text image:(NSData *)image{
    
    AVFile *file = [AVFile fileWithData:image];
    
    AVIMImageMessage *message = [AVIMImageMessage messageWithText:text file:file attributes:nil];
    
    [self.conversation sendMessage:message callback:^(BOOL succeeded, NSError *error) {
      if (succeeded) {
        NSLog(@"发送成功！");
          
      }
    }];

}

/**
  发送音频消息
 */
- (void)sendMessage:(NSString *)text audio:(NSData *)audio{
    
    AVFile *file = [AVFile fileWithData:audio];
    AVIMAudioMessage *message = [AVIMAudioMessage messageWithText:text file:file attributes:nil];
    
    [self.conversation sendMessage:message callback:^(BOOL succeeded, NSError *error) {
      if (succeeded) {
        NSLog(@"发送成功！");
          
      }
    }];
}

/**
  发送音频消息
 */
- (void)sendMessage:(NSString *)text video:(NSData *)video{
    
    AVFile *file = [AVFile fileWithData:video];
    AVIMVideoMessage *message = [AVIMVideoMessage messageWithText:text file:file attributes:nil];
    
    [self.conversation sendMessage:message callback:^(BOOL succeeded, NSError *error) {
      if (succeeded) {
        NSLog(@"发送成功！");
      }
    }];
}



/**
  查询聊天记录
 */
-(void)queryHistoryConversationMessages:(int)limit
                              messageId:(NSString *)messageId
                              timestamp:(int64_t)sendTimestamp{

    __weak __typeof(self) weakSelf = self;

    if (messageId == nil) {
        /**
         第一次查询会话记录
        */
        [self.conversation queryMessagesWithLimit:limit callback:^(NSArray<AVIMMessage *> *messages, NSError *error) {

            [weakSelf sendMessagesToFlutter:messages];
        }];
    }else {
        /**
        第二次或以上查询，需要根据上一次的查询结果进行查询
        */

        [self.conversation queryMessagesBeforeId:messageId
                                            timestamp:sendTimestamp
                                                limit:limit
                                             callback:^(NSArray<AVIMMessage *> *messagesInPage, NSError *error) {

             [weakSelf sendMessagesToFlutter:messagesInPage];
         }];
    }

}


-(void)sendConversationsToFlutter:(NSArray<AVIMConversation *> *)conversations {
    
    NSMutableArray *array = [[NSMutableArray alloc] init];
    
    for (AVIMConversation *conversation in conversations) {

        if(conversation.lastMessage == nil){
            continue;
        }
        NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
        [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
        NSString *lastMessageAt = [dateFormatter stringFromDate:conversation.lastMessageAt];
        NSDictionary *dic       = @{@"clientId":conversation.clientId,
                              @"conversationId":conversation.conversationId,
                              @"lastMessageAt":lastMessageAt,
                              @"members":conversation.members,
                              @"unreadMessagesCount":@(conversation.unreadMessagesCount),
                              @"lastMessage":conversation.lastMessage.content
        };
        [array addObject:dic];
    }
    if(conversationEventBlock != nil){
        conversationEventBlock([array copy]);
    }
}

-(void)sendMessagesToFlutter:(NSArray<AVIMMessage *> *)messages {
    
    NSMutableArray *array = [[NSMutableArray alloc] init];
    
    for (AVIMMessage *message in messages) {

//        [NSDate dateWithTimeIntervalSince1970:(message.sendTimestamp / 1000.0)]
        NSDictionary *dic = @{@"messageId":message.messageId,
                              @"clientId":message.clientId,
                              @"conversationId":message.conversationId,
                              @"content":message.content,
                              @"timestamp":[NSNumber numberWithLong:message.sendTimestamp],
                              @"mediaType":@(message.mediaType),
                              @"ioType":@(message.ioType),
        };
        [array addObject:dic];
    }
    if(messageEventBlock != nil){
        messageEventBlock([array copy]);
    }
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
        [self sendMessagesToFlutter:@[message]];
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
        [self sendMessagesToFlutter:@[message]];
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
