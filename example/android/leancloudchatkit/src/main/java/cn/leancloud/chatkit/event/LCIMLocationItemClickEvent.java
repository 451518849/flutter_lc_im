package cn.leancloud.chatkit.event;

import com.avos.avoscloud.im.v2.AVIMMessage;

/**
 * Created by wli on 15/9/20.
 * 聊天时地理位置 item 点击时触发该事件
 * 其实这些 item 都可以放到一个 event 处理，因为兼容以前的逻辑，暂时分开
 */
public class LCIMLocationItemClickEvent {
  public AVIMMessage message;
}