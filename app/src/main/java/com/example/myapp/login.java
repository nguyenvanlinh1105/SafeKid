package com.example.myapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
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

public class login extends AppCompatActivity {

    TextView edit_mail, edit_password,btnLogin_TK,btnDangKiFormLogin ;
    CheckBox cb_nho_mat_khau_login ;
    SharedPreferences sharedPreferences, shareUserResponseLogin;
    private FirebaseAuth mAuth;
    ImageView btn_back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        edit_mail = findViewById(R.id.edtMail_login);
        edit_password = findViewById(R.id.edtPassword_login);
        btn_back =  findViewById(R.id.btn_back);
        btnLogin_TK = findViewById(R.id.btn_Login_TK);
        cb_nho_mat_khau_login = findViewById(R.id.cb_nho_mat_khau_login);
        btnDangKiFormLogin =findViewById(R.id.btnDangKi_formLogin);
        sharedPreferences = getSharedPreferences("dataLogin", MODE_PRIVATE);

        edit_mail.setText(sharedPreferences.getString("email",""));
        edit_password.setText(sharedPreferences.getString("password",""));
        cb_nho_mat_khau_login.setChecked(sharedPreferences.getBoolean("checked",false));


        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent firstpage = new Intent(login.this, First_page.class);
                startActivity(firstpage);
                finish();
            }
        });
        btnDangKiFormLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent formDangKi = new Intent(login.this, Register.class);
                startActivity(formDangKi);
                finish();
            }
        });
        btnLogin_TK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edit_mail.getText().toString().trim();
                String password = edit_password.getText().toString().trim();
                if(cb_nho_mat_khau_login.isChecked()){
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("email", email);
                    editor.putString("password",password);
                    editor.putBoolean("checked", true);
                    editor.apply();
                }
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(login.this, "Vui lòng nhập email và password trước khi nhấn đăng nhập", Toast.LENGTH_SHORT).show();
                } else {
                    // function login
                    signIn(email, password);
                }

            }
        });
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(login.this,
                                "Đăng nhập thành công! Email: " + user.getEmail(),
                                Toast.LENGTH_SHORT).show();

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
