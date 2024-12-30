package com.example.myapp;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

public class frag_dashboard extends Fragment {

    public frag_dashboard() {
        // Required empty public constructor
    }

    public static frag_dashboard newInstance(String param1, String param2) {
        frag_dashboard fragment = new frag_dashboard();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_frag_dashboard, container, false);

        // Tìm ImageView trái tim
        ImageView heartView = rootView.findViewById(R.id.heartImage);

        // Tạo hiệu ứng nhịp đập cho trái tim
        ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(
                heartView,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.2f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.2f, 1f)
        );
        scaleAnimator.setDuration(1000);
        scaleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        scaleAnimator.start();

        // Tìm View vòng tròn
        View circleView = rootView.findViewById(R.id.circularBorder);

        // Tạo hiệu ứng scale cho vòng tròn
        ObjectAnimator circleAnimator = ObjectAnimator.ofPropertyValuesHolder(
                circleView,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.3f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.3f, 1f)
        );
        circleAnimator.setDuration(1200);
        circleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        circleAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        circleAnimator.start();

        return rootView;
    }

}
