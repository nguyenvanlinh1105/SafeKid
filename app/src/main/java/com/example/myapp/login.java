package com.example.myapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
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
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

public class login extends AppCompatActivity {

    private EditText edit_mail, edit_password;
    private TextView btnLogin_TK, btnDangKiFormLogin;
    private CheckBox cb_nho_mat_khau_login;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private FunctionUtils functionUtils;
    private ImageView btn_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        functionUtils = new FunctionUtils();

        // Ánh xạ View
        edit_mail = findViewById(R.id.edtMail_login);  // Sửa lại từ TextView thành EditText
        edit_password = findViewById(R.id.edtPassword_login);
        btn_back = findViewById(R.id.btn_back);
        btnLogin_TK = findViewById(R.id.btn_Login_TK);
        cb_nho_mat_khau_login = findViewById(R.id.cb_nho_mat_khau_login);
        btnDangKiFormLogin = findViewById(R.id.btnDangKi_formLogin);

        sharedPreferences = getSharedPreferences("dataLogin", MODE_PRIVATE);

        // Lấy dữ liệu SharedPreferences
        edit_mail.setText(sharedPreferences.getString("email", ""));
        edit_password.setText(sharedPreferences.getString("password", ""));
        cb_nho_mat_khau_login.setChecked(sharedPreferences.getBoolean("checked", false));

        functionUtils.setupPasswordVisibilityToggle(edit_password);

        // xu li back vè trang truoc
        btn_back.setOnClickListener(view -> {
            Intent firstPage = new Intent(login.this, com.example.myapp.First_page.class);
            startActivity(firstPage);
            finish();
        });

        // xu li dang ki
        btnDangKiFormLogin.setOnClickListener(view -> {
            Intent formDangKi = new Intent(login.this, Register.class);
            startActivity(formDangKi);
            finish();
        });

        // xu li dang nhap
        btnLogin_TK.setOnClickListener(view -> {
            String email = edit_mail.getText().toString().trim();
            String password = edit_password.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(login.this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(login.this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (cb_nho_mat_khau_login.isChecked()) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("email", email);
                editor.putString("password", password);
                editor.putBoolean("checked", true);
                editor.apply();
            }

            signIn(email, password);
        });
    }

    /**
     * Hàm đăng nhập Firebase với xử lý lỗi cụ thể
     */
    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                Toast.makeText(login.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(login.this, MainActivity2.class));
                                finish();
                            }
                        } else {
                            handleFirebaseAuthError(task.getException());
                        }
                    }
                });
    }

    /**
     * Xử lý lỗi Firebase cụ thể
     */
    private void handleFirebaseAuthError(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidUserException) {
            Toast.makeText(login.this, "Tài khoản không tồn tại hoặc đã bị vô hiệu hóa", Toast.LENGTH_LONG).show();
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(login.this, "Sai mật khẩu, vui lòng thử lại", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(login.this, "Lỗi: " + exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
