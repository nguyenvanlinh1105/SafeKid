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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapp.Util.FunctionUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class login extends AppCompatActivity {

    private EditText edit_mail, edit_password;
    private TextView btnLogin_TK, btnDangKiFormLogin,btn_QuenMatKhau;
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
        btn_QuenMatKhau = findViewById(R.id.btn_QuenMatKhau);
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
//        btnDangKiFormLogin.setOnClickListener(view -> {
//            Intent formDangKi = new Intent(login.this, Register.class);
//            startActivity(formDangKi);
//            finish();
//        });
        // xử lí lấy lại mật khẩu
        btn_QuenMatKhau.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(login.this);
            builder.setTitle("Quên mật khẩu?");
            builder.setMessage("Nếu bạn quên mật khẩu, vui lòng liên hệ với admin qua email:\n22115053122225@sv.ute.udn.vn");

            builder.setPositiveButton("OK", (dialog, which) -> {
                dialog.dismiss();
            });

            AlertDialog dialog = builder.create();
            dialog.show();
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

            loginWithDatabase(email, password);
        });
    }

    /**
     * Hàm đăng nhập Firebase với xử lý lỗi cụ thể
     */
    private void loginWithDatabase(String email, String password) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;

                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String userEmail = userSnap.child("email").getValue(String.class);
                    String userPassword = userSnap.child("password").getValue(String.class);
                    String userStatus = userSnap.child("status").getValue(String.class);

                    if (userEmail != null && userEmail.equals(email)) {
                        found = true;

                        if (!password.equals(userPassword)) {
                            Toast.makeText(login.this, "Sai mật khẩu", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (userStatus != null && userStatus.equalsIgnoreCase("active")) {
                            Toast.makeText(login.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                            // Chuyển sang MainActivity2
                            startActivity(new Intent(login.this, MainActivity2.class));
                            finish();
                        } else {
                            Toast.makeText(login.this, "Tài khoản đã bị vô hiệu hóa", Toast.LENGTH_SHORT).show();
                        }

                        break;
                    }
                }

                if (!found) {
                    Toast.makeText(login.this, "Tài khoản không tồn tại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(login.this, "Lỗi truy vấn: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
