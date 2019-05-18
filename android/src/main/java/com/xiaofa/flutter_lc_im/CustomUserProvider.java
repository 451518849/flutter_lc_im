package com.xiaofa.flutter_lc_im;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.leancloud.chatkit.LCChatKitUser;
import cn.leancloud.chatkit.LCChatProfileProvider;
import cn.leancloud.chatkit.LCChatProfilesCallBack;

public class CustomUserProvider  implements LCChatProfileProvider {

    private static CustomUserProvider customUserProvider;

    public synchronized static CustomUserProvider getInstance() {
        if (null == customUserProvider) {
            customUserProvider = new CustomUserProvider();
        }
        return customUserProvider;
    }
    private CustomUserProvider() {}

    private List<LCChatKitUser> partUsers = new ArrayList<LCChatKitUser>();


    public void setAllUsers(List<Map<String,String>> users){
        for (Map<String,String> user : users){
            LCChatKitUser u = new LCChatKitUser();
            u.setUserId(user.get("user_id"));
            u.setName(user.get("name"));
            u.setAvatarUrl(user.get("avatar_url"));
            partUsers.add(u);
        }
    }

    @Override
    public void fetchProfiles(List<String> list, LCChatProfilesCallBack callBack) {
        List<LCChatKitUser> userList = new ArrayList<LCChatKitUser>();
        for (String userId : list) {
            for (LCChatKitUser user : partUsers) {
                if (user.getUserId().equals(userId)) {
                    userList.add(user);
                    break;
                }
            }
        }
        callBack.done(userList, null);
    }

    @Override
    public List<LCChatKitUser> getAllUsers() {
        return partUsers;
    }
}
