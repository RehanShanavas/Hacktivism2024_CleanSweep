package com.example.cleansweep;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import Fragments.HomeFragment;
import Fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity implements ProfileFragment.OnProfileUpdatedListener {

    FirebaseAuth mAuth;
    FirebaseUser user;

    TextView Title;
    ImageView burgerBtn;
    ImageView refreshButton;

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;

    ImageView userImage;
    TextView username;
    TextView userEmail;

    LinearLayout homeFrame;
    LinearLayout profileFrame;
    LinearLayout logoutFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));

        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        homeFrame = findViewById(R.id.nav_home_btn);

        profileFrame = findViewById(R.id.nav_profile_btn);

        logoutFrame = findViewById(R.id.nav_logout_btn);

        Title = findViewById(R.id.title_bar);
        burgerBtn = findViewById(R.id.burger_btn);
        refreshButton = findViewById(R.id.refresh_btn);
        drawerLayout = findViewById(R.id.menu_drawer);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        userImage = findViewById(R.id.user_image);
        username = findViewById(R.id.user_name);
        userEmail = findViewById(R.id.user_email);
        user = mAuth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            username.setText(Objects.requireNonNull(user.getDisplayName()));
            userEmail.setText(Objects.requireNonNull(user.getEmail()));
            Glide.with(this).load(user.getPhotoUrl()).into(userImage);
        }

        burgerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_content, new HomeFragment()).commit();
                Title.setText(R.string.home);
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        homeFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_content, new HomeFragment()).commit();
                Title.setText(R.string.home);
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });
        profileFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_content, new ProfileFragment()).commit();
                Title.setText(R.string.profile);
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });
        logoutFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
//        INITIALIZE WITH HOME FRAGMENT
        getSupportFragmentManager().beginTransaction().replace(R.id.main_content, new HomeFragment()).commit();
        Title.setText(R.string.home);
    }

    @Override
    public void onProfileUpdated(String username, Uri photoUri) {
        // Update UI elements with new profile data
        if (username != null) {
            this.username.setText(username);
        }
        if (photoUri != null) {
            Glide.with(this).load(photoUri).into(userImage);
        }
    }
}
