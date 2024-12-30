package com.example.myapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapp.Util.FunctionUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.myapp.MainActivity2;

public class login extends AppCompatActivity {

    // Khai báo các thành phần giao diện và biến cần thiết
    private TextView edit_mail, btnLogin_TK, btnDangKiFormLogin;
    private EditText edit_password;
    private CheckBox cb_nho_mat_khau_login;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private FunctionUtils functionUtils;
    private ImageView btn_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bật chế độ EdgeToEdge và đặt giao diện
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Thiết lập Padding cho layout chính để tránh bị che khuất
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo đối tượng FunctionUtils để hỗ trợ xử lý mật khẩu
        functionUtils = new FunctionUtils();

        // Ánh xạ các thành phần giao diện
        edit_mail = findViewById(R.id.edtMail_login);
        edit_password = findViewById(R.id.edtPassword_login);
        btn_back = findViewById(R.id.btn_back);
        btnLogin_TK = findViewById(R.id.btn_Login_TK);
        cb_nho_mat_khau_login = findViewById(R.id.cb_nho_mat_khau_login);
        btnDangKiFormLogin = findViewById(R.id.btnDangKi_formLogin);

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences("dataLogin", MODE_PRIVATE);

        // Lấy dữ liệu từ SharedPreferences và hiển thị
        edit_mail.setText(sharedPreferences.getString("email", ""));
        edit_password.setText(sharedPreferences.getString("password", ""));
        cb_nho_mat_khau_login.setChecked(sharedPreferences.getBoolean("checked", false));

        // Thiết lập chức năng ẩn/hiện mật khẩu
        functionUtils.setupPasswordVisibilityToggle(edit_password);

        // Xử lý sự kiện nút quay lại
        btn_back.setOnClickListener(view -> {
            Intent firstPage = new Intent(login.this, com.example.myapp.First_page.class);
            startActivity(firstPage);
            finish();
        });

        // Xử lý sự kiện nút chuyển sang form đăng ký
        btnDangKiFormLogin.setOnClickListener(view -> {
            Intent formDangKi = new Intent(login.this, Register.class);
            startActivity(formDangKi);
            finish();
        });

        // Xử lý sự kiện nút đăng nhập
        btnLogin_TK.setOnClickListener(view -> {
            String email = edit_mail.getText().toString().trim();
            String password = edit_password.getText().toString().trim();

            // Lưu thông tin nếu checkbox được chọn
            if (cb_nho_mat_khau_login.isChecked()) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("email", email);
                editor.putString("password", password);
                editor.putBoolean("checked", true);
                editor.apply();
            }

            // Kiểm tra email và password hợp lệ
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(login.this, "Vui lòng nhập email và mật khẩu trước khi nhấn đăng nhập", Toast.LENGTH_SHORT).show();
            } else {
                // Thực hiện chức năng đăng nhập
                signIn(email, password);
            }
        });
    }

    /**
     * Hàm xử lý đăng nhập với FirebaseAuth
     * @param email Email người dùng nhập vào
     * @param password Mật khẩu người dùng nhập vào
     */
    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Đăng nhập thành công
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(login.this,
                                    "Đăng nhập thành công! Email: " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();

                            // Chuyển sang màn hình chính
                            Intent home = new Intent(login.this, MainActivity2.class);
                            startActivity(home);
                            finish();
                        } else {
                            // Đăng nhập thất bại
                            Toast.makeText(login.this,
                                    "Đăng nhập thất bại: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
