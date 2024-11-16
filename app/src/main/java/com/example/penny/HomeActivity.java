package com.example.penny;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends BaseActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Initialize Google Auth
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // OAuth 2.0 Client ID from google-services.json
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Display a welcome message
        TextView welcomeText = findViewById(R.id.welcome_text);
        ImageView profileImageView = findViewById(R.id.profile_image); // Ensure you have this ImageView in your XML layout

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            String photoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;

            // Set welcome text
            welcomeText.setText("Welcome, " + displayName + "!");

            // Load the user's profile image using Glide
            if (photoUrl != null) {
                Glide.with(this).load(photoUrl).into(profileImageView);
            }
        }

        // Handle logout button
        Button logoutButton = findViewById(R.id.logout);
        logoutButton.setOnClickListener(view -> {
            mAuth.signOut();
            // Google sign out
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                // After logging out from Google, redirect to LoginActivity
                Toast.makeText(HomeActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                finish();
            });
        });
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_home;
    }
}
