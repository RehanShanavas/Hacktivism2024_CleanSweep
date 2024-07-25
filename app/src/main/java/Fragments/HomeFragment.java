package Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.cleansweep.Post;
import com.example.cleansweep.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

import Adapters.PostAdapter;
import Models.PostObject;

public class HomeFragment extends Fragment {

    CardView addPostButton;
    RecyclerView postrecyclerView;
    PostAdapter postAdapter;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        postrecyclerView = view.findViewById(R.id.post_container);
        postrecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        db = FirebaseFirestore.getInstance();
        docRef = db.collection("posts").document();



        addPostButton = view.findViewById(R.id.add_post);
        addPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), Post.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        db.collection("posts").addSnapshotListener(new EventListener<com.google.firebase.firestore.QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable com.google.firebase.firestore.QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    // Handle error
                    return;
                }

                if (value != null) {
                    List<PostObject> postList = new ArrayList<>();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        PostObject post = doc.toObject(PostObject.class);
                        postList.add(post);
                    }
                    // Initialize the adapter with context and data
                    postAdapter = new PostAdapter(getActivity(), postList);
                    postrecyclerView.setAdapter(postAdapter);
                }
            }
        });
    }
}