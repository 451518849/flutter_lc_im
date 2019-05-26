package cn.leancloud.chatkit.cache;

import android.content.Context;
import android.text.TextUtils;

import com.avos.avoscloud.AVCallback;
import com.avos.avoscloud.AVException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by wli on 16/2/26.
 * 缓存未读消息数量
 * <p/>
 * 流程
 * 1、初始化时从 db 里同步数据到缓存
 * 2、插入数据时先更新缓存，在更新 db
 * 3、获取的话只从缓存里读取数据
 */
public class LCIMConversationItemCache {

  private final String CONVERSATION_ITEM_TABLE_NAME = "ConversationItem";

  private Map<String, LCIMConversationItem> conversationItemMap;
  private LCIMLocalStorage conversationItemDBHelper;

  private LCIMConversationItemCache() {
    conversationItemMap = new HashMap<String, LCIMConversationItem>();
  }

  private static LCIMConversationItemCache conversationItemCache;

  public static synchronized LCIMConversationItemCache getInstance() {
    if (null == conversationItemCache) {
      conversationItemCache = new LCIMConversationItemCache();
    }
    return conversationItemCache;
  }

  /**
   * 因为只有在第一次的时候需要设置 Context 以及 clientId，所以单独拎出一个函数主动调用初始化
   * 避免 getInstance 传入过多参数
   * 因为需要同步数据，所以此处需要有回调
   */
  public synchronized void initDB(Context context, String clientId, AVCallback callback) {
    conversationItemDBHelper = new LCIMLocalStorage(context, clientId, CONVERSATION_ITEM_TABLE_NAME);
    conversationItemMap.clear();
    syncData(callback);
  }

  /**
   * 删除该 Conversation 未读数量的缓存
   *
   * @param convid 不能为空
   */
  public synchronized void deleteConversation(String convid) {
    if (!TextUtils.isEmpty(convid)) {
      conversationItemMap.remove(convid);
      conversationItemDBHelper.deleteData(Arrays.asList(convid));
    }
  }

  /**
   * 缓存该 Conversastoin，默认未读数量为 0
   *
   * @param convId 不能为空
   */
  public synchronized void insertConversation(String convId) {
    if (!TextUtils.isEmpty(convId)) {
      LCIMConversationItem item = getConversationItemFromMap(convId);
      item.updateTime = System.currentTimeMillis();
      syncToCache(item);
    }
  }

  /**
   * 缓存该 conversation
   * @param convId conversationId
   * @param milliSeconds 指定该 conversation 更新的时间，用于排序
   */
  public synchronized void insertConversation(String convId, long milliSeconds) {
    if (!TextUtils.isEmpty(convId) && milliSeconds >= 0) {
      LCIMConversationItem item = getConversationItemFromMap(convId);
      item.updateTime = milliSeconds;
      syncToCache(item);
    }
  }

  /**
   * 获得排序后的 Conversation Id list，根据本地更新时间降序排列
   *
   * @return
   */
  public synchronized List<String> getSortedConversationList() {
    List<String> idList = new ArrayList<>();
    SortedSet<LCIMConversationItem> sortedSet = new TreeSet<>();
    sortedSet.addAll(conversationItemMap.values());
    for (LCIMConversationItem item : sortedSet) {
      idList.add(item.conversationId);
    }
    return idList;
  }

  public synchronized void cleanup() {
    conversationItemDBHelper.deleteAllData();
  }

  /**
   * 同步 db 数据到内存中
   */
  private void syncData(final AVCallback callback) {
    conversationItemDBHelper.getIds(new AVCallback<List<String>>() {
      @Override
      protected void internalDone0(final List<String> idList, AVException e) {
        conversationItemDBHelper.getData(idList, new AVCallback<List<String>>() {
          @Override
          protected void internalDone0(final List<String> dataList, AVException e) {
            if (null != dataList) {
              for (int i = 0; i < dataList.size(); i++) {
                LCIMConversationItem conversationItem = LCIMConversationItem.fromJsonString(dataList.get(i));
                conversationItemMap.put(conversationItem.conversationId, conversationItem);
              }
            }
            callback.internalDone(e);
          }
        });
      }
    });
  }

  /**
   * 从 map 中获取 ConversationItem，如缓存中没有，则 new 一个新实例返回
   *
   * @param convId
   * @return
   */
  private LCIMConversationItem getConversationItemFromMap(String convId) {
    if (conversationItemMap.containsKey(convId)) {
      return conversationItemMap.get(convId);
    }
    return new LCIMConversationItem(convId);
  }

  /**
   * 存储未读消息数量到内存
   */
  private void syncToCache(LCIMConversationItem item) {
    if (null != item) {
      conversationItemMap.put(item.conversationId, item);
      conversationItemDBHelper.insertData(item.conversationId, item.toJsonString());
    }
  }
}
