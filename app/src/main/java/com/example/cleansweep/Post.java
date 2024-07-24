package com.example.cleansweep;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import Models.PostObject;

public class Post extends AppCompatActivity {

    EditText postTitle;
    ImageView postImage;
    Uri imageUri;
    EditText startDateTime;
    EditText endDateTime;

    Button confirmPost;
    Button cancelPost;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post);

        postTitle = findViewById(R.id.post_title);
        postImage = findViewById(R.id.post_image);
        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    checkAndRequestPermissions();
                }
            }
        });

        startDateTime = findViewById(R.id.start_date_time);
        endDateTime = findViewById(R.id.end_date_time);

        startDateTime.setOnClickListener(view -> showDateTimePicker(startDateTime));
        endDateTime.setOnClickListener(view -> showDateTimePicker(endDateTime));

        confirmPost = findViewById(R.id.confirm_post);
        confirmPost.setOnClickListener(view -> {
            String title = postTitle.getText().toString();
            String startdate = startDateTime.getText().toString();
            String enddate = endDateTime.getText().toString();

            boolean valid = true;

            if (TextUtils.isEmpty(title)) {
                Toast.makeText(Post.this, "Enter a Title", Toast.LENGTH_SHORT).show();
                valid = false;
            }
            if (imageUri == null) {
                Toast.makeText(Post.this, "Select an Image", Toast.LENGTH_SHORT).show();
                valid = false;
            }
            if (TextUtils.isEmpty(startdate)) {
                Toast.makeText(Post.this, "Enter the Starting Date", Toast.LENGTH_SHORT).show();
                valid = false;
            }
            if (TextUtils.isEmpty(enddate)) {
                Toast.makeText(Post.this, "Enter the Ending Date", Toast.LENGTH_SHORT).show();
                valid = false;
            }

            if (valid) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("post_images");
                StorageReference imageFilePath = storageReference.child(Objects.requireNonNull(imageUri.getLastPathSegment()));
                imageFilePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageDownloadLink = uri.toString();
                                PostObject postObject = new PostObject(
                                        title,
                                        imageDownloadLink,
                                        FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                        FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString(),
                                        startdate,
                                        enddate
                                );
                                addPost(postObject);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Post.this, "Image upload failed.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Post.this, "Image upload failed.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        cancelPost = findViewById(R.id.cancel_post);
        cancelPost.setOnClickListener(view -> {
            Intent intent = new Intent(Post.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
        gallery.setType("image/*");
        startActivityForResult(gallery, 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(Post.this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(Post.this, Manifest.permission.READ_MEDIA_IMAGES)) {
                Toast.makeText(Post.this, "Please accept permissions", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(Post.this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 1);
            }
        } else {
            openGallery();
        }
    }

    private void showDateTimePicker(final EditText editText) {
        final Calendar currentDate = Calendar.getInstance();
        final Calendar date = Calendar.getInstance();
        new DatePickerDialog(Post.this, (view, year, monthOfYear, dayOfMonth) -> {
            date.set(year, monthOfYear, dayOfMonth);
            new TimePickerDialog(Post.this, (view1, hourOfDay, minute) -> {
                date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                date.set(Calendar.MINUTE, minute);
                editText.setText(String.format(Locale.getDefault(), "%02d/%02d/%d %02d:%02d",
                        date.get(Calendar.YEAR),
                        date.get(Calendar.MONTH) + 1,
                        date.get(Calendar.DAY_OF_MONTH),
                        date.get(Calendar.HOUR_OF_DAY),
                        date.get(Calendar.MINUTE)));
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }

    @Override
    protected void onActivityResult(int reqcode, int rescode, Intent data) {
        super.onActivityResult(reqcode, rescode, data);
        if (reqcode == 1 && rescode == RESULT_OK && data != null) {
            imageUri = data.getData();
            postImage.setImageURI(imageUri);
        }
    }

    private void addPost(PostObject postObject){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("posts").document();
        String docId = docRef.getId();
        postObject.setDocId(docId);
        docRef.set(postObject, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Post.this, "Post created.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Post.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(Post.this, "Post creation failed.", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Post.this, "Post creation failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
