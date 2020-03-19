package com.xiaofa.flutter_lc_im;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import androidx.core.app.NotificationCompat;

public class LCPushService {
    static String channelId;
    static Context context;
    static Activity activity;
    static boolean isOpen;

    public static void setDefaultChannelId(Context context,Activity activity,String channelId){
        LCPushService.context = context;
        LCPushService.activity = activity;
        LCPushService.channelId = channelId;
    }
    public static void sendMessageNotification(String message) {

        NotificationManager manager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(context, activity.getClass());
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setContentTitle("您收到一条新消息")
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(context.getApplicationInfo().icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),context.getApplicationInfo().icon))
                .setAutoCancel(true)
                .build();
        manager.notify(1, notification);
    }
}
