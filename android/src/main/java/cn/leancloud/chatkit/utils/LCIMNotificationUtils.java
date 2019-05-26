package cn.leancloud.chatkit.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.LinkedList;
import java.util.List;

import android.support.v4.app.NotificationCompat;

/**
 * Created by wli on 15/8/26.
 * Notification 相关的 Util
 */
public class LCIMNotificationUtils {

  private static int lastNotificationId = 0;

  /**
   * tag list，用来标记是否应该展示 Notification
   * 比如已经在聊天页面了，实际就不应该再弹出 notification
   */
  private static List<String> notificationTagList = new LinkedList<String>();

  /**
   * 添加 tag 到 tag list，在 MessageHandler 弹出 notification 前会判断是否与此 tag 相等
   * 若相等，则不弹，反之，则弹出
   *
   * @param tag
   */
  public static void addTag(String tag) {
    if (!notificationTagList.contains(tag)) {
      notificationTagList.add(tag);
    }
  }

  /**
   * 在 tag list 中 remove 该 tag
   *
   * @param tag
   */
  public static void removeTag(String tag) {
    notificationTagList.remove(tag);
  }

  /**
   * 判断是否应该弹出 notification
   * 判断标准是该 tag 是否包含在 tag list 中
   *
   * @param tag
   * @return
   */
  public static boolean isShowNotification(String tag) {
    return !notificationTagList.contains(tag);
  }

  public static void showNotification(Context context, String title, String content, Intent intent) {
    showNotification(context, title, content, null, intent);
  }

  public static void showNotification(Context context, String title, String content, String sound, Intent intent) {
    PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
        .setSmallIcon(context.getApplicationInfo().icon)
        .setContentTitle(title).setAutoCancel(true).setContentIntent(contentIntent)
        .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
        .setContentText(content);
    NotificationManager manager =
      (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    Notification notification = mBuilder.build();
    if (sound != null && sound.trim().length() > 0) {
      notification.sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + sound);
    }
    lastNotificationId = (lastNotificationId > 10000 ? 0 : lastNotificationId + 1);
    manager.notify(lastNotificationId, notification);
  }
}
