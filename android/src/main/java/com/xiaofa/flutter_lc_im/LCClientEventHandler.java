package com.xiaofa.flutter_lc_im;

import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.AVIMClientEventHandler;

import static com.xiaofa.flutter_lc_im.FlutterLcImPlugin.clientStatusEventCallback;

/**
 * Created by wli on 15/12/16.
 * 与网络相关的 handler
 * 注意，此 handler 并不是网络状态通知，而是当前 client 的连接状态
 */
public class LCClientEventHandler extends AVIMClientEventHandler {

  private static LCClientEventHandler eventHandler;

  public static synchronized LCClientEventHandler getInstance() {
    if (null == eventHandler) {
      eventHandler = new LCClientEventHandler();
    }
    return eventHandler;
  }

  private LCClientEventHandler() {
  }

  @Override
  public void onConnectionPaused(AVIMClient avimClient) {
    if (clientStatusEventCallback != null) {
      clientStatusEventCallback.success("Paused");
    }
  }

  @Override
  public void onConnectionResume(AVIMClient avimClient) {
    if (clientStatusEventCallback != null) {
    clientStatusEventCallback.success("Resumed");
  }
  }

  @Override
  public void onClientOffline(AVIMClient avimClient, int i) {
    if (clientStatusEventCallback != null) {
      clientStatusEventCallback.success("Closed");
    }
  }
}
