package com.example.myapp;

import android.os.Bundle;
import android.os.Handler;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;

public class frag_gps extends Fragment {

    private WebView webView;
    private Handler handler;
    private Runnable updateRunnable;

    // Firebase reference
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
        webView.setWebViewClient(new WebViewClient());



        webView.addJavascriptInterface(new WebAppInterface(), "Android");

        webView.loadUrl("file:///android_asset/map.html");

        databaseReference = FirebaseDatabase.getInstance().getReference("ToaDo");

        handler = new Handler();
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

   // interface class
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

    // Hàm để ứng dụng truyền dữ liệu vào WebView
    public void setMapPosition(double longitude, double latitude) {
        if (webView != null) {
            webView.evaluateJavascript("updateMapPosition(" + longitude + ", " + latitude + ");", null);
            webView.evaluateJavascript("GetToaDoChiTiet(" + longitude + ", " + latitude + ");", null);
        }
    }

    // Hàm lấy tọa độ từ Firebase
    private void getToaDoFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String longStr = dataSnapshot.child("long").getValue(String.class);
                    String latStr = dataSnapshot.child("lat").getValue(String.class);

                    if (longStr != null && latStr != null) {

                        double longitude = Double.parseDouble(longStr);
                        double latitude = Double.parseDouble(latStr);

                        // Cập nhật vị trí trên WebView
                        setMapPosition(longitude, latitude);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý khi có lỗi trong việc lấy dữ liệu từ Firebase
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webView != null) {
            webView.destroy();
        }

        // Hủy bỏ Handler để tránh rò rỉ bộ nhớ
        if (handler != null && updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
        }
    }
}
