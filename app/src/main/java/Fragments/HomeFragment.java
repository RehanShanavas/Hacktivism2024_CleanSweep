package Fragments;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.cleansweep.Post;
import com.example.cleansweep.R;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import Adapters.PostAdapter;
import Models.PostObject;
import ch.hsr.geohash.GeoHash;

import com.firebase.geofire.GeoLocation;
import com.google.firebase.firestore.QuerySnapshot;

public class HomeFragment extends Fragment {

    private static final int REQUEST_LOCATION = 1;
    private LocationManager locationManager;
    private LocationListener locationListener;


    double latitude;
    double longitude;
    String currentGeoHash = "";
    String currentDate = "";

    CardView addPostButton;
    RecyclerView postrecyclerView;
    PostAdapter postAdapter;
    ProgressBar progressBar;

    FirebaseFirestore db;
    DocumentReference docRef;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupLocationListener();
        checkLocationPermission();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        postrecyclerView = view.findViewById(R.id.post_container);
        postrecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        db = FirebaseFirestore.getInstance();
        docRef = db.collection("posts").document();

        progressBar = view.findViewById(R.id.home_progressBar);
        progressBar.setVisibility(View.VISIBLE);

        addPostButton = view.findViewById(R.id.add_post);
        addPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!currentGeoHash.isEmpty()){
                    Intent intent = new Intent(getActivity(), Post.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), "Please enable location services", Toast.LENGTH_SHORT).show();
                }

            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void setupFirestoreListener() {
        final GeoLocation center = new GeoLocation(latitude, longitude);
        final double radiusInM = 50 * 1000;

        List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM);
        final List<Task<QuerySnapshot>> collection = new ArrayList<>();

        for (GeoQueryBounds b : bounds) {
            Query q = db.collection("posts")
                    .orderBy("geoHashVal")
                    .startAt(b.startHash)
                    .endAt(b.endHash);
            collection.add(q.get());
        }

        Tasks.whenAll(collection).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                List<PostObject> postList = new ArrayList<>();
                for (Task<QuerySnapshot> task : collection) {
                    QuerySnapshot snapshot = task.getResult();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        PostObject post = doc.toObject(PostObject.class);
                        post.setDocId(doc.getId());
                        postList.add(post);
                    }
                }

                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                postAdapter = new PostAdapter(getActivity(), postList, currentUserId, currentGeoHash, currentDate);
                postrecyclerView.setAdapter(postAdapter);
                progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle error
                progressBar.setVisibility(View.GONE);
            }
        });
    }



    private void setupLocationListener() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                currentGeoHash = GeoHash.geoHashStringWithCharacterPrecision(latitude, longitude, 7);
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm");
                currentDate = sdf.format(new Date());
                if (!currentDate.isEmpty()) {
                    locationManager.removeUpdates(this);
                    setupFirestoreListener();
                }
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
            locationManager = (LocationManager) requireActivity().getSystemService(LOCATION_SERVICE);
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            } else {
                requestLocationUpdates();
            }
        }
    }

    private void requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates();
            }
        }
    }
}
