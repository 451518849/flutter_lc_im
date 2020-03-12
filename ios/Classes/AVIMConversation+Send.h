//
//  AVIMConversation+Send.h
//  AVOSCloud
//
//  Created by 小发工作室 on 2020/3/12.
//

#import <Foundation/Foundation.h>
#import <AVOSCloudIM/AVOSCloudIM.h>
#import "XFMessage.h"
#import <Flutter/Flutter.h>



@interface AVIMConversation(Send)

/**
  发送消息
 */
- (void)sendMessage:(XFMessage *)message;

/**
  查询历史消息
 */
- (void)findConversationsWithClient:(AVIMClient *)client
                              limit:(int)limit
                             offset:(int)offset
                           callback:(FlutterEventSink) callback;

@end

