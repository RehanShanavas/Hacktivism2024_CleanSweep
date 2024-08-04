package Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.cleansweep.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfileFragment extends Fragment {

    public interface OnProfileUpdatedListener {
        void onProfileUpdated(String username, Uri photoUri);
    }
    private OnProfileUpdatedListener callback;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            callback = (OnProfileUpdatedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnProfileUpdatedListener");
        }
    }

    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseAuth mAuth;
    private StorageReference mStorage;

    TextView Title;

    ImageView updateImage;
    EditText updateUsername;
    ProgressBar progressBar;
    Button btnUpdate;
    Button btnCancel;
    Uri imageUri;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        assert getActivity() != null;
        Title = getActivity().findViewById(R.id.title_bar);
        updateImage = view.findViewById(R.id.update_image);
        updateUsername = view.findViewById(R.id.update_profile_username);
        progressBar = view.findViewById(R.id.update_profile_progressBar);
        btnUpdate = view.findViewById(R.id.btn_update);
        btnCancel = view.findViewById(R.id.btn_cancel_update);

        loadUserProfile();

        updateImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserProfile();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // go back to home fragment
                Title.setText("Home");
                assert getActivity() != null;
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_content, new HomeFragment()).commit();
            }
        });

        return view;
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String username = user.getDisplayName();
            Uri profileImageUrl = user.getPhotoUrl();

            updateUsername.setText(username);
            if (profileImageUrl != null) {
                Glide.with(this).load(profileImageUrl).into(updateImage);
            }
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(updateImage);
        }
    }

    private void updateUserProfile() {
        progressBar.setVisibility(View.VISIBLE);
        final FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            final String username = updateUsername.getText().toString().trim();

            if (imageUri != null) {
                final StorageReference fileReference = mStorage.child("profile_pictures").child(user.getUid() + ".jpg");
                fileReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            fileReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        Uri downloadUri = task.getResult();
                                        updateFirebaseUserProfile(username, downloadUri);
                                    } else {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(getActivity(), "Failed to get profile image URL.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getActivity(), "Profile image upload failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                updateFirebaseUserProfile(username, user.getPhotoUrl());
            }
        }
    }

    private void updateFirebaseUserProfile(String username, Uri photoUri) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.updateProfile(new UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .setPhotoUri(photoUri)
                    .build()
            ).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                        // Notify the activity about the update
                        if (callback != null) {
                            callback.onProfileUpdated(username, photoUri);
                        }
                        Title.setText("Home");
                        assert getActivity() != null;
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_content, new HomeFragment()).commit();
                    } else {
                        Toast.makeText(getActivity(), "Profile update failed.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
