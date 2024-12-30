package com.example.myapp.Util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.myapp.R;

public class NotificationHelper {

    private Context context;

    public NotificationHelper(Context context) {
        this.context = context;
    }

    public void sendNotification(String tilte, String content) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bg_app);

        String channelId = "default_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Default Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(tilte)
                .setContentText(content)
                .setSmallIcon(R.drawable.bg_app)
                .setLargeIcon(bitmap)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {

            int notificationId = (int) System.currentTimeMillis();
            notificationManager.notify(notificationId, notification);
        }
    }
}