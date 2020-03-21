package com.xiaofa.flutter_lc_im;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import cn.leancloud.im.v2.AVIMConversation;
import cn.leancloud.im.v2.AVIMMessage;

public class LCConvertUtils {

    public static String clientId;

    public static Map<String,Object> convertMessageToFlutterModel(AVIMMessage message){
        Map<String, Object> dic = new HashMap<>();
        dic.put("messageId", message.getMessageId());
        dic.put("clientId", clientId);
        dic.put("conversationId", message.getConversationId());
        dic.put("content", message.getContent());
        dic.put("timestamp", message.getTimestamp());
        if (clientId.equals(message.getFrom())){
            dic.put("ioType", AVIMMessage.AVIMMessageIOType.AVIMMessageIOTypeOut.getIOType());
        }else {
            dic.put("ioType", AVIMMessage.AVIMMessageIOType.AVIMMessageIOTypeIn.getIOType());
        }
        return dic;
    }

    public static Map<String,Object> convertConversationToFlutterModel(AVIMConversation con){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(con.getLastMessageAt());
        Map<String, Object> dic = new HashMap<>();
        dic.put("conversationId", con.getConversationId());
        dic.put("members", con.getMembers());
        dic.put("clientId", clientId);
        dic.put("unreadMessagesCount", con.getUnreadMessagesCount());
        dic.put("lastMessage", con.getLastMessage().getContent());
        dic.put("lastMessageAt", dateString);

        return dic;
    }
}
