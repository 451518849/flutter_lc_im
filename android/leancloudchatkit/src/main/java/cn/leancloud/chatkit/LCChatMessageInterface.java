package cn.leancloud.chatkit;

/**
 * Created by wli on 16/7/11.
 * 用于自定义 Message 的一些 UI 展示
 */
public interface LCChatMessageInterface {

  /**
   * 自定义 Message 在回话列表页面的提示展示
   * @return
   */
  String getShorthand();
}
