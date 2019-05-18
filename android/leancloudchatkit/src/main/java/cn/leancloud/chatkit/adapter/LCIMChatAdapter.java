package cn.leancloud.chatkit.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMReservedMessageType;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.leancloud.chatkit.LCChatKit;
import cn.leancloud.chatkit.viewholder.LCIMChatHolderOption;
import cn.leancloud.chatkit.viewholder.LCIMChatItemAudioHolder;
import cn.leancloud.chatkit.viewholder.LCIMChatItemHolder;
import cn.leancloud.chatkit.viewholder.LCIMChatItemImageHolder;
import cn.leancloud.chatkit.viewholder.LCIMChatItemLocationHolder;
import cn.leancloud.chatkit.viewholder.LCIMChatItemTextHolder;
import cn.leancloud.chatkit.viewholder.LCIMCommonViewHolder;

/**
 * Created by wli on 15/8/13.
 * 聊天的 Adapter，此处还有可优化的地方，稍后考虑一下提取出公共的 adapter
 */
public class LCIMChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private final int ITEM_LEFT = 100;
  private final int ITEM_LEFT_TEXT = 101;
  private final int ITEM_LEFT_IMAGE = 102;
  private final int ITEM_LEFT_AUDIO = 103;
  private final int ITEM_LEFT_LOCATION = 104;

  private final int ITEM_RIGHT = 200;
  private final int ITEM_RIGHT_TEXT = 201;
  private final int ITEM_RIGHT_IMAGE = 202;
  private final int ITEM_RIGHT_AUDIO = 203;
  private final int ITEM_RIGHT_LOCATION = 204;

  private final int ITEM_UNKNOWN = 300;

  // 时间间隔最小为十分钟
  private final static long TIME_INTERVAL = 1000 * 60 * 3;
  private boolean isShowUserName = true;
  protected List<AVIMMessage> messageList = new ArrayList<AVIMMessage>();
  private Set<String> messageIdSet = new HashSet<>();

  private long lastDeliveredAt = 0;
  private long lastReadAt = 0;

  public LCIMChatAdapter() {
    super();
  }

  public void setMessageList(List<AVIMMessage> messages) {
    messageList.clear();
    messageIdSet.clear();
    if (null != messages) {
      for (AVIMMessage msg : messages) {
        if (messageIdSet.add(msg.getMessageId())) {
          messageList.add(msg);
        }
      }
    }
  }

  /**
   * 添加多条消息记录
   * @param messages
   */
  public void addMessageList(List<AVIMMessage> messages) {
    for (int i = messages.size(); i> 0; i--) {
      AVIMMessage msg = messages.get(i - 1);
      if (messageIdSet.add(msg.getMessageId())) {
        messageList.add(0, msg);
      }
    }
  }

  /**
   * 添加消息记录
   * @param message
   */
  public void addMessage(AVIMMessage message) {
    if (messageIdSet.add(message.getMessageId())) {
      messageList.add(message);
    }
  }

  public void updateMessage(AVIMMessage message) {
    for (int i = 0; i < messageList.size(); i++) {
      if (messageList.get(i).getMessageId().equals(message.getMessageId())) {
        messageList.remove(i);
        messageList.add(i, message);
        notifyItemChanged(i);
        return;
      }
    }
  }

  /**
   * 获取第一条消息记录，方便下拉时刷新数据
   * @return
   */
  public AVIMMessage getFirstMessage() {
    if (null != messageList && messageList.size() > 0) {
      return messageList.get(0);
    } else {
      return null;
    }
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    switch (viewType) {
      case ITEM_LEFT:
      case ITEM_LEFT_TEXT:
        return new LCIMChatItemTextHolder(parent.getContext(), parent, true);
      case ITEM_LEFT_IMAGE:
        return new LCIMChatItemImageHolder(parent.getContext(), parent, true);
      case ITEM_LEFT_AUDIO:
        return new LCIMChatItemAudioHolder(parent.getContext(), parent, true);
      case ITEM_LEFT_LOCATION:
        return new LCIMChatItemLocationHolder(parent.getContext(), parent, true);
      case ITEM_RIGHT:
      case ITEM_RIGHT_TEXT:
        return new LCIMChatItemTextHolder(parent.getContext(), parent, false);
      case ITEM_RIGHT_IMAGE:
        return new LCIMChatItemImageHolder(parent.getContext(), parent, false);
      case ITEM_RIGHT_AUDIO:
        return new LCIMChatItemAudioHolder(parent.getContext(), parent, false);
      case ITEM_RIGHT_LOCATION:
        return new LCIMChatItemLocationHolder(parent.getContext(), parent, false);
      default:
        return new LCIMChatItemTextHolder(parent.getContext(), parent, true);
    }
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    ((LCIMCommonViewHolder)holder).bindData(messageList.get(position));
    if (holder instanceof LCIMChatItemHolder) {
      LCIMChatHolderOption option = new LCIMChatHolderOption();
      option.setShowName(isShowUserName);
      option.setShowTime(shouldShowTime(position));
      option.setShowDelivered(shouldShowDelivered(position));
      option.setShowRead(shouldShowRead(position));
      ((LCIMChatItemHolder)holder).setHolderOption(option);
    }
  }

  @Override
  public int getItemViewType(int position) {
    AVIMMessage message = messageList.get(position);
    if (null != message && message instanceof AVIMTypedMessage) {
      AVIMTypedMessage typedMessage = (AVIMTypedMessage) message;
      boolean isMe = fromMe(typedMessage);
      if (typedMessage.getMessageType() == AVIMReservedMessageType.TextMessageType.getType()) {
        return isMe ? ITEM_RIGHT_TEXT : ITEM_LEFT_TEXT;
      } else if (typedMessage.getMessageType() == AVIMReservedMessageType.AudioMessageType.getType()) {
        return isMe ? ITEM_RIGHT_AUDIO : ITEM_LEFT_AUDIO;
      } else if (typedMessage.getMessageType() == AVIMReservedMessageType.ImageMessageType.getType()) {
        return isMe ? ITEM_RIGHT_IMAGE : ITEM_LEFT_IMAGE;
      } else if (typedMessage.getMessageType() == AVIMReservedMessageType.LocationMessageType.getType()) {
        return isMe ? ITEM_RIGHT_LOCATION : ITEM_LEFT_LOCATION;
      } else {
        return isMe ? ITEM_RIGHT : ITEM_LEFT;
      }
    }
    return ITEM_UNKNOWN;
  }

  @Override
  public int getItemCount() {
    return messageList.size();
  }

  /**
   * item 是否应该展示时间
   * @param position
   * @return
   */
  private boolean shouldShowTime(int position) {
    if (position == 0) {
      return true;
    }
    long lastTime = messageList.get(position - 1).getTimestamp();
    long curTime = messageList.get(position).getTimestamp();
    return curTime - lastTime > TIME_INTERVAL;
  }

  /**
   * 是否应该展示已送达标记
   * @param position
   * @return
   */
  private boolean shouldShowDelivered(int position) {
    if (null != messageList && messageList.size() > 0) {
      int size = messageList.size();
      if (position < size) {
        long curTime = messageList.get(position).getTimestamp();
        if (curTime < lastDeliveredAt) {
          return position == size - 1 || lastDeliveredAt < messageList.get(position + 1).getTimestamp();
        }
      }
    }
    return false;
  }

  /**
   * 是否应该展示已读标记
   * @param position
   * @return
   */
  private boolean shouldShowRead(int position) {
    if (null != messageList && messageList.size() > 0) {
      int size = messageList.size();
      if (position < size) {
        long curTime = messageList.get(position).getTimestamp();
        if (curTime < lastReadAt) {
          return position == size - 1 || lastReadAt < messageList.get(position + 1).getTimestamp();
        }
      }
    }
    return false;
  }

  /**
   * item 是否展示用户名
   * 因为
   * @param isShow
   */
  public void showUserName(boolean isShow) {
    isShowUserName = isShow;
  }

  /**
   * 设置已读及送达回执的标志位置
   * @param deliveredAt
   * @param readAt
   */
  public void setDeliveredAndReadMark(long deliveredAt, long readAt) {
    lastDeliveredAt = deliveredAt;
    lastReadAt = readAt;
  }

  /**
   * 因为 RecyclerView 中的 item 缓存默认最大为 5，造成会重复的 create item 而卡顿
   * 所以这里根据不同的类型设置不同的缓存值，经验值，不同 app 可以根据自己的场景进行更改
   */
  public void resetRecycledViewPoolSize(RecyclerView recyclerView) {
    recyclerView.getRecycledViewPool().setMaxRecycledViews(ITEM_LEFT_TEXT, 25);
    recyclerView.getRecycledViewPool().setMaxRecycledViews(ITEM_LEFT_IMAGE, 10);
    recyclerView.getRecycledViewPool().setMaxRecycledViews(ITEM_LEFT_AUDIO, 15);
    recyclerView.getRecycledViewPool().setMaxRecycledViews(ITEM_LEFT_LOCATION, 10);

    recyclerView.getRecycledViewPool().setMaxRecycledViews(ITEM_RIGHT_TEXT, 25);
    recyclerView.getRecycledViewPool().setMaxRecycledViews(ITEM_RIGHT_IMAGE, 10);
    recyclerView.getRecycledViewPool().setMaxRecycledViews(ITEM_RIGHT_AUDIO, 15);
    recyclerView.getRecycledViewPool().setMaxRecycledViews(ITEM_RIGHT_LOCATION, 10);
  }

  /**
   * 是不是当前用户发送的数据
   * @param msg
   * @return
   */
  protected boolean fromMe(AVIMTypedMessage msg) {
    String selfId = LCChatKit.getInstance().getCurrentUserId();
    return msg.getFrom() != null && msg.getFrom().equals(selfId);
  }
}