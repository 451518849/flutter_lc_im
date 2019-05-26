package cn.leancloud.chatkit.activity;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.leancloud.chatkit.LCChatKitUser;
import cn.leancloud.chatkit.R;
import cn.leancloud.chatkit.adapter.LCIMCommonListAdapter;

public class LCIMUserSelectActivity extends AppCompatActivity {
  public static final String KEY_USERS = "users";
  public static final String KEY_TITLE = "title";
  public static final String KEY_RESULT_DATA = "result_data";

  private LCIMContactFragment contactFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.lcim_contact_activity);
    contactFragment = (LCIMContactFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_contact);
    contactFragment.setHasOptionsMenu(true);
    contactFragment.setListMode(LCIMCommonListAdapter.ListMode.SELECT);
    String attachedUsers = getIntent().getStringExtra(KEY_USERS);
    if (null != attachedUsers && attachedUsers.length() > 0) {
      List<LCChatKitUser> users = JSON.parseArray(attachedUsers, LCChatKitUser.class);
      System.out.println("init with specified users: ");
      for (LCChatKitUser m : users) {
        System.out.println("\t" + m.toString());
      }
      contactFragment.setSpecifiedUsers(users);
    }
    String title = getIntent().getStringExtra(KEY_TITLE);
    initActionBar(title);
  }

  /**
   * 设置 actionBar title 以及 up 按钮事件
   *
   * @param title
   */
  protected void initActionBar(String title) {
    ActionBar actionBar = getSupportActionBar();
    if (null != actionBar) {
      if (null != title) {
        actionBar.setTitle(title);
      }
      actionBar.setDisplayUseLogoEnabled(false);
      actionBar.setDisplayHomeAsUpEnabled(true);
      finishActivity(RESULT_OK);
    }
  }


}
