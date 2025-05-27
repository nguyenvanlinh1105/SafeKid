package com.example.myapp;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class frag_dashboard extends Fragment {
    private TextView heartRateText, text_warning_forgot, textwarningforgot_status, textwarningHeart;
    private LinearLayout pieForgot;
    private DatabaseReference heartRateRef, forgot_status;
    private boolean isAlarmOn = false;
    private boolean isWarningOn = false;
    private View circleView;
    private ImageView image_forgot;

    public frag_dashboard() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_frag_dashboard, container, false);

        // Map view
        heartRateText = rootView.findViewById(R.id.heartRateText);
        text_warning_forgot = rootView.findViewById(R.id.text_warning_forgot);
        textwarningforgot_status = rootView.findViewById(R.id.textwarningforgot_status);
        pieForgot = rootView.findViewById(R.id.pieForgot);
        textwarningHeart = rootView.findViewById(R.id.heartRateUnit);
        ImageView heartView = rootView.findViewById(R.id.heartImage);
//        image_forgot = rootView.findViewById(R.id.image_forgot);

        // Firebase reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        heartRateRef = database.getReference("NhipTim");
        forgot_status = database.getReference("forgot_status");

        // Heart animation
        ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(
                heartView,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.2f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.2f, 1f)
        );
        scaleAnimator.setDuration(1000);
        scaleAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        scaleAnimator.start();

        // Circle animation
        circleView = rootView.findViewById(R.id.circularBorder);
        ObjectAnimator circleAnimator = ObjectAnimator.ofPropertyValuesHolder(
                circleView,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.3f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.3f, 1f)
        );
        circleAnimator.setDuration(1200);
        circleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        circleAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        circleAnimator.start();

        startFirebaseListeners();
        return rootView;
    }

    private void startFirebaseListeners() {
        heartRateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                Double heartRateStr = snapshot.getValue(Double.class);
                if (heartRateStr == null) return;

                try {
                    int heartRate = heartRateStr.intValue();
                    heartRateText.setText(heartRate + " bpm");
                    boolean isDangerous = heartRate < 60 || heartRate > 100;

                    if (isDangerous) {
                        triggerAlarm("Nhịp tim bất thường: " + heartRate + " bpm");
                        textwarningHeart.setText("Nhịp tim ở mức nguy hiểm");
                        textwarningHeart.setTextColor(Color.RED);
                        heartRateText.setTextColor(Color.RED);
                        circleView.setBackgroundResource(R.drawable.circular_border);
                    } else {
                        updateAlarmState(false);
                        textwarningHeart.setText("Nhịp tim an toàn");
                        textwarningHeart.setTextColor(Color.rgb(48, 104, 61));
                        heartRateText.setTextColor(Color.rgb(48, 104, 61));
                        circleView.setBackgroundResource(R.drawable.heart_safe);
                    }
                } catch (NumberFormatException e) {
                    heartRateText.setText("Dữ liệu lỗi");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                heartRateText.setText("Lỗi: " + error.getMessage());
            }
        });

        forgot_status.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                Long forgotStatusValue = snapshot.getValue(Long.class);
                if (forgotStatusValue == null) return;
                boolean forgotStatus = (forgotStatusValue == 1);

                GradientDrawable drawable = new GradientDrawable();
                drawable.setCornerRadius(20);
                drawable.setShape(GradientDrawable.RECTANGLE);

                if (forgotStatus) {
                    drawable.setColor(Color.RED);
                    pieForgot.setBackground(drawable);
                    triggerWarning("Phát hiện bỏ quên trẻ trên xe");
                 //   image_forgot.setColorFilter(Color.WHITE);
                    textwarningforgot_status.setText("Phát hiện bỏ quên trẻ trên xe");
                    text_warning_forgot.setText("THÔNG BÁO KHẨN CẤP!");
                    text_warning_forgot.setTextColor(Color.WHITE);
                    textwarningforgot_status.setTextColor(Color.WHITE);

                } else {
                    drawable.setColor(Color.rgb(59, 247, 178));
                    pieForgot.setBackground(drawable);
                    textwarningforgot_status.setText("AN TOÀN!");
                    text_warning_forgot.setText("KHÔNG PHÁT HIỆN NGUY HIỂM!");
                    text_warning_forgot.setTextColor(Color.BLACK);
                    textwarningforgot_status.setTextColor(Color.BLACK);
                    updateWarningState(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                textwarningforgot_status.setText("Lỗi: " + error.getMessage());
            }
        });
    }



    private void triggerAlarm(String message) {
        updateAlarmState(true);
    }

    private void updateAlarmState(boolean shouldRing) {
        if (shouldRing == isAlarmOn) return;
        isAlarmOn = shouldRing;
        if (shouldRing) {
            startAlarmService();
        } else {
            stopAlarmService();
        }
    }

    private void startAlarmService() {
        Context context = getContext();
        if (context == null) return;

        if (!Settings.canDrawOverlays(context)) {
            requestOverlayPermission();
            return;
        }

        Intent intent = new Intent(context, AlarmService.class);
        context.startService(intent);
    }

    private void stopAlarmService() {
        Context context = getContext();
        if (context == null) return;

        Intent intent = new Intent(context, AlarmService.class);
        context.stopService(intent);
    }

    private void triggerWarning(String message) {
        updateWarningState(true);
    }

    private void updateWarningState(boolean shouldWarn) {
        if (shouldWarn == isWarningOn) return;
        isWarningOn = shouldWarn;
        if (shouldWarn) {
            startWarningService();
        } else {
            stopWarningService();
        }
    }

    private void startWarningService() {
        Context context = getContext();
        if (context == null) return;

        if (!Settings.canDrawOverlays(context)) {
            requestOverlayPermission();
            return;
        }

        Intent intent = new Intent(context, WarningService.class);
        context.startService(intent);
    }

    private void stopWarningService() {
        Context context = getContext();
        if (context == null) return;

        Intent intent = new Intent(context, WarningService.class);
        context.stopService(intent);
    }

    private void requestOverlayPermission() {
        Context context = getContext();
        if (context == null) return;

        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + context.getPackageName()));
        startActivity(intent);
    }
}
