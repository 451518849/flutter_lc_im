package cn.leancloud.chatkit.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.leancloud.chatkit.LCChatKit;
import cn.leancloud.chatkit.LCChatKitUser;
import cn.leancloud.chatkit.R;
import cn.leancloud.chatkit.adapter.LCIMCommonListAdapter;
import cn.leancloud.chatkit.adapter.LCIMMembersAdapter;
import cn.leancloud.chatkit.event.LCIMMemberSelectedChangeEvent;
import cn.leancloud.chatkit.event.LCIMMemberLetterEvent;
import cn.leancloud.chatkit.view.LCIMDividerItemDecoration;
import de.greenrobot.event.EventBus;

import static android.app.Activity.RESULT_OK;
import static cn.leancloud.chatkit.activity.LCIMUserSelectActivity.KEY_RESULT_DATA;

/**
 * Created by wli on 15/12/4.
 * 联系人页面
 */
public class LCIMContactFragment extends Fragment {

  protected SwipeRefreshLayout refreshLayout;
  protected RecyclerView recyclerView;

  private LCIMMembersAdapter itemAdapter;
  LinearLayoutManager layoutManager;

  private List<LCChatKitUser> specifiedUsers;
  private LCIMCommonListAdapter.ListMode listMode = LCIMCommonListAdapter.ListMode.SHOW_ACTION;
  private Set<LCChatKitUser> selectedUsers = new HashSet<>();

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.contact_fragment, container, false);

    refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.contact_fragment_srl_list);
    recyclerView = (RecyclerView) view.findViewById(R.id.contact_fragment_rv_list);

    layoutManager = new LinearLayoutManager(getActivity());
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.addItemDecoration(new LCIMDividerItemDecoration(getActivity()));
    itemAdapter = new LCIMMembersAdapter();
    recyclerView.setAdapter(itemAdapter);
    refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        refreshMembers();
      }
    });

    EventBus.getDefault().register(this);
    return view;
  }

  @Override
  public void onDestroyView() {
    EventBus.getDefault().unregister(this);
    super.onDestroyView();
  }

  @Override
  public void onResume() {
    super.onResume();
    refreshMembers();
  }

  public void setSpecifiedUsers(List<LCChatKitUser> users) {
    this.specifiedUsers = users;
    itemAdapter.setMemberList(this.specifiedUsers);
  }

  public void setListMode(LCIMCommonListAdapter.ListMode mode) {
    this.listMode = mode;
    itemAdapter.setShowMode(mode);
  }

  private void refreshMembers() {
    if (itemAdapter.getItemCount() < 1) {
      itemAdapter.setMemberList(LCChatKit.getInstance().getProfileProvider().getAllUsers());
    }
    itemAdapter.notifyDataSetChanged();
    refreshLayout.setRefreshing(false);
  }

  @Override
  public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
    if (LCIMCommonListAdapter.ListMode.SELECT == this.listMode) {
      inflater.inflate(R.menu.conv_member_menu, menu);
    }
  }

  @Override
  public boolean onOptionsItemSelected (MenuItem item) {
    if (android.R.id.home == item.getItemId()) {
      getActivity().onBackPressed();
      return true;
    }
    if (item.getItemId() == R.id.menu_conv_member_saving) {

      Intent output = new Intent();
      LCChatKitUser[] users = new LCChatKitUser[this.selectedUsers.size()];
      this.selectedUsers.toArray(users);
      System.out.println("save all changes. users=" + JSON.toJSONString(users));
      output.putExtra(KEY_RESULT_DATA, JSON.toJSONString(users));
      getActivity().setResult(RESULT_OK, output);
      getActivity().finish();
    }
    return super.onOptionsItemSelected(item);
  }
  /**
   * 处理 LetterView 发送过来的 LCIMMemberLetterEvent
   * 会通过 LCIMMembersAdapter 获取应该要跳转到的位置，然后跳转
   */
  public void onEvent(LCIMMemberLetterEvent event) {
    Character targetChar = Character.toLowerCase(event.letter);
    if (itemAdapter.getIndexMap().containsKey(targetChar)) {
      int index = itemAdapter.getIndexMap().get(targetChar);
      if (index > 0 && index < itemAdapter.getItemCount()) {
        layoutManager.scrollToPositionWithOffset(index, 0);
      }
    }
  }

  public void onEvent(LCIMMemberSelectedChangeEvent event) {
    System.out.println("eventHandler. isChecked=" + event.isSelected + ", user=" + event.member);
    if (null != event && null != event.member) {
      if (event.isSelected) {
        this.selectedUsers.add(event.member);
      } else {
        this.selectedUsers.remove(event.member);
      }
    }
  }
}
