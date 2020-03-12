//
//  AVIMTypedMessage+Send.m
//  AVOSCloud
//
//  Created by 小发工作室 on 2020/3/11.
//

#import "AVIMTypedMessage+Send.h"
#import "XFMessage.h"

@implementation AVIMTypedMessage (Send)

+ (AVIMTypedMessage *)messageWithXFMessage:(XFMessage *)message {
    AVIMTypedMessage *avimTypedMessage;
    switch (message.mediaType) {
        case kAVIMMessageMediaTypeText: {
            avimTypedMessage = [AVIMTextMessage messageWithText:message.text attributes:message.attributes];
            break;
        }
        case kAVIMMessageMediaTypeVideo:
            avimTypedMessage = [AVIMVideoMessage messageWithText:nil
                                                attachedFilePath:message.voicePath
                                                      attributes:message.attributes];
            break;
        case kAVIMMessageMediaTypeImage: {
            avimTypedMessage = [AVIMImageMessage messageWithText:nil
                                                attachedFilePath:message.photoPath
                                                      attributes:message.attributes];

            break;
        }
        case kAVIMMessageMediaTypeAudio: {
            avimTypedMessage = [AVIMAudioMessage messageWithText:nil
                                                attachedFilePath:message.voicePath
                                                      attributes:message.attributes];
            NSMutableDictionary *metaData = [NSMutableDictionary dictionary];
            if (message.voiceDuration.length != 0) {
                [metaData setValue:message.voiceDuration forKey:@"duration"];
            }
            [avimTypedMessage.file setMetaData:metaData];
            break;
        }
        case kAVIMMessageMediaTypeLocation: {
            avimTypedMessage = [AVIMLocationMessage messageWithText:message.geolocations
                                                           latitude:message.location.coordinate.latitude
                                                          longitude:message.location.coordinate.longitude
                                                         attributes:message.attributes];
            break;
        case kAVIMMessageMediaTypeNone:
            //TODO:
            break;
        }
        case kAVIMMessageMediaTypeFile:
            //TODO
            break;
        case kAVIMMessageMediaTypeRecalled:
            //TODO
            break;
    }
    return avimTypedMessage;
}


+ (void)queryHistoryMessagesWithConversation:(AVIMConversation *)conversation
                                       limit:(int)limit
                                   messageId:(NSString *)messageId
                                   timestamp:(int64_t)sendTimestamp
                                    callback:(FlutterEventSink)callback{


    if (messageId == nil) {
        /**
         第一次查询会话记录
        */
        [conversation queryMessagesWithLimit:limit callback:^(NSArray<AVIMMessage *> *messages, NSError *error) {

            [AVIMTypedMessage convertMessagesToFlutterMessages:messages callback:callback];
        }];
    }else {
        /**
        第二次或以上查询，需要根据上一次的查询结果进行查询
        */

        [conversation queryMessagesBeforeId:messageId
                                            timestamp:sendTimestamp
                                                limit:limit
                                             callback:^(NSArray<AVIMMessage *> *messagesInPage, NSError *error) {

             [AVIMTypedMessage convertMessagesToFlutterMessages:messagesInPage callback:callback];
         }];
    }

}



+ (void)convertMessagesToFlutterMessages:(NSArray<AVIMMessage *> *)messages callback:(FlutterEventSink)callback{
    
    NSMutableArray *array = [[NSMutableArray alloc] init];
    
    for (AVIMMessage *message in messages) {
        
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
    if(callback != nil){
        callback([array copy]);
    }
}

@end
