//
//  AVIMTypedMessage+Send.h
//  AVOSCloud
//
//  Created by 小发工作室 on 2020/3/11.
//

#import <Foundation/Foundation.h>
#import <AVOSCloudIM/AVOSCloudIM.h>
#import <Flutter/Flutter.h>
#import "XFMessage.h"


@interface AVIMTypedMessage (Send)

/**
 *消息转化成发送消息
 */
+ (AVIMTypedMessage *)messageWithXFMessage:(XFMessage *)message;

/**
 * 查询聊天记录
 */

+ (void)queryHistoryMessagesWithConversation:(AVIMConversation *)conversation
                                       limit:(int)limit
                                   messageId:(NSString *)messageId
                                   timestamp:(int64_t)sendTimestamp
                                    callback:(FlutterEventSink)callback;


/**
    转成flutter消息并回掉给flutter
 */
+ (void)convertMessagesToFlutterMessages:(NSArray<AVIMMessage *> *)messages
                                callback:(FlutterEventSink)callback;

@end

