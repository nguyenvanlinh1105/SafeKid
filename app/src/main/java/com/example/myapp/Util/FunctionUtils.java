package com.example.myapp.Util;

import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.widget.EditText;

import com.example.myapp.R;

public class FunctionUtils {
    public FunctionUtils() {
    }

    // Hàm xử lý hiển thị/ẩn mật khẩu
    public static void setupPasswordVisibilityToggle(EditText editText) {
        editText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // Kiểm tra vị trí chạm vào DrawableRight
                if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width())) {
                    if (editText.getTransformationMethod() instanceof PasswordTransformationMethod) {
                        // Hiển thị mật khẩu
                        editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.show_password, 0);
                    } else {
                        // Ẩn mật khẩu
                        editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_hide_password, 0);
                    }
                    // Đặt lại con trỏ ở cuối văn bản
                    editText.setSelection(editText.getText().length());
                    return true;
                }
            }
            return false;
        });
    }
}
