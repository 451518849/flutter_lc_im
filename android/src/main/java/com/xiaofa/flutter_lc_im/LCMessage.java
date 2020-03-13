package com.xiaofa.flutter_lc_im;

import java.io.IOException;
import java.util.Map;

import cn.leancloud.im.v2.AVIMTypedMessage;
import cn.leancloud.im.v2.annotation.AVIMMessageType;
import cn.leancloud.im.v2.messages.AVIMAudioMessage;
import cn.leancloud.im.v2.messages.AVIMImageMessage;
import cn.leancloud.im.v2.messages.AVIMTextMessage;
import cn.leancloud.im.v2.messages.AVIMVideoMessage;

public class LCMessage {
    public String text;
    public String photoPath;
    public String videoPath;
    public String videoDuration;
    public String voicePath;
    public String voiceDuration;
    public Map attributes;
    public int messageType;

    public LCMessage(){
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public void setVideoDuration(String videoDuration) {
        this.videoDuration = videoDuration;
    }

    public void setVoicePath(String voicePath) {
        this.voicePath = voicePath;
    }

    public void setVoiceDuration(String voiceDuration) {
        this.voiceDuration = voiceDuration;
    }

    public void setAttributes(Map attributes) {
        this.attributes = attributes;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public AVIMTypedMessage convertToMessage() throws IOException {

        if (this.messageType == AVIMMessageType.TEXT_MESSAGE_TYPE) {
            AVIMTextMessage msg = new AVIMTextMessage();
            msg.setText(text);
            msg.setAttrs(attributes);
            return msg;
        } else if (this.messageType == AVIMMessageType.IMAGE_MESSAGE_TYPE) {
            AVIMImageMessage msg = new AVIMImageMessage(photoPath);
            msg.setAttrs(attributes);
            return msg;
        } else if (this.messageType == AVIMMessageType.AUDIO_MESSAGE_TYPE) {
            AVIMAudioMessage msg = new AVIMAudioMessage(voicePath);
            msg.getFileMetaData().put("duration",this.voiceDuration);
            msg.setAttrs(attributes);
            return msg;
        } else if (this.messageType == AVIMMessageType.VIDEO_MESSAGE_TYPE) {
            AVIMVideoMessage msg = new AVIMVideoMessage(videoPath);
            msg.getFileMetaData().put("duration",this.videoDuration);
            msg.setAttrs(attributes);
            return msg;
        }
        return null;
    }

}
