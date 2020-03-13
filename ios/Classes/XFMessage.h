//
//  XFMessage.h
//  AVOSCloud
//
//  Created by 小发工作室 on 2020/3/11.
//

#import <Foundation/Foundation.h>
#import <AVOSCloudIM/AVOSCloudIM.h>
#import <CoreLocation/CoreLocation.h>

@interface XFMessage : NSObject

//文本信息
@property (nonatomic, copy, readonly   ) NSString             *text;

//图片信息
@property (nonatomic, strong, readwrite) UIImage              *photo;
@property (nonatomic, copy, readonly   ) NSString             *photoPath;

//视频信息
@property (nonatomic, strong, readonly ) UIImage              *videoConverPhoto;
@property (nonatomic, copy, readonly   ) NSString             *videoPath;

// 语音信息
@property (nonatomic, copy, readonly   ) NSString             *voicePath;
@property (nonatomic, copy, readonly   ) NSString             *voiceDuration;

//地理信息
@property (nonatomic, strong, readonly ) UIImage              *localPositionPhoto;
@property (nonatomic, copy, readonly   ) NSString             *geolocations;
@property (nonatomic, strong, readonly ) CLLocation           *location;

//公共信息
@property (nonatomic, assign, readonly ) AVIMMessageMediaType mediaType;
@property (nonatomic, copy, readwrite  ) NSString             *messageId;
@property (nonatomic, assign           ) NSTimeInterval       timestamp;
@property (nonatomic, strong, nullable ) NSDictionary         *attributes;// 自定义属性


- (instancetype)initWithText:(NSString *)text
                   timestamp:(NSTimeInterval)timestamp
                   messageId:(NSString *)messageId
                  attributes:(NSDictionary *)attributes;

/**
 *  初始化图片类型的消息
 *
 *  @param photo          目标图片
 *  @param photoPath 目标图片本地路径
 *  @param timestamp           发送时间
 *
 *  @return 返回Message model 对象
 */
- (instancetype)initWithPhoto:(UIImage *)photo
                    photoPath:(NSString *)photoPath
                    timestamp:(NSTimeInterval)timestamp
                    messageId:(NSString *)messageId
                   attributes:(NSDictionary *)attributes;

/**
 *  初始化视频类型的消息
 *
 *  @param videoPath        目标视频的本地路径，如果是下载过，或者是从本地发送的时候，会存在
 *  @param timestamp             发送时间
 *
 *  @return 返回Message model 对象
 */
- (instancetype)initWithVideoPath:(NSString *)videoPath
                        timestamp:(NSTimeInterval)timestamp
                        messageId:(NSString *)messageId
                       attributes:(NSDictionary *)attributes;

/**
 *  初始化语音类型的消息
 *
 *  @param voicePath        目标语音的本地路径
 *  @param voiceDuration    目标语音的时长
 *  @param timestamp             发送时间
 *
 *  @return 返回Message model 对象
 */
- (instancetype)initWithVoicePath:(NSString *)voicePath
                    voiceDuration:(NSString *)voiceDuration
                        timestamp:(NSTimeInterval)timestamp
                        messageId:(NSString *)messageId
                       attributes:(NSDictionary *)attributes;



- (instancetype)initWithLocalPositionPhoto:(UIImage *)localPositionPhoto
                              geolocations:(NSString *)geolocations
                                  location:(CLLocation *)location
                                 timestamp:(NSTimeInterval)timestamp
                                 messageId:(NSString *)messageId
                                attributes:(NSDictionary *)attributes;


@end
