package com.xiaofa.flutter_lc_im;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.leancloud.AVQuery;
import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.AVIMConversation;
import cn.leancloud.im.v2.AVIMConversationsQuery;
import cn.leancloud.im.v2.AVIMException;
import cn.leancloud.im.v2.AVIMMessage;
import cn.leancloud.im.v2.AVIMMessageOption;
import cn.leancloud.im.v2.AVIMTypedMessage;
import cn.leancloud.im.v2.callback.AVIMConversationCallback;
import cn.leancloud.im.v2.callback.AVIMConversationQueryCallback;
import cn.leancloud.im.v2.callback.AVIMMessagesQueryCallback;
import io.flutter.plugin.common.EventChannel;

public class LCConversation extends AVIMConversation {

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    protected String clientId;

    public LCConversation(AVIMClient client, List<String> members, Map<String, Object> attributes, boolean isTransient) {
        super(client, members, attributes, isTransient);
    }

    public LCConversation(AVIMClient client,AVIMConversation conversation){
        super(client,conversation.getConversationId());
        this.setClientId(client.getClientId());
        this.setMembers(conversation.getMembers());
        this.setAttributes(conversation.getAttributes());
        this.setName(conversation.getName());
        this.setConversationId(conversation.getConversationId());
        this.setCreator(conversation.getCreator());
        this.setTemporaryExpiredat(conversation.getTemporaryExpiredat());
    }
    public LCConversation(AVIMClient client, String conversationId) {
        super(client, conversationId);
    }

    public void sendLcMessage(LCMessage message) throws IOException {
        AVIMTypedMessage avimTypedMessage = message.convertToMessage();
        AVIMMessageOption option = new AVIMMessageOption();
        String pushMessage = "{\"alert\":\"您有一条未读消息\"}";
        option.setPushData(pushMessage);
        this.sendMessage(avimTypedMessage,option,new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
                if (e == null) {
                    System.out.println("消息发送成功");

                }
            }
        });
    }

    public static void queryHistoryConversations(final AVIMClient client, int limit, int offset, final EventChannel.EventSink conversationEventCallback) {
        AVIMConversationsQuery query = client.getConversationsQuery();
        query.limit(limit);
        query.skip(offset);
        query.setQueryPolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.setCacheMaxAge(24 * 60 * 60);
        query.setWithLastMessagesRefreshed(true);
        query.findInBackground(new AVIMConversationQueryCallback() {
            @Override
            public void done(List<AVIMConversation> convs, AVIMException e) {
                if (e == null) {
                    // convs 就是想要的结果
                    sendConversationsToFlutter(convs,client.getClientId(),conversationEventCallback);
                }
            }
        });
    }

    private static void sendConversationsToFlutter(List<AVIMConversation> convs, String clientId, EventChannel.EventSink conversationEventCallback) {

        ArrayList conversations = new ArrayList();
        for (AVIMConversation con : convs) {

            if(con.getLastMessage() == null){
                continue;
            }

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateString = formatter.format(con.getLastMessageAt());
            Map<String, Object> dic = new HashMap<>();
            dic.put("conversationId", con.getConversationId());
            dic.put("members", con.getMembers());
            dic.put("clientId", clientId);
            dic.put("unreadMessagesCount", con.getUnreadMessagesCount());
            dic.put("lastMessage", con.getLastMessage().getContent());
            dic.put("lastMessageAt", dateString);
            conversations.add(dic);
        }

        if (conversationEventCallback != null){
            conversationEventCallback.success(conversations);
        }
    }

    public void queryHistoryConversationMessages(int limit, String messageId, long timestamp, final EventChannel.EventSink messageEventCallback) {

        if (messageId == null) {
            this.queryMessages(limit, new AVIMMessagesQueryCallback() {
                @Override
                public void done(List<AVIMMessage> messages, AVIMException e) {
                    if (e == null) {
                        sendConversationMessagesToFlutter(messages,messageEventCallback);
                    }
                }
            });
        } else {
            this.queryMessages(messageId, timestamp, limit,
                    new AVIMMessagesQueryCallback() {
                        @Override
                        public void done(List<AVIMMessage> messagesInPage, AVIMException e) {
                            if (e == null) {
                                // 查询成功返回
                                sendConversationMessagesToFlutter(messagesInPage,messageEventCallback);

                            }
                        }
                    });
        }
    }


    private void sendConversationMessagesToFlutter(List<AVIMMessage> mess, EventChannel.EventSink messageEventCallback) {

        ArrayList messages = new ArrayList();
        for (AVIMMessage message : mess) {
            Map<String, Object> dic = new HashMap<>();
            dic.put("messageId", message.getMessageId());
            dic.put("clientId", message.getFrom());
            dic.put("conversationId", message.getConversationId());
            dic.put("content", message.getContent());
            dic.put("timestamp", message.getTimestamp());
            if (clientId.equals(message.getFrom())){
                dic.put("ioType", AVIMMessage.AVIMMessageIOType.AVIMMessageIOTypeOut.getIOType());
            }else {
                dic.put("ioType", AVIMMessage.AVIMMessageIOType.AVIMMessageIOTypeIn.getIOType());
            }
            messages.add(dic);
        }

        if (messageEventCallback != null){
            messageEventCallback.success(messages);
        }
    }
}
