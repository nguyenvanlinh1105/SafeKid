package com.example.myapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;

public class PopupActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hiển thị popup trên màn hình khóa
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );

        setContentView(R.layout.popup_alarm);

        // Nút tắt popup và dừng service
        Button btnOk = findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(v -> {
            stopAlarmService(); // Dừng service (tắt âm thanh)
            finish();           // Đóng popup
        });
    }

    private void stopAlarmService() {
        stopService(new Intent(this, AlarmService.class));
    }
}
