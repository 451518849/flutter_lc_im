//
//  AVIMConversation+Send.m
//  AVOSCloud
//
//  Created by 小发工作室 on 2020/3/12.
//

#import "AVIMConversation+Send.h"
#import "AVIMTypedMessage+Send.h"

@implementation AVIMConversation(Send)

- (void)sendMessage:(XFMessage *)message {
    
    AVIMTypedMessage *aviMessage = [AVIMTypedMessage messageWithXFMessage:message];
    AVIMMessageOption *option    = [[AVIMMessageOption alloc] init];
    option.pushData              = @{@"alert" : @"您有一条未读消息"};

    [self sendMessage:aviMessage option:option callback:^(BOOL succeeded, NSError *error) {
      if (succeeded) {
        NSLog(@"发送成功！");
          
      }
    }];
}

+ (void)findConversationsWithClient:(AVIMClient *)client limit:(int)limit offset:(int)offset callback:(FlutterEventSink) callback{
    AVIMConversationQuery *query = [client conversationQuery];
    query.limit       = limit;
    query.skip        = offset;
    query.option      = AVIMConversationQueryOptionWithMessage;
    query.cachePolicy = kAVCachePolicyNetworkElseCache;
    query.cacheMaxAge = 24 * 60 * 60; //缓存一天
    
    [query findConversationsWithCallback:^(NSArray<AVIMConversation *> * _Nullable conversations,
                                           NSError * _Nullable error) {
        
        [AVIMConversation convertConversastionsToFlutterConversations:conversations callback:callback];
    }];
}

+ (void)convertConversastionsToFlutterConversations:(NSArray<AVIMConversation *>*) conversations callback:(FlutterEventSink) callback{
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
    if(callback != nil){
        callback([array copy]);
    }
}
@end
