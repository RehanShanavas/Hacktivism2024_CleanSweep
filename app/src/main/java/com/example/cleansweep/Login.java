package com.example.cleansweep;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    TextInputEditText edittextemail, edittextpassword;
    Button buttonlogin;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    Button buttonRegisterLink;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));

        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        edittextemail = findViewById(R.id.login_email);
        edittextpassword = findViewById(R.id.login_password);
        buttonlogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.login_progressBar);
        buttonRegisterLink = findViewById(R.id.register_link);
        buttonRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
                finish();
            }
        });

        buttonlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String email, password;
                email = String.valueOf(edittextemail.getText());
                password = String.valueOf(edittextemail.getText());

                boolean valid = true;

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Login.this, "Enter your email", Toast.LENGTH_SHORT).show();
                    valid = false;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Login.this, "Enter your password", Toast.LENGTH_SHORT).show();
                    valid = false;
                }

                if (valid){
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Login.this, "Login successful.", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(Login.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
}