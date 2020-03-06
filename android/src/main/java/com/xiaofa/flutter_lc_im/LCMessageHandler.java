package com.xiaofa.flutter_lc_im;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.AVIMConversation;
import cn.leancloud.im.v2.AVIMMessage;
import cn.leancloud.im.v2.AVIMMessageHandler;
import cn.leancloud.im.v2.messages.AVIMTextMessage;
import io.flutter.plugin.common.EventChannel;

// Java/Android SDK 通过定制自己的消息事件 Handler 处理服务端下发的消息通知
public class LCMessageHandler extends AVIMMessageHandler {

    public EventChannel.EventSink messageEventCallback;

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

        System.out.println("接收到消息:"+message);

        if(message instanceof AVIMTextMessage){
            ArrayList messages = new ArrayList();
            Map<String, Object> dic = new HashMap<>();
            dic.put("messageId", message.getMessageId());
            dic.put("clientId", message.getFrom());
            dic.put("conversationId", message.getConversationId());
            dic.put("content", message.getContent());
            dic.put("timestamp", message.getTimestamp());
            dic.put("ioType", message.getMessageIOType().getIOType());
            messages.add(dic);
            if (this.messageEventCallback != null){
                this.messageEventCallback.success(messages);
            }
        }
    }
}