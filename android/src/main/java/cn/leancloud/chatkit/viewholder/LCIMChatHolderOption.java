package cn.leancloud.chatkit.viewholder;

/**
 * Created by wli on 2017/3/23.
 */

public class LCIMChatHolderOption {

  private boolean isShowTime = false;
  private boolean isShowName = false;
  private boolean isShowRead = false;
  private boolean isShowDelivered = false;

  public boolean isShowTime() {
    return isShowTime;
  }

  public void setShowTime(boolean showTime) {
    isShowTime = showTime;
  }

  public boolean isShowName() {
    return isShowName;
  }

  public void setShowName(boolean showName) {
    isShowName = showName;
  }

  public boolean isShowRead() {
    return isShowRead;
  }

  public void setShowRead(boolean showRead) {
    isShowRead = showRead;
  }

  public boolean isShowDelivered() {
    return isShowDelivered;
  }

  public void setShowDelivered(boolean showDelivered) {
    isShowDelivered = showDelivered;
  }
}
