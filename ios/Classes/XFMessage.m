//
//  XFMessage.m
//  AVOSCloud
//
//  Created by 小发工作室 on 2020/3/11.
//

#import "XFMessage.h"

@implementation XFMessage

- (instancetype)initWithText:(NSString *)text
                   timestamp:(NSTimeInterval)timestamp
                   messageId:(NSString *)messageId
                  attributes:(NSDictionary *)attributes{
    self = [super init];
    if (self) {
        _text = text;
        _timestamp = timestamp;
        _messageId = messageId;
        _mediaType = kAVIMMessageMediaTypeText;
        _attributes = attributes;
    }
    return self;
}

- (instancetype)initWithPhoto:(UIImage *)photo
                    photoPath:(NSString *)photoPath
                    timestamp:(NSTimeInterval)timestamp
                   messageId :(NSString *)messageId
                   attributes:(NSDictionary *)attributes{
    self = [super init];
    if (self) {
        _photo     = photo;
        _photoPath = photoPath;
        _timestamp = timestamp;
        _messageId = messageId;
        _mediaType = kAVIMMessageMediaTypeImage;
        _attributes = attributes;
    }
    return self;
}



- (instancetype)initWithVoicePath:(NSString *)voicePath
                    voiceDuration:(NSString *)voiceDuration
                        timestamp:(NSTimeInterval)timestamp
                        messageId:(NSString *)messageId
                       attributes:(NSDictionary *)attributes {
    self = [super init];
    if (self) {
        _voicePath     = voicePath;
        _voiceDuration = voiceDuration;
        _timestamp     = timestamp;
        _messageId     = messageId;
        _mediaType     = kAVIMMessageMediaTypeAudio;
        _attributes = attributes;
    }
    return self;
}

- (instancetype)initWithVideoPath:(NSString *)videoPath
                    videoDuration:(NSString *)videoDuration
                        timestamp:(NSTimeInterval)timestamp
                        messageId:(NSString *)messageId
                       attributes:(NSDictionary *)attributes{
    self = [super init];
    if (self) {
        _videoPath     = videoPath;
        _videoDuration = videoDuration;
        _timestamp     = timestamp;
        _messageId     = messageId;
        _mediaType     = kAVIMMessageMediaTypeVideo;
        _attributes    = attributes;
    }
    return self;
}

- (instancetype)initWithLocalPositionPhoto:(UIImage *)localPositionPhoto
                              geolocations:(NSString *)geolocations
                                  location:(CLLocation *)location
                                 timestamp:(NSTimeInterval)timestamp
                                 messageId:(NSString *)messageId
                                attributes:(NSDictionary *)attributes{
    self = [super init];
    if (self) {
        _localPositionPhoto = localPositionPhoto;
        _geolocations       = geolocations;
        _location           = location;
        _timestamp          = timestamp;
        _messageId          = messageId;
        _mediaType          = kAVIMMessageMediaTypeLocation;
        _attributes         = attributes;
    }
    return self;
}

@end
