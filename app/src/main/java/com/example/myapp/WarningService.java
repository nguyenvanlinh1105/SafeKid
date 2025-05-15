package com.example.myapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class WarningService extends Service {
    private MediaPlayer mediaPlayer;
    private static final String CHANNEL_ID = "WarningServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, getNotification()); // Chạy foreground service
        startAlarm();
        showPopup();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Warning Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private Notification getNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Báo động cảnh báo phát hiện để quên trẻ trên xe")
                .setContentText("Nhấn để tắt báo động")
                .setSmallIcon(R.drawable.bg_app)
                .build();
    }

    private void startAlarm() {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        }
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.baodong));
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
    }

    private void showPopup() {
        Intent intent = new Intent(this, PopupWarningActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void stopAlarm() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            Log.d("WarningService", "Received action: " + action);
            if ("STOP_ALARM".equals(action)) {
                stopAlarm();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAlarm(); // Đảm bảo tắt khi service bị kill
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
