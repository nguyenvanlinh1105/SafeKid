package com.example.myapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Register extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private ImageView btn_back;
    private TextView edt_Password_signin, edt_Mail_signin, edt_Confirm_Password_signin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        sharedPreferences = getSharedPreferences("dataLogin", MODE_PRIVATE);
        edt_Mail_signin = findViewById(R.id.edt_Mail_signin);
        edt_Password_signin = findViewById(R.id.edt_Password_signin);
        edt_Confirm_Password_signin = findViewById(R.id.edt_confirmPassword_signin);
        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent firstpage = new Intent(Register.this, First_page.class);
                startActivity(firstpage);
                finish();
            }
        });

        TextView btnDangNhap_formRegister = findViewById(R.id.btnDangNhap_formRegister);
        btnDangNhap_formRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent login = new Intent(Register.this, login.class);
                startActivity(login);
                finish();
            }
        });


        TextView btnDangKi = findViewById(R.id.btn_DangKi_TK);
        btnDangKi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edt_Mail_signin.getText().toString().trim();
                String confirmpass = edt_Confirm_Password_signin.getText().toString().trim();
                String password = edt_Password_signin.getText().toString().trim();

                // Kiểm tra email
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(Register.this, "Email không đúng định dạng!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Kiểm tra mật khẩu và xác nhận mật khẩu
                if (!password.equals(confirmpass)) {
                    Toast.makeText(Register.this, "Mật khẩu và Xác nhận mật khẩu không khớp!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(Register.this, "Mật khẩu phải chứa ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show();
                    return;
                }
                signUp(email, password);
            }
        });


    }

    private void signUp(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("email", email);
                        editor.putString("password",password);
                        editor.putBoolean("checked", true);
                        editor.apply();

                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(Register.this,
                                "Đăng ký thành công! Email: " + user.getEmail(),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Register.this,
                                "Đăng ký thất bại: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

}