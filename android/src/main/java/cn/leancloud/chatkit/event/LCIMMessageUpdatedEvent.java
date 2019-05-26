package cn.leancloud.chatkit.event;

import com.avos.avoscloud.im.v2.AVIMMessage;

/**
 * Created by fengjunwen on 2017/11/16.
 */

public class LCIMMessageUpdatedEvent {
  public AVIMMessage message;

  public LCIMMessageUpdatedEvent(AVIMMessage message) {
    this.message = message;
  }
}
