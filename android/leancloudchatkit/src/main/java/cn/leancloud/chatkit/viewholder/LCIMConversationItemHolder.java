package cn.leancloud.chatkit.viewholder;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVCallback;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMChatRoom;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMReservedMessageType;
import com.avos.avoscloud.im.v2.AVIMServiceConversation;
import com.avos.avoscloud.im.v2.AVIMTemporaryConversation;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMMessagesQueryCallback;
import com.avos.avoscloud.im.v2.callback.AVIMSingleMessageQueryCallback;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.leancloud.chatkit.LCChatMessageInterface;
import cn.leancloud.chatkit.R;
import cn.leancloud.chatkit.cache.LCIMConversationItemCache;
import cn.leancloud.chatkit.event.LCIMConversationItemLongClickEvent;
import cn.leancloud.chatkit.utils.LCIMConstants;
import cn.leancloud.chatkit.utils.LCIMConversationUtils;
import cn.leancloud.chatkit.utils.LCIMLogUtils;
import de.greenrobot.event.EventBus;

/**
 * Created by wli on 15/10/8.
 * 会话 item 对应的 holder
 */
public class LCIMConversationItemHolder extends LCIMCommonViewHolder {

  ImageView avatarView;
  TextView unreadView;
  TextView typeView;
  TextView messageView;
  TextView timeView;
  TextView nameView;
  RelativeLayout avatarLayout;
  LinearLayout contentLayout;

  public LCIMConversationItemHolder(ViewGroup root) {
    super(root.getContext(), root, R.layout.lcim_conversation_item);
    initView();
  }

  public void initView() {
    avatarView = (ImageView) itemView.findViewById(R.id.conversation_item_iv_avatar);
    nameView = (TextView) itemView.findViewById(R.id.conversation_item_tv_name);
    timeView = (TextView) itemView.findViewById(R.id.conversation_item_tv_time);
    unreadView = (TextView) itemView.findViewById(R.id.conversation_item_tv_unread);
    typeView = (TextView) itemView.findViewById(R.id.conversation_item_tv_type);
    messageView = (TextView) itemView.findViewById(R.id.conversation_item_tv_message);
    avatarLayout = (RelativeLayout) itemView.findViewById(R.id.conversation_item_layout_avatar);
    contentLayout = (LinearLayout) itemView.findViewById(R.id.conversation_item_layout_content);
  }

  @Override
  public void bindData(Object o) {
    reset();
    final AVIMConversation conversation = (AVIMConversation) o;
    if (null != conversation) {
      if (null == conversation.getCreatedAt()) {
        conversation.fetchInfoInBackground(new AVIMConversationCallback() {
          @Override
          public void done(AVIMException e) {
            if (e != null) {
              LCIMLogUtils.logException(e);
            } else {
              updateName(conversation);
              updateIcon(conversation);
            }
          }
        });
      } else {
        updateName(conversation);
        updateIcon(conversation);
      }

      if (conversation instanceof AVIMServiceConversation) {
        typeView.setText("S");
      } else if (conversation instanceof AVIMTemporaryConversation) {
        typeView.setText("T");
      } else if (conversation instanceof AVIMChatRoom) {
        typeView.setText("R");
      } else {
        typeView.setText("C");
      }
      updateUnreadCount(conversation);
      updateLastMessage(conversation.getLastMessage());
      itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          onConversationItemClick(conversation);
        }
      });

      itemView.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
          AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
          builder.setItems(new String[]{"删除该聊天"}, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              EventBus.getDefault().post(new LCIMConversationItemLongClickEvent(conversation));
            }
          });
          AlertDialog dialog = builder.create();
          dialog.show();
          return false;
        }
      });
    }
  }

  /**
   * 一开始的时候全部置为空，避免因为异步请求造成的刷新不及时而导致的展示原有的缓存数据
   */
  private void reset() {
    avatarView.setImageResource(0);
    nameView.setText("");
    timeView.setText("");
    messageView.setText("");
    typeView.setText("");
    unreadView.setVisibility(View.GONE);
  }

  /**
   * 更新 name，单聊的话展示对方姓名，群聊展示所有用户的用户名
   *
   * @param conversation
   */
  private void updateName(final AVIMConversation conversation) {
    LCIMConversationUtils.getConversationName(conversation, new AVCallback<String>() {
      @Override
      protected void internalDone0(String s, AVException e) {
        if (null != e) {
          LCIMLogUtils.logException(e);
        } else {

          nameView.setText(s);
        }
      }
    });
  }

  /**
   * 更新 item icon，目前的逻辑为：
   * 单聊：展示对方的头像
   * 群聊：展示一个静态的 icon
   *
   * @param conversation
   */
  private void updateIcon(AVIMConversation conversation) {
    if (null != conversation) {
      if (conversation.isTransient() || conversation.getMembers().size() > 2) {
        avatarView.setImageResource(R.drawable.lcim_group_icon);
      } else {
        LCIMConversationUtils.getConversationPeerIcon(conversation, new AVCallback<String>() {
          @Override
          protected void internalDone0(String s, AVException e) {
            if (null != e) {
              LCIMLogUtils.logException(e);
            }
            if (!TextUtils.isEmpty(s)) {
              Picasso.with(getContext()).load(s)
                .placeholder(R.drawable.lcim_default_avatar_icon).into(avatarView);
            } else {
              avatarView.setImageResource(R.drawable.lcim_default_avatar_icon);
            }
          }
        });
      }
    }
  }

  /**
   * 更新未读消息数量
   *
   * @param conversation
   */
  private void updateUnreadCount(AVIMConversation conversation) {
    int num = conversation.getUnreadMessagesCount();
    unreadView.setText(num + "");
    unreadView.setVisibility(num > 0 ? View.VISIBLE : View.GONE);
  }

  /**
   * 更新 item 的展示内容，及最后一条消息的内容
   *
   * @param message
   */
  private void updateLastMessage(AVIMMessage message) {
    if (null != message) {
      Date date = new Date(message.getTimestamp());
      SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
      timeView.setText(format.format(date));
      messageView.setText(getMessageeShorthand(getContext(), message));
    }
  }

  private void onConversationItemClick(AVIMConversation conversation) {
    try {
      Intent intent = new Intent();
      intent.setPackage(getContext().getPackageName());
      intent.setAction(LCIMConstants.CONVERSATION_ITEM_CLICK_ACTION);
      intent.addCategory(Intent.CATEGORY_DEFAULT);
      intent.putExtra(LCIMConstants.CONVERSATION_ID, conversation.getConversationId());
      getContext().startActivity(intent);
    } catch (ActivityNotFoundException exception) {
      Log.i(LCIMConstants.LCIM_LOG_TAG, exception.toString());
    }
  }

  public static ViewHolderCreator HOLDER_CREATOR = new ViewHolderCreator<LCIMConversationItemHolder>() {
    @Override
    public LCIMConversationItemHolder createByViewGroupAndType(ViewGroup parent, int viewType, int attachData) {
      return new LCIMConversationItemHolder(parent);
    }
  };

  private static CharSequence getMessageeShorthand(Context context, AVIMMessage message) {
    if (message instanceof AVIMTypedMessage) {
      AVIMReservedMessageType type = AVIMReservedMessageType.getAVIMReservedMessageType(
        ((AVIMTypedMessage) message).getMessageType());
      switch (type) {
        case TextMessageType:
          return ((AVIMTextMessage) message).getText();
        case ImageMessageType:
          return context.getString(R.string.lcim_message_shorthand_image);
        case LocationMessageType:
          return context.getString(R.string.lcim_message_shorthand_location);
        case AudioMessageType:
          return context.getString(R.string.lcim_message_shorthand_audio);
        default:
          CharSequence shortHand = "";
          if (message instanceof LCChatMessageInterface) {
            LCChatMessageInterface messageInterface = (LCChatMessageInterface) message;
            shortHand = messageInterface.getShorthand();
          }
          if (TextUtils.isEmpty(shortHand)) {
            shortHand = context.getString(R.string.lcim_message_shorthand_unknown);
          }
          return shortHand;
      }
    } else {
      return message.getContent();
    }
  }
}
