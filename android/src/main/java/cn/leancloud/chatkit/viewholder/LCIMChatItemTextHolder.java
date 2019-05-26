package cn.leancloud.chatkit.viewholder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;

import cn.leancloud.chatkit.R;


/**
 * Created by wli on 15/9/17.
 * 聊天页面中的文本 item 对应的 holder
 */
public class LCIMChatItemTextHolder extends LCIMChatItemHolder {

  protected TextView contentView;

  public LCIMChatItemTextHolder(Context context, ViewGroup root, boolean isLeft) {
    super(context, root, isLeft);
  }

  @Override
  public void initView() {
    super.initView();
    if (isLeft) {
      conventLayout.addView(View.inflate(getContext(), R.layout.lcim_chat_item_left_text_layout, null));
      contentView = (TextView) itemView.findViewById(R.id.chat_left_text_tv_content);
    } else {
      conventLayout.addView(View.inflate(getContext(), R.layout.lcim_chat_item_right_text_layout, null));
      contentView = (TextView) itemView.findViewById(R.id.chat_right_text_tv_content);
    }
  }

  @Override
  public void bindData(Object o) {
    super.bindData(o);
    AVIMMessage message = (AVIMMessage) o;
    if (message instanceof AVIMTextMessage) {
      AVIMTextMessage textMessage = (AVIMTextMessage) message;
      contentView.setText(textMessage.getText());
    }
  }
}
