package com.example.cleansweep;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

public class Register extends AppCompatActivity {

    ImageView profileImage;
    Uri imageUri;

    TextInputEditText edittextusername, edittextemail, edittextpassword;
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

    protected void onActivityResult(int reqcode, int rescode, Intent data) {
        super.onActivityResult(reqcode, rescode, data);
        if (reqcode == 1 && rescode == RESULT_OK && data != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
        gallery.setType("image/*");
        startActivityForResult(gallery, 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(Register.this, Manifest.permission.READ_MEDIA_IMAGES)!= PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(Register.this, Manifest.permission.READ_MEDIA_IMAGES)){
                Toast.makeText(Register.this, "Please accept permissions", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(Register.this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 1);
            }
        } else {
            openGallery();
        }
    }

    private void updateUserInfo(String name, Uri img, FirebaseUser user){
        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("profile_pictures");
        StorageReference imageFilePath = mStorage.child(Objects.requireNonNull(img.getLastPathSegment()));
        imageFilePath.putFile(img).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .setPhotoUri(uri)
                                .build();

                        user.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(Register.this, "Account created.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), Login.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();

        profileImage = findViewById(R.id.registration_profile_image);
        profileImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    checkAndRequestPermissions();
                }
            }
        });

        edittextusername = findViewById(R.id.register_username);
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
                String username, email, password;
                username = String.valueOf(edittextusername.getText());
                email = String.valueOf(edittextemail.getText());
                password = String.valueOf(edittextemail.getText());

                boolean valid = true;

                if (imageUri == null) {
                    Toast.makeText(Register.this, "Select profile image", Toast.LENGTH_SHORT).show();
                    valid = false;
                }
                if (TextUtils.isEmpty(username)) {
                    Toast.makeText(Register.this, "Enter your username", Toast.LENGTH_SHORT).show();
                    valid = false;
                }
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Register.this, "Enter your email", Toast.LENGTH_SHORT).show();
                    valid = false;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Register.this, "Enter your password", Toast.LENGTH_SHORT).show();
                    valid = false;
                }

                if (valid){
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                FirebaseUser currentUser = mAuth.getCurrentUser();
                                updateUserInfo(username, imageUri, currentUser);
                            } else {
                                Toast.makeText(Register.this, "Account creation failed.", Toast.LENGTH_SHORT).show();
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