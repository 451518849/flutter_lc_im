package cn.leancloud.chatkit.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by wli on 15/7/29.
 * 关于软键盘的 Util 类
 */
public class LCIMSoftInputUtils {

  /**
   * 如果当前键盘已经显示，则隐藏
   * 如果当前键盘未显示，则显示
   *
   * @param context
   */
  public static void toggleSoftInput(Context context) {
    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
  }

  /**
   * 弹出键盘
   *
   * @param context
   * @param view
   */
  public static void showSoftInput(Context context, View view) {
    if (view != null) {
      InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.showSoftInput(view, 0);
    }
  }

  /**
   * 隐藏键盘
   *
   * @param context
   * @param view
   */
  public static void hideSoftInput(Context context, View view) {
    if (view != null) {
      InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }
}
