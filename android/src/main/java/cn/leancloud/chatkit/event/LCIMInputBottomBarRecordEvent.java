package cn.leancloud.chatkit.event;

/**
 * Created by wli on 15/7/29.
 * InputBottomBar 录音事件，录音完成时触发
 */
public class LCIMInputBottomBarRecordEvent extends LCIMInputBottomBarEvent {

  /**
   * 录音本地路径
   */
  public String audioPath;

  /**
   * 录音长度
   */
  public int audioDuration;

  public LCIMInputBottomBarRecordEvent(int action, String path, int duration, Object tag) {
    super(action, tag);
    audioDuration = duration;
    audioPath = path;
  }
}
