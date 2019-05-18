package cn.leancloud.chatkit.utils;

import android.util.Log;

import com.avos.avoscloud.AVOSCloud;

/**
 * Created by wli on 16/4/6.
 */
public class LCIMLogUtils {
  public static final String LOGTAG = "LCChatKit";
  public static boolean debugEnabled = false;

  static {
    debugEnabled = AVOSCloud.isDebugLogEnabled();
  }

  public LCIMLogUtils() {
  }

  private static String getDebugInfo() {
    Throwable stack = new Throwable().fillInStackTrace();
    StackTraceElement[] trace = stack.getStackTrace();
    int n = 2;
    return trace[n].getClassName() + " " + trace[n].getMethodName() + "()" + ":" + trace[n].getLineNumber() +
      " ";
  }

  private static String getLogInfoByArray(String[] infos) {
    StringBuilder sb = new StringBuilder();
    for (String info : infos) {
      sb.append(info);
      sb.append(" ");
    }
    return sb.toString();
  }

  public static void i(String... s) {
    if (debugEnabled) {
      Log.i(LOGTAG, getDebugInfo() + getLogInfoByArray(s));
    }
  }

  public static void e(String... s) {
    if (debugEnabled) {
      Log.e(LOGTAG, getDebugInfo() + getLogInfoByArray(s));
    }
  }

  public static void d(String... s) {
    if (debugEnabled) {
      Log.d(LOGTAG, getDebugInfo() + getLogInfoByArray(s));
    }
  }

  public static void v(String... s) {
    if (debugEnabled) {
      Log.v(LOGTAG, getDebugInfo() + getLogInfoByArray(s));
    }
  }

  public static void w(String... s) {
    if (debugEnabled) {
      Log.w(LOGTAG, getDebugInfo() + getLogInfoByArray(s));
    }
  }

  public static void logException(Throwable tr) {
    if (debugEnabled) {
      Log.e(LOGTAG, getDebugInfo(), tr);
    }
  }
}
