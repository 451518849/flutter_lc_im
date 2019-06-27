package com.xiaofa.flutter_lc_im;

import android.content.Context;
import android.content.Intent;

import com.avos.avoscloud.AVCallback;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.AVIMTypedMessageHandler;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;

import cn.leancloud.chatkit.LCChatKit;
import cn.leancloud.chatkit.LCChatKitUser;
import cn.leancloud.chatkit.R;
import cn.leancloud.chatkit.cache.LCIMConversationItemCache;
import cn.leancloud.chatkit.cache.LCIMProfileCache;
import cn.leancloud.chatkit.event.LCIMIMTypeMessageEvent;
import cn.leancloud.chatkit.utils.LCIMConstants;
import cn.leancloud.chatkit.utils.LCIMNotificationUtils;
import de.greenrobot.event.EventBus;

/**
 * Created by zhangxiaobo on 15/4/20.
 * AVIMTypedMessage 的 handler，socket 过来的 AVIMTypedMessage 都会通过此 handler 与应用交互
 * 需要应用主动调用 AVIMMessageManager.registerMessageHandler 来注册
 * 当然，自定义的消息也可以通过这种方式来处理
 */
public class CustomMessageHandler extends AVIMTypedMessageHandler<AVIMTypedMessage> {

    private Context context;

    public CustomMessageHandler(Context context) {
        this.context = context;
    }

    @Override
    public void onMessage(AVIMTypedMessage message, AVIMConversation conversation, AVIMClient client) {
        super.onMessage(message, conversation, client);
        System.out.println("onMessage:here" + message.getContent());
        if (message == null || message.getMessageId() == null) {
            System.out.println("may be SDK Bug, message or message id is null");
            return;
        }

        if (LCChatKit.getInstance().getCurrentUserId() == null) {
            System.out.println("selfId is null, please call LCChatKit.open!");
            client.close(null);
        } else {
            if (!client.getClientId().equals(LCChatKit.getInstance().getCurrentUserId())) {
                client.close(null);
            } else {
                System.out.println("conversation.getConversationId():" + conversation.getConversationId());
                sendNotification(message, conversation);
//                if (!LCIMNotificationUtils.isShowNotification(conversation.getConversationId())) {
//                    sendNotification(message, conversation);
//                }
                LCIMConversationItemCache.getInstance().insertConversation(message.getConversationId());
                if (!message.getFrom().equals(client.getClientId())) {
                    sendEvent(message, conversation);
                }
            }
        }
    }

    @Override
    public void onMessageReceipt(AVIMTypedMessage message, AVIMConversation conversation, AVIMClient client) {
        super.onMessageReceipt(message, conversation, client);
    }

    /**
     * 发送消息到来的通知事件
     *
     * @param message
     * @param conversation
     */
    private void sendEvent(AVIMTypedMessage message, AVIMConversation conversation) {
        LCIMIMTypeMessageEvent event = new LCIMIMTypeMessageEvent();
        event.message = message;
        event.conversation = conversation;
        EventBus.getDefault().post(event);
    }

    private void sendNotification(final AVIMTypedMessage message, final AVIMConversation conversation) {
        System.out.println("conversation:"+ conversation);
        System.out.println("message:"+ ((AVIMTextMessage) message).getText());
        if (null != conversation && null != message) {
            final String notificationContent = message instanceof AVIMTextMessage ?
                    ((AVIMTextMessage) message).getText() : context.getString(R.string.lcim_unspport_message_type);
            LCIMProfileCache.getInstance().getCachedUser(message.getFrom(), new AVCallback<LCChatKitUser>() {
                @Override
                protected void internalDone0(LCChatKitUser userProfile, AVException e) {
                    System.out.println("userProfile:"+ userProfile);
                    System.out.println("AVException:"+ e);
                    System.out.println("您有一条的新消息:"+ ((AVIMTextMessage) message).getText());
                    String title = "您有一条的新消息";
                    Intent intent = getIMNotificationIntent(conversation.getConversationId(), message.getFrom());
                    LCIMNotificationUtils.showNotification(context, title, notificationContent, null, intent);
//                    if (e != null) {
//                        System.out.println("推送消息:"+ message.getContent());
//                        String title = "您有一条的新消息";
//                        Intent intent = getIMNotificationIntent(conversation.getConversationId(), message.getFrom());
//                        LCIMNotificationUtils.showNotification(context, title, notificationContent, null, intent);
//                        LCIMLogUtils.logException(e);
//                    } else if (null != userProfile) {
//                        String title = userProfile.getName();
//                        Intent intent = getIMNotificationIntent(conversation.getConversationId(), message.getFrom());
//                        LCIMNotificationUtils.showNotification(context, title, notificationContent, null, intent);
//                    }else {
//                        System.out.println("推送消息:"+ message.getContent());
//                        String title = "您有一条的新消息";
//                        Intent intent = getIMNotificationIntent(conversation.getConversationId(), message.getFrom());
//                        LCIMNotificationUtils.showNotification(context, title, notificationContent, null, intent);
//                    }
                }
            });
        }
    }

    /**
     * 点击 notification 触发的 Intent
     * 注意要设置 package 已经 Category，避免多 app 同时引用 lib 造成消息干扰
     * @param conversationId
     * @param peerId
     * @return
     */
    private Intent getIMNotificationIntent(String conversationId, String peerId) {
        Intent intent = new Intent();
        intent.setAction(LCIMConstants.CHAT_NOTIFICATION_ACTION);
        intent.putExtra(LCIMConstants.CONVERSATION_ID, conversationId);
        intent.putExtra(LCIMConstants.PEER_ID, peerId);
        intent.setPackage(context.getPackageName());
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        return intent;
    }
}
