package cn.leancloud.chatkit.event;

import com.avos.avoscloud.im.v2.AVIMConversation;

/**
 * Created by wli on 16/9/14.
 */
public class LCIMConversationItemLongClickEvent {
  public AVIMConversation conversation;

  public LCIMConversationItemLongClickEvent(AVIMConversation conversation) {
    this.conversation = conversation;
  }
}
