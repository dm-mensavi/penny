package com.example.penny;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.api.ApiException;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private ProgressBar progressBar; // For showing loading status

    // Use ActivityResultLauncher instead of deprecated startActivityForResult()
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleSignInResult(task);
                    } else {
                        progressBar.setVisibility(View.GONE); // Hide the spinner in case of failure
                    }
                } else {
                    progressBar.setVisibility(View.GONE); // Hide the spinner in case of failure
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Google Sign-In Options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Find the Google Sign-In button and set an OnClickListener
        SignInButton googleSignInButton = findViewById(R.id.googleSignInButton);
        googleSignInButton.setOnClickListener(view -> signIn());

        // Initialize the ProgressBar
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE); // Initially hidden
    }

    private void signIn() {
        // Show loading spinner when sign-in starts
        progressBar.setVisibility(View.VISIBLE);

        // Launch the Google Sign-In intent using ActivityResultLauncher
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void handleSignInResult(@NonNull Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Sign-in success, store user data
            String email = account.getEmail();
            String displayName = account.getDisplayName();
            String photoUrl = (account.getPhotoUrl() != null) ? account.getPhotoUrl().toString() : "";

            storeUserData(email, displayName, photoUrl);

            // Hide loading spinner
            progressBar.setVisibility(View.GONE);

            // Navigate to Home Page with user data
            navigateToHomePage(displayName, photoUrl);
        } catch (ApiException e) {
            // Sign-in failure, hide the spinner and show error message
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Sign-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void storeUserData(String email, String displayName, String photoUrl) {
        SharedPreferences sharedPreferences = getSharedPreferences("PennyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userEmail", email);
        editor.putString("userDisplayName", displayName);
        editor.putString("userPhotoUrl", photoUrl);
        editor.apply(); // Save the changes
    }

    private void navigateToHomePage(String displayName, String photoUrl) {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.putExtra("userName", displayName);
        intent.putExtra("userPhotoUrl", photoUrl);
        startActivity(intent);
        finish(); // Finish LoginActivity to prevent going back
    }
}
