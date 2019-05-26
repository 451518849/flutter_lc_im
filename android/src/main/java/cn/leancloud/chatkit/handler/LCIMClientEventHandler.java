package cn.leancloud.chatkit.handler;

import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMClientEventHandler;

import cn.leancloud.chatkit.event.LCIMConnectionChangeEvent;
import cn.leancloud.chatkit.utils.LCIMLogUtils;
import de.greenrobot.event.EventBus;

/**
 * Created by wli on 15/12/16.
 * 与网络相关的 handler
 * 注意，此 handler 并不是网络状态通知，而是当前 client 的连接状态
 */
public class LCIMClientEventHandler extends AVIMClientEventHandler {

  private static LCIMClientEventHandler eventHandler;

  public static synchronized LCIMClientEventHandler getInstance() {
    if (null == eventHandler) {
      eventHandler = new LCIMClientEventHandler();
    }
    return eventHandler;
  }

  private LCIMClientEventHandler() {
  }


  private volatile boolean connect = false;

  /**
   * 是否连上聊天服务
   *
   * @return
   */
  public boolean isConnect() {
    return connect;
  }

  public void setConnectAndNotify(boolean isConnect) {
    connect = isConnect;
    EventBus.getDefault().post(new LCIMConnectionChangeEvent(connect));
  }

  @Override
  public void onConnectionPaused(AVIMClient avimClient) {
    setConnectAndNotify(false);
  }

  @Override
  public void onConnectionResume(AVIMClient avimClient) {
    setConnectAndNotify(true);
  }

  @Override
  public void onClientOffline(AVIMClient avimClient, int i) {
    LCIMLogUtils.d("client " + avimClient.getClientId() + " is offline!");
  }
}
