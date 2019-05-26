package cn.leancloud.chatkit.handler;

import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMConversationEventHandler;

import java.util.List;

import cn.leancloud.chatkit.cache.LCIMConversationItemCache;
import cn.leancloud.chatkit.event.LCIMConversationReadStatusEvent;
import cn.leancloud.chatkit.event.LCIMOfflineMessageCountChangeEvent;
import cn.leancloud.chatkit.event.LCIMMessageUpdatedEvent;
import cn.leancloud.chatkit.utils.LCIMLogUtils;
import de.greenrobot.event.EventBus;

/**
 * Created by wli on 15/12/1.
 * 和 Conversation 相关的事件的 handler
 * 需要应用主动调用  AVIMMessageManager.setConversationEventHandler
 * 关于回调会何时执行可以参见 https://leancloud.cn/docs/realtime_guide-android.html#添加其他成员
 */
public class LCIMConversationHandler extends AVIMConversationEventHandler {

  private static LCIMConversationHandler eventHandler;

  public static synchronized LCIMConversationHandler getInstance() {
    if (null == eventHandler) {
      eventHandler = new LCIMConversationHandler();
    }
    return eventHandler;
  }

  private LCIMConversationHandler() {
  }

  @Override
  public void onUnreadMessagesCountUpdated(AVIMClient client, AVIMConversation conversation) {
    LCIMConversationItemCache.getInstance().insertConversation(conversation.getConversationId());
    AVIMMessage lastMessage = conversation.getLastMessage();
    System.out.println("LCIMConversationHandler#onUnreadMessagesCountUpdated conv=" + conversation.getConversationId() + ", lastMsg: " + lastMessage.getContent());
    EventBus.getDefault().post(new LCIMOfflineMessageCountChangeEvent(conversation, lastMessage));
  }

  @Override
  public void onLastDeliveredAtUpdated(AVIMClient client, AVIMConversation conversation) {
    LCIMConversationReadStatusEvent event = new LCIMConversationReadStatusEvent();
    event.conversationId = conversation.getConversationId();
    EventBus.getDefault().post(event);
  }

  @Override
  public void onLastReadAtUpdated(AVIMClient client, AVIMConversation conversation) {
    LCIMConversationReadStatusEvent event = new LCIMConversationReadStatusEvent();
    event.conversationId = conversation.getConversationId();
    EventBus.getDefault().post(event);
  }

  @Override
  public void onMemberLeft(AVIMClient client, AVIMConversation conversation, List<String> members, String kickedBy) {
    // 因为不同用户需求不同，此处暂不做默认处理，如有需要，用户可以通过自定义 Handler 实现
  }

  @Override
  public void onMemberJoined(AVIMClient client, AVIMConversation conversation, List<String> members, String invitedBy) {
  }

  @Override
  public void onKicked(AVIMClient client, AVIMConversation conversation, String kickedBy) {
  }

  @Override
  public void onInvited(AVIMClient client, AVIMConversation conversation, String operator) {
  }

  @Override
  public void onMessageRecalled(AVIMClient client, AVIMConversation conversation, AVIMMessage message) {
    EventBus.getDefault().post(new LCIMMessageUpdatedEvent(message));
  }

  @Override
  public void onMessageUpdated(AVIMClient client, AVIMConversation conversation, AVIMMessage message) {
    LCIMLogUtils.d("message " + message.getMessageId() + " updated!");
    EventBus.getDefault().post(new LCIMMessageUpdatedEvent(message));
  }

}
