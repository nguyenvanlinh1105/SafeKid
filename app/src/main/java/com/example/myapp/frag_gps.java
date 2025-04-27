package com.example.myapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;

public class frag_gps extends Fragment {

    private WebView webView;
    private Handler handler;
    private Runnable updateRunnable;

    private double lastLongitude = 0;
    private double lastLatitude = 0;

    private DatabaseReference databaseReference;

    public frag_gps() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_frag_gps, container, false);

        webView = view.findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true); // Hỗ trợ DOM storage cho WebView
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                getToaDoFirebase();
            }
        });

        webView.addJavascriptInterface(new WebAppInterface(), "Android");

        webView.loadUrl("file:///android_asset/map.html");

        databaseReference = FirebaseDatabase.getInstance().getReference("ToaDo");

        handler = new Handler(Looper.getMainLooper());
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                getToaDoFirebase();
                handler.postDelayed(this, 3000);
            }
        };

        handler.post(updateRunnable);

        return view;
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
    }

    public void setMapPosition(double longitude, double latitude) {
        if (webView != null) {
            webView.evaluateJavascript("updateMapPosition(" + longitude + ", " + latitude + ");", null);

            // Chỉ gọi OpenCage API khi tọa độ thay đổi
            if (longitude != lastLongitude || latitude != lastLatitude) {
                webView.evaluateJavascript("GetToaDoChiTiet(" + longitude + ", " + latitude + ");", null);
                // Sử dụng innerHTML để chèn HTML, và escape đúng dấu nháy kép
                webView.evaluateJavascript("document.getElementById('status-info').innerHTML = '<i class=\\\"fas fa-bus\\\"></i> Trạng thái: Xe đang di chuyển';", null);
            } else {
                webView.evaluateJavascript("document.getElementById('status-info').innerHTML = '<i class=\\\"fas fa-bus\\\"></i> Trạng thái: Xe đang dừng';", null);
            }

            lastLongitude = longitude;
            lastLatitude = latitude;
        }
    }

    private void getToaDoFirebase() {
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DataSnapshot dataSnapshot = task.getResult();
                Double longitude = dataSnapshot.child("long").getValue(Double.class);
                Double latitude = dataSnapshot.child("lat").getValue(Double.class);

                if (longitude != null && latitude != null) {
                    try {
                       // double longitude = Double.parseDouble(longStr);
                       // double latitude = Double.parseDouble(latStr);
                        setMapPosition(longitude, latitude);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webView != null) {
            webView.destroy();
            webView = null;
        }

        if (handler != null && updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
        }
    }
}