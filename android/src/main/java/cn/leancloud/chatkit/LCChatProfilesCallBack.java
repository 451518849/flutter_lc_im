package cn.leancloud.chatkit;


import java.util.List;

/**
 * Created by wli on 16/2/2.
 */
public interface LCChatProfilesCallBack {
  public void done(List<LCChatKitUser> userList, Exception exception);
}
