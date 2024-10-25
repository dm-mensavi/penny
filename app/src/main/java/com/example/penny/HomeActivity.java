package com.example.penny;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class HomeActivity extends AppCompatActivity {

    private TextView userNameTextView;
    private ImageView userImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Find the views
        userNameTextView = findViewById(R.id.userNameTextView);
        userImageView = findViewById(R.id.userImageView);

        // Get the user data from the intent
        String userName = getIntent().getStringExtra("userName");
        String userPhotoUrl = getIntent().getStringExtra("userPhotoUrl");

        // Set the user name
        userNameTextView.setText(userName);

        // Load the user image using Glide (you need to add Glide dependency in your build.gradle)
        Glide.with(this).load(userPhotoUrl).placeholder(R.drawable.logo).into(userImageView);
    }
}
