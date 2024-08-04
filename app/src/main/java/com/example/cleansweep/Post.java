package com.example.cleansweep;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

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
import ch.hsr.geohash.GeoHash;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Post extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 1;
    private LocationManager locationManager;
    private LocationListener locationListener;

    double latitude;
    double longitude;

    EditText postTitle;
    ImageView postImage;
    Uri imageUri;
    TextView postLocation;
    EditText startDateTime;
    EditText endDateTime;
    ProgressBar progressBar;

    Button confirmPost;
    Button cancelPost;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));

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

        postLocation = findViewById(R.id.post_location);
        setupLocationListener();
        checkLocationPermission();

        startDateTime = findViewById(R.id.start_date_time);
        endDateTime = findViewById(R.id.end_date_time);

        startDateTime.setOnClickListener(view -> showDateTimePicker(startDateTime));
        endDateTime.setOnClickListener(view -> showDateTimePicker(endDateTime));

        progressBar = findViewById(R.id.add_post_progressBar);

        confirmPost = findViewById(R.id.confirm_post);
        confirmPost.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            String title = postTitle.getText().toString();
            String startdate = startDateTime.getText().toString();
            String enddate = endDateTime.getText().toString();
            String latitudestr = String.valueOf(latitude);
            String longitudestr = String.valueOf(longitude);
            String locationaddress = getLocationDetails(latitude, longitude);
            String geohash = GeoHash.geoHashStringWithCharacterPrecision(latitude, longitude, 7);


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
            if (TextUtils.isEmpty(locationaddress)) {
                Toast.makeText(Post.this, "Location not found", Toast.LENGTH_SHORT).show();
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
                                String documentId = FirebaseFirestore.getInstance().collection("posts").document().getId();
                                String imageDownloadLink = uri.toString();
                                PostObject postObject = new PostObject(
                                        title,
                                        imageDownloadLink,
                                        FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                                        FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                        FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString(),
                                        latitudestr,
                                        longitudestr,
                                        locationaddress,
                                        geohash,
                                        startdate,
                                        enddate,
                                        documentId
                                );
                                addPost(postObject);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Post.this, "Image upload failed.", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Post.this, "Image upload failed.", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
            } else {
                progressBar.setVisibility(View.GONE);
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

    private String getLocationDetails(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String country = address.getCountryName();
                String city = address.getLocality();
                String addressLine = address.getAddressLine(0);

                String returnval = country + ", " + city + ", " + addressLine;
                return returnval;
            }
        } catch (IOException e) {
            // Handle the exception
        }
        return "";
    }

    private void setupLocationListener() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
//                latitude = 37.764832;
//                longitude = -121.84930;

                postLocation.setText(getLocationDetails(latitude, longitude));
                locationManager.removeUpdates(this);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(@NonNull String provider) {}

            @Override
            public void onProviderDisabled(@NonNull String provider) {}
        };
    }

    private void checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            } else {
                requestLocationUpdates();
            }
        }
    }

    private void requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
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

    private void addPost(PostObject postObject) {
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
                progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Post.this, "Post creation failed.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
