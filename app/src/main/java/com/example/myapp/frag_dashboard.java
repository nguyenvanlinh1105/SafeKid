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
    private TextView heartRateText, progressValueNhietDo, textwarningNhietDo,textwarningHeart;
    private LinearLayout pieNhietDo;
    private DatabaseReference heartRateRef, temperatureRef;
    private boolean isAlarmOn = false;
    private View circleView;

    public frag_dashboard() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_frag_dashboard, container, false);

        heartRateText = rootView.findViewById(R.id.heartRateText);
        progressValueNhietDo = rootView.findViewById(R.id.progressValueNhietDo);
        textwarningNhietDo = rootView.findViewById(R.id.textwarningNhietDo);
        pieNhietDo = rootView.findViewById(R.id.pieNhietdo);
        textwarningHeart = rootView.findViewById(R.id.heartRateUnit);
        ImageView heartView = rootView.findViewById(R.id.heartImage);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        heartRateRef = database.getReference("NhipTim");
        temperatureRef = database.getReference("NhietDo");
        ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(
                heartView,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.2f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.2f, 1f)
        );
        scaleAnimator.setDuration(1000);
        scaleAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        scaleAnimator.start();
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
                String heartRateStr = snapshot.getValue(String.class);
                if (heartRateStr == null) return;

                try {
                    int heartRate = Integer.parseInt(heartRateStr);
                    heartRateText.setText(heartRate + " bpm");
                    boolean isDangerous = heartRate < 60 || heartRate > 100;

                    if (isDangerous) {
                        triggerAlarm("Nhịp tim bất thường: " + heartRate + " bpm");
                        textwarningHeart.setText("Nhịp tym ở mức nguy hiểm");
                        textwarningHeart.setTextColor(Color.RED);
                        heartRateText.setTextColor(Color.RED);
                        circleView.setBackgroundResource(R.drawable.circular_border);
                    } else {
                        updateAlarmState(false);
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

        temperatureRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                String tempStr = snapshot.getValue(String.class);
                if (tempStr == null) return;

                try {
                    double tempValue = Double.parseDouble(tempStr);
                    progressValueNhietDo.setText(tempValue + "°C");
                    boolean isDangerous = tempValue < 20 || tempValue > 30;
                    // set màu nguy hiểm cho nhiệt độ
                    GradientDrawable drawable = new GradientDrawable();
                    drawable.setCornerRadius(20);
                    drawable.setShape(GradientDrawable.RECTANGLE);
                    drawable.setStroke(5, Color.BLACK);
                    // drawable.setShadowLayer(10, 0, 0, Color.GRAY);

                    if (isDangerous) {
                        drawable.setColor(Color.RED);
                    } else {
                        drawable.setColor(Color.rgb(0, 255, 156));
                    }
                    pieNhietDo.setBackground(drawable);

                    if (isDangerous) {
                        triggerAlarm("Nhiệt độ bất thường: " + tempValue + "°C");

                        textwarningNhietDo.setText("Nhiệt độ ở mức nguy hiểm");
                    } else {
                        textwarningNhietDo.setText("Nhiệt độ an toàn");
                        updateAlarmState(false);
                    }
                } catch (NumberFormatException e) {
                    progressValueNhietDo.setText("Dữ liệu lỗi");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressValueNhietDo.setText("Lỗi: " + error.getMessage());
            }
        });
    }

    private void updatePieStyle(boolean isDangerous) {

    }

    private void triggerAlarm(String message) {
       // showAlert("Cảnh báo!", message);
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

    private void showAlert(String title, String message) {
        if (getContext() == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    private void requestOverlayPermission() {
        Context context = getContext();
        if (context == null) return;

        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + context.getPackageName()));
        startActivity(intent);
    }
}
