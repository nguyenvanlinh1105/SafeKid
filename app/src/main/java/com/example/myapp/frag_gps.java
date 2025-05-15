package com.example.myapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;

public class frag_gps extends Fragment {

    private WebView webView;
    private Handler handler;
    private ValueEventListener valueEventListener;

    private double lastLongitude = 0;
    private double lastLatitude = 0;
    private String lastAddress = "Đang tải địa chỉ...";
    private long lastTimestamp = 0;

    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;

    private static final double DISTANCE_THRESHOLD = 5;
    private static final long STOP_THRESHOLD = 10 * 60 * 1000;
    private static final long DEBOUNCE_DELAY = 10 * 1000;
    private static final int MAX_API_CALLS_PER_MINUTE = 30;

    private long lastApiCallTime = 0;
    private int apiCallCount = 0;
    private boolean isApiCallPending = false;

    private boolean isFirstLocationLoaded = false;

    public frag_gps() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_frag_gps, container, false);

        webView = view.findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d("WEBVIEW", "Trang map.html đã tải xong");
                setupFirebaseListener();
            }
        });

        webView.addJavascriptInterface(new WebAppInterface(), "Android");
        webView.loadUrl("file:///android_asset/map.html");

        databaseReference = FirebaseDatabase.getInstance().getReference("ToaDo");
        sharedPreferences = requireActivity().getSharedPreferences("LocationCache", Context.MODE_PRIVATE);

        handler = new Handler(Looper.getMainLooper());

        lastLongitude = Double.longBitsToDouble(sharedPreferences.getLong("lastLongitude", Double.doubleToLongBits(0)));
        lastLatitude = Double.longBitsToDouble(sharedPreferences.getLong("lastLatitude", Double.doubleToLongBits(0)));
        lastAddress = sharedPreferences.getString("lastAddress", "Đang tải địa chỉ...");
        Log.d("CACHE", "Địa chỉ từ cache: " + lastAddress);

        if (lastAddress != null && !lastAddress.equals("Đang tải địa chỉ...")) {
            webView.evaluateJavascript("document.getElementById('address-info').innerHTML = '<i class=\\\"fas fa-location-dot\\\"></i> Địa chỉ: " + lastAddress + "';", null);
        } else {
            webView.evaluateJavascript("GetToaDoChiTiet(" + lastLongitude + ", " + lastLatitude + ");", null);
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                apiCallCount = 0;
                Log.d("API_CALL", "Reset bộ đếm API");
                handler.postDelayed(this, 60 * 1000);
            }
        }, 60 * 1000);

        return view;
    }

    private void setupFirebaseListener() {
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Double longitude = dataSnapshot.child("long").getValue(Double.class);
                    Double latitude = dataSnapshot.child("lat").getValue(Double.class);
                    Long timestamp = dataSnapshot.child("timestamp").getValue(Long.class);

                    if (longitude != null && latitude != null && timestamp != null) {
                        Log.d("FIREBASE", "Nhận dữ liệu: long=" + longitude + ", lat=" + latitude + ", timestamp=" + timestamp);
                        setMapPosition(longitude, latitude, timestamp);
                    } else {
                        Log.d("FIREBASE", "Dữ liệu không đầy đủ: " + dataSnapshot.toString());
                        webView.evaluateJavascript("document.getElementById('address-info').innerHTML = '<i class=\\\"fas fa-location-dot\\\"></i> Dữ liệu Firebase không hợp lệ';", null);
                    }
                } else {
                    Log.d("FIREBASE", "Không có dữ liệu từ ToaDo");
                    webView.evaluateJavascript("document.getElementById('address-info').innerHTML = '<i class=\\\"fas fa-location-dot\\\"></i> Không có dữ liệu từ Firebase';", null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FIREBASE", "Lỗi Firebase: " + databaseError.getMessage());
                webView.evaluateJavascript("document.getElementById('address-info').innerHTML = '<i class=\\\"fas fa-location-dot\\\"></i> Lỗi Firebase: " + databaseError.getMessage() + "';", null);
            }
        };

        databaseReference.addValueEventListener(valueEventListener);
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void updatePosition(double longitude, double latitude) {
            if (webView != null) {
                webView.post(() -> {
                    webView.evaluateJavascript("updateMapPosition(" + longitude + ", " + latitude + ");", null);
                });
            }
        }

        @JavascriptInterface
        public void updateAddress(String address) {
            Log.d("CACHE", "Cập nhật địa chỉ: " + address);
            lastAddress = address;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("lastAddress", address);
            editor.putLong("lastLongitude", Double.doubleToLongBits(lastLongitude));
            editor.putLong("lastLatitude", Double.doubleToLongBits(lastLatitude));
            editor.apply();
        }
    }

    public void setMapPosition(double longitude, double latitude, Long timestamp) {
        if (webView == null) return;

        Log.d("SET_MAP", "Cập nhật vị trí: long=" + longitude + ", lat=" + latitude);

        webView.evaluateJavascript("updateMapPosition(" + longitude + ", " + latitude + ");", null);

        long currentTime = System.currentTimeMillis();

        // Nếu timestamp từ tương lai, giới hạn lại để không sai logic
        if (timestamp > currentTime) {
            Log.w("SET_MAP", "Timestamp lớn hơn thời gian hiện tại, chỉnh lại để khớp.");
            timestamp = currentTime;
        }

        long timeDiffSeconds = (currentTime - timestamp) / 1000;
        String stopTimeDisplay;

        long days = 0, hours = 0, minutes = 0;  // Khai báo trước để dùng chung

        if (timeDiffSeconds < 60) {
            stopTimeDisplay = timeDiffSeconds + " giây";
        } else {
            long timeDiffMinutes = timeDiffSeconds / 60;
            days = timeDiffMinutes / 1440;
            long remainingMinutes = timeDiffMinutes % 1440;
            hours = remainingMinutes / 60;
            minutes = remainingMinutes % 60;

            if (days > 0) {
                stopTimeDisplay = days + " ngày " +
                        (hours > 0 ? hours + " giờ " : "") +
                        (minutes > 0 ? minutes + " phút" : "");
            } else if (hours > 0) {
                stopTimeDisplay = hours + " giờ " +
                        (minutes > 0 ? minutes + " phút" : "");
            } else {
                stopTimeDisplay = minutes + " phút";
            }
        }

        Log.d("TIME_DIFF", "days=" + days + ", hours=" + hours + ", minutes=" + minutes + ", seconds=" + timeDiffSeconds);

        webView.evaluateJavascript("document.getElementById('stop-time-info').innerHTML = '<i class=\\\"fas fa-clock\\\"></i> Thời gian dừng lại: " + stopTimeDisplay + "';", null);

        boolean isMoving = (longitude != lastLongitude || latitude != lastLatitude);

        // Cập nhật lần đầu
        if (!isFirstLocationLoaded) {
            Log.d("SET_MAP", "Lần đầu load vị trí -> Cập nhật map, trạng thái và gọi API");
            isFirstLocationLoaded = true;
            webView.evaluateJavascript("GetToaDoChiTiet(" + longitude + ", " + latitude + ");", null);

            if (isMoving) {
                webView.evaluateJavascript("document.getElementById('status-info').innerHTML = '<i class=\\\"fas fa-bus\\\"></i> Trạng thái: Xe đang di chuyển';", null);
            } else {
                webView.evaluateJavascript("document.getElementById('status-info').innerHTML = '<i class=\\\"fas fa-bus\\\"></i> Trạng thái: Xe đang dừng';", null);
            }


            lastLongitude = longitude;
            lastLatitude = latitude;
            lastTimestamp = timestamp;
            return;
        }

        if (isMoving) {
            double distance = calculateDistance(lastLatitude, lastLongitude, latitude, longitude);
            Log.d("SET_MAP", "Xe đang di chuyển, khoảng cách: " + distance + "m");

            if (distance > DISTANCE_THRESHOLD) {
                scheduleApiCall(longitude, latitude);
            } else {
                Log.d("SET_MAP", "Khoảng cách ≤ " + DISTANCE_THRESHOLD + "m, dùng địa chỉ cũ: " + lastAddress);
                webView.evaluateJavascript("document.getElementById('address-info').innerHTML = '<i class=\\\"fas fa-location-dot\\\"></i> Địa chỉ: " + lastAddress + "';", null);
            }

            webView.evaluateJavascript("document.getElementById('status-info').innerHTML = '<i class=\\\"fas fa-bus\\\"></i> Trạng thái: Xe đang di chuyển';", null);
        } else {
            long timeSinceLastMove = currentTime - lastTimestamp;
            Log.d("SET_MAP", "Xe đang dừng, thời gian dừng: " + timeSinceLastMove + "ms");

            if (timeSinceLastMove < STOP_THRESHOLD) {
                webView.evaluateJavascript("document.getElementById('address-info').innerHTML = '<i class=\\\"fas fa-location-dot\\\"></i> Địa chỉ: " + lastAddress + "';", null);
            }

            webView.evaluateJavascript("document.getElementById('status-info').innerHTML = '<i class=\\\"fas fa-bus\\\"></i> Trạng thái: Xe đang dừng';", null);
        }

        lastLongitude = longitude;
        lastLatitude = latitude;
        lastTimestamp = timestamp;
    }



    private void scheduleApiCall(double longitude, double latitude) {
        long currentTime = System.currentTimeMillis();
        if (apiCallCount >= MAX_API_CALLS_PER_MINUTE) {
            Log.d("API_CALL", "Đã vượt giới hạn API: " + apiCallCount);
            webView.evaluateJavascript("document.getElementById('address-info').innerHTML = '<i class=\\\"fas fa-location-dot\\\"></i> Địa chỉ: " + lastAddress + "';", null);
            return;
        }
        if (isApiCallPending) {
            Log.d("API_CALL", "Tọa độ thay đổi liên tục, áp dụng debounce");
            handler.removeCallbacksAndMessages(null);
        }
        Log.d("API_CALL", "Lên lịch gọi API sau " + DEBOUNCE_DELAY + "ms");
        isApiCallPending = true;
        handler.postDelayed(() -> {
            if (apiCallCount < MAX_API_CALLS_PER_MINUTE) {
                Log.d("API_CALL", "Gọi API cho tọa độ: " + longitude + ", " + latitude);
                webView.evaluateJavascript("GetToaDoChiTiet(" + longitude + ", " + latitude + ");", value -> {
                    Log.d("WEBVIEW", "evaluateJavascript callback: " + value);
                });
                apiCallCount++;
                lastApiCallTime = System.currentTimeMillis();
            }
            isApiCallPending = false;
        }, DEBOUNCE_DELAY);
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        if (databaseReference != null && valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
