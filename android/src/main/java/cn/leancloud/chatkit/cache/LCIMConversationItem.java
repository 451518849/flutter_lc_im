package cn.leancloud.chatkit.cache;

import com.alibaba.fastjson.JSONObject;

import cn.leancloud.chatkit.utils.LCIMLogUtils;

/**
 * Created by wli on 16/3/8.
 * 会话 item，包含三个属性，ConversatoinId，unreadCount，updateTime
 */
class LCIMConversationItem implements Comparable {
  private static final String ITEM_KEY_CONVCERSATION_ID = "conversation_id";
  private static final String ITEM_KEY_UNDATE_TIME = "upadte_time";
  public String conversationId = "";
  public long updateTime = 0;

  public LCIMConversationItem() {
  }

  public LCIMConversationItem(String conversationId) {
    this.conversationId = conversationId;
  }

  public String toJsonString() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put(ITEM_KEY_CONVCERSATION_ID, conversationId);
    jsonObject.put(ITEM_KEY_UNDATE_TIME, updateTime);
    return jsonObject.toJSONString();
  }

  public static LCIMConversationItem fromJsonString(String json) {
    LCIMConversationItem item = new LCIMConversationItem();
    JSONObject jsonObject = null;
    try {
      jsonObject = JSONObject.parseObject(json);
      item.conversationId = jsonObject.getString(ITEM_KEY_CONVCERSATION_ID);
      item.updateTime = jsonObject.getLong(ITEM_KEY_UNDATE_TIME);
    } catch (Exception e) {
      LCIMLogUtils.logException(e);
    }
    return item;
  }

  @Override
  public int compareTo(Object another) {
    return (int) (((LCIMConversationItem) another).updateTime - updateTime);
  }
}