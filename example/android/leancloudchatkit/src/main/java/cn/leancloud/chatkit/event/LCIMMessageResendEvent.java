package cn.leancloud.chatkit.event;

import com.avos.avoscloud.im.v2.AVIMMessage;

/**
 * Created by wli on 16/2/23.
 * 聊天页面，重新发送消息的事件
 */
public class LCIMMessageResendEvent {
  public AVIMMessage message;
}
