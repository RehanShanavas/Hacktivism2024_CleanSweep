package com.example.cleansweep;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Register extends AppCompatActivity {

    TextInputEditText edittextemail, edittextpassword;
    Button buttonregister;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;

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
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        edittextemail = findViewById(R.id.register_email);
        edittextpassword = findViewById(R.id.register_password);
        buttonregister = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.register_progressBar);
        textView = findViewById(R.id.login_link);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        buttonregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String email, password;
                email = String.valueOf(edittextemail.getText());
                password = String.valueOf(edittextemail.getText());

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Register.this, "Enter your email", Toast.LENGTH_SHORT).show();
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Register.this, "Enter your password", Toast.LENGTH_SHORT).show();
                }

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
//                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(Register.this, "Account created.", Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(Register.this, "Account creation failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}