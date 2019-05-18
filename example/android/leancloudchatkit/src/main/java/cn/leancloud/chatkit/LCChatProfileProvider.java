package cn.leancloud.chatkit;

import java.util.List;

/**
 * Created by wli on 16/2/2.
 * 用户体系的接口，开发者需要实现此接口来接入 LCChatKit
 */
public interface LCChatProfileProvider {
  void fetchProfiles(List<String> userIdList, LCChatProfilesCallBack profilesCallBack);
  List<LCChatKitUser> getAllUsers();
}
