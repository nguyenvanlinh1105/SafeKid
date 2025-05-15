package com.example.myapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseBackgroundService extends Service {
    private DatabaseReference heartRateRef, forgotStatusRef;
    private static final String CHANNEL_ID = "FirebaseServiceChannel";
    private boolean listenersRegistered = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, createNotification());
        Log.d("FirebaseService", "Service started...");
        startFirebaseListeners();
    }

    private void startFirebaseListeners() {
        if (listenersRegistered) return;

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        heartRateRef = database.getReference("NhipTim");
        forgotStatusRef = database.getReference("forgot_status");

        heartRateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                String heartRateStr = snapshot.getValue(String.class);
                if (heartRateStr == null) return;

                try {
                    int heartRate = Integer.parseInt(heartRateStr);
                    if (heartRate < 60 || heartRate > 100) {
                        triggerAlarm("Nhịp tim nguy hiểm: " + heartRate + " bpm");
                    }
                } catch (NumberFormatException e) {
                    Log.e("FirebaseService", "Lỗi đọc nhịp tim", e);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("FirebaseService", "Firebase error: " + error.getMessage());
            }
        });

        forgotStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                Long forgotStatus = snapshot.getValue(Long.class);
                if (forgotStatus != null && forgotStatus == 1L) {
                    triggerWarning();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("FirebaseService", "Firebase error: " + error.getMessage());
            }
        });

        listenersRegistered = true;
    }

    private void triggerAlarm(String message) {
        Intent intent = new Intent(this, AlarmService.class);
        intent.putExtra("ALARM_MESSAGE", message);
        startService(intent);
    }

    private void triggerWarning() {
        Intent intent = new Intent(this, WarningService.class);
        startService(intent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Firebase Background Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Ứng dụng đang chạy nền")
                .setContentText("Đang theo dõi dữ liệu từ Firebase...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("FirebaseService", "Service đang chạy foreground...");
        return START_STICKY;
    }
}
