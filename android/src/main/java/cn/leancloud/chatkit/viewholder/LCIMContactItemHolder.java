package cn.leancloud.chatkit.viewholder;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import cn.leancloud.chatkit.LCChatKitUser;
import cn.leancloud.chatkit.R;
import cn.leancloud.chatkit.activity.LCIMConversationActivity;
import cn.leancloud.chatkit.adapter.LCIMCommonListAdapter;
import cn.leancloud.chatkit.event.LCIMMemberSelectedChangeEvent;
import cn.leancloud.chatkit.utils.LCIMConstants;
import de.greenrobot.event.EventBus;

/**
 * Created by wli on 15/11/24.
 */
public class LCIMContactItemHolder extends LCIMCommonViewHolder<LCChatKitUser> {

  TextView nameView;
  ImageView avatarView;
  CheckBox checkBox;

  public LCChatKitUser lcChatKitUser;
  private int showMode = 0;

  public LCIMContactItemHolder(Context context, ViewGroup root) {
    this(context, root, LCIMCommonListAdapter.ListMode.SHOW_ACTION.intValue());
  }

  public LCIMContactItemHolder(Context context, ViewGroup root, int showMode) {
    super(context, root, R.layout.common_user_item);
    this.showMode = showMode;
    initView();
  }

  public void initView() {
    nameView = (TextView)itemView.findViewById(R.id.tv_friend_name);
    avatarView = (ImageView)itemView.findViewById(R.id.img_friend_avatar);
    checkBox = (CheckBox)itemView.findViewById(R.id.checkBox);

    if (LCIMCommonListAdapter.ListMode.SELECT.intValue() == this.showMode) {
      checkBox.setVisibility(View.VISIBLE);
      checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          LCIMMemberSelectedChangeEvent event = new LCIMMemberSelectedChangeEvent();
          event.member = lcChatKitUser;
          event.isSelected = isChecked;
          EventBus.getDefault().post(event);
        }
      });
    } else {
      checkBox.setVisibility(View.GONE);
    }

    if (LCIMCommonListAdapter.ListMode.SHOW_ACTION.intValue() == this.showMode) {
      itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent intent = new Intent(getContext(), LCIMConversationActivity.class);
          intent.putExtra(LCIMConstants.PEER_ID, lcChatKitUser.getUserId());
          getContext().startActivity(intent);
        }
      });
    }
  }

  @Override
  public void bindData(LCChatKitUser lcChatKitUser) {
    this.lcChatKitUser = lcChatKitUser;
    final String avatarUrl = lcChatKitUser.getAvatarUrl();
    if (!TextUtils.isEmpty(avatarUrl)) {
      Picasso.with(getContext()).load(avatarUrl).into(avatarView);
    } else {
      avatarView.setImageResource(R.drawable.lcim_default_avatar_icon);
    }
    nameView.setText(lcChatKitUser.getName());
  }

  public static ViewHolderCreator HOLDER_CREATOR = new ViewHolderCreator<LCIMContactItemHolder>() {
    @Override
    public LCIMContactItemHolder createByViewGroupAndType(ViewGroup parent, int viewType, int attachData) {
      return new LCIMContactItemHolder(parent.getContext(), parent, attachData);
    }
  };
}
