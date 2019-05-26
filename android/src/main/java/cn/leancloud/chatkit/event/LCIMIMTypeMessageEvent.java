package cn.leancloud.chatkit.event;

import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;

/**
 * Created by wli on 15/8/23.
 * 收到 AVIMTypedMessage 消息后的事件
 */
public class LCIMIMTypeMessageEvent {
  public AVIMTypedMessage message;
  public AVIMConversation conversation;
}
