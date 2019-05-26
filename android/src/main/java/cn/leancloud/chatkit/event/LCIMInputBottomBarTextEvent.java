package cn.leancloud.chatkit.event;

/**
 * Created by wli on 15/7/29.
 * InputBottomBar 发送文本事件
 */
public class LCIMInputBottomBarTextEvent extends LCIMInputBottomBarEvent {

  /**
   * 发送的文本内容
   */
  public String sendContent;

  public LCIMInputBottomBarTextEvent(int action, String content, Object tag) {
    super(action, tag);
    sendContent = content;
  }
}
