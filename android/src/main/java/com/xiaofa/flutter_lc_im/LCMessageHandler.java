package com.xiaofa.flutter_lc_im;

import java.util.ArrayList;
import java.util.Map;

import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.AVIMConversation;
import cn.leancloud.im.v2.AVIMMessage;
import cn.leancloud.im.v2.AVIMMessageHandler;
import cn.leancloud.im.v2.messages.AVIMAudioMessage;
import cn.leancloud.im.v2.messages.AVIMImageMessage;
import cn.leancloud.im.v2.messages.AVIMTextMessage;
import cn.leancloud.im.v2.messages.AVIMVideoMessage;
import io.flutter.plugin.common.EventChannel;

// Java/Android SDK 通过定制自己的消息事件 Handler 处理服务端下发的消息通知
public class LCMessageHandler extends AVIMMessageHandler {

    public EventChannel.EventSink messageEventCallback;
    public EventChannel.EventSink conversationEventCallback;

    public LCMessageHandler(){}
    public LCMessageHandler(EventChannel.EventSink messageEventCallback){
       this.messageEventCallback = messageEventCallback;
    }
    /**
     * 重载此方法来处理接收消息
     *
     * @param message
     * @param conversation
     * @param client
     */
    @Override
    public void onMessage(AVIMMessage message, AVIMConversation conversation, AVIMClient client){

        this.convertMessageToFlutter(conversation,message);
    }

    public void convertMessageToFlutter(AVIMConversation conversation,AVIMMessage message){

        ArrayList messages = new ArrayList();

        if (LCPushService.isOpen){
            if (message instanceof AVIMTextMessage){
                LCPushService.sendMessageNotification(((AVIMTextMessage) message).getText());
            }else if (message instanceof AVIMImageMessage){
                LCPushService.sendMessageNotification("[图片消息]");
            }else if (message instanceof AVIMVideoMessage){
                LCPushService.sendMessageNotification("[视频消息]");
            }else if (message instanceof AVIMAudioMessage){
                LCPushService.sendMessageNotification("[音频消息]");
            }
        }

        Map dic = LCConvertUtils.convertMessageToFlutterModel(message);
        messages.add(dic);

        if (this.messageEventCallback != null){
            this.messageEventCallback.success(messages);
        }

        if (conversationEventCallback != null){
            ArrayList conversations = new ArrayList();
            Map con = LCConvertUtils.convertConversationToFlutterModel(conversation);
            conversations.add(con);
            conversationEventCallback.success(conversations);
        }


    }

}