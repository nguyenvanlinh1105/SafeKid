package com.example.myapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;

import com.google.firebase.database.FirebaseDatabase;

public class PopupWarningActivity extends Activity {
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

        setContentView(R.layout.popup_warning);

        // Gắn hiệu ứng cho layout popup
        View popupLayout = findViewById(R.id.popup_layout);
        popupLayout.setAlpha(0f);
        popupLayout.setScaleX(0.8f);
        popupLayout.setScaleY(0.8f);

        // Hiệu ứng scale và fade in
        popupLayout.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(450)
                .setInterpolator(new OvershootInterpolator())
                .start();

        // Nút tắt popup và dừng âm thanh
        Button btnOk = findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(v -> {
            resetForgotStatus();       // Đặt lại trạng thái Firebase
            stopWarningService();      // Dừng service và âm thanh
            finish();                  // Đóng popup
        });
    }

    private void stopWarningService() {
        stopService(new Intent(this, WarningService.class)); // Dừng service thay vì gửi STOP_ALARM
    }

    private void resetForgotStatus() {
        FirebaseDatabase.getInstance()
                .getReference("forgot_status")
                .setValue(0L); // Reset về 0
    }
}
