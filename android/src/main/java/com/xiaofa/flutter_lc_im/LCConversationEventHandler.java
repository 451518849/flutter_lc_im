package com.xiaofa.flutter_lc_im;

import java.util.List;

import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.AVIMConversation;
import cn.leancloud.im.v2.AVIMConversationEventHandler;
import io.flutter.plugin.common.EventChannel;

// Java/Android SDK 通过定制自己的对话事件 Handler 处理服务端下发的对话事件通知
public class LCConversationEventHandler extends AVIMConversationEventHandler {

    protected EventChannel.EventSink conversationEventCallback;

    public LCConversationEventHandler(EventChannel.EventSink conversationEventCallback){
        this.conversationEventCallback = conversationEventCallback;
    }
    @Override
    public void onMemberLeft(AVIMClient client, AVIMConversation conversation, List<String> members, String kickedBy) {

    }

    @Override
    public void onMemberJoined(AVIMClient client, AVIMConversation conversation, List<String> members, String invitedBy) {

    }

    @Override
    public void onKicked(AVIMClient client, AVIMConversation conversation, String kickedBy) {

    }

    /**
     * 实现本方法来处理当前用户被邀请到某个聊天对话事件
     *
     * @param client
     * @param conversation 被邀请的聊天对话
     * @param operator 邀请你的人
     * @since 3.0
     */
    @Override
    public void onInvited(AVIMClient client, AVIMConversation conversation, String invitedBy) {
        // 当前 clientId（Jerry）被邀请到对话，执行此处逻辑
    }
}
