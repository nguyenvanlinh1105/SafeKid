package com.example.myapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;

import com.google.firebase.database.FirebaseDatabase;

public class PopupWarningActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Giữ popup luôn hiện trên màn hình khóa, không bị ẩn khi nhấn ra ngoài
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |   // Cho phép nhận sự kiện chạm bên ngoài
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN    // Giúp hiển thị fullscreen chuẩn
        );

        setContentView(R.layout.popup_warning);

        // Gắn hiệu ứng cho layout popup
        View popupLayout = findViewById(R.id.popup_layout);
        popupLayout.setAlpha(0f);
        popupLayout.setScaleX(0.8f);
        popupLayout.setScaleY(0.8f);

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
            stopWarningService();      // Dừng service và âm thanh
            finish();                  // Đóng popup
        });
    }

    // Chặn nút Back
    @Override
    public void onBackPressed() {
        // Không làm gì để chặn đóng popup bằng nút back
    }

    // Chặn tương tác ngoài layout
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Trả về true để chặn mọi chạm ngoài layout
        return true;
    }

    private void stopWarningService() {
        stopService(new Intent(this, WarningService.class));
    }
}
