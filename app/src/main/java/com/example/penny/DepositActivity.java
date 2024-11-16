package com.example.penny;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class DepositActivity extends BaseActivity {

    private EditText incomeAmount;
    private EditText incomeDescription;
    private Spinner incomeCategory;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize UI elements
        incomeAmount = findViewById(R.id.income_amount);
        incomeDescription = findViewById(R.id.income_description);
        incomeCategory = findViewById(R.id.income_category);
        Button addIncomeButton = findViewById(R.id.add_income_button);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set onClick listener for the button
        addIncomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    saveIncomeToDatabase();
                }
            }
        });
    }

    // Method to validate input
    private boolean validateInput() {
        if (incomeAmount.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter an amount.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (incomeDescription.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter a description.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Method to save income to Firestore
    private void saveIncomeToDatabase() {
        String amount = incomeAmount.getText().toString().trim();
        String description = incomeDescription.getText().toString().trim();
        String category = incomeCategory.getSelectedItem().toString();
        // Get the current user ID from Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        Map<String, Object> income = new HashMap<>();
        income.put("amount", Double.parseDouble(amount));
        income.put("description", description);
        income.put("category", category);
        income.put("timestamp", System.currentTimeMillis());
        income.put("userId", userId); // Include userId field

        db.collection("incomes")
                .add(income)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(DepositActivity.this, "Income added successfully!", Toast.LENGTH_SHORT).show();
                    clearFields();
                    startActivity(new Intent(DepositActivity.this, TransactionsActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DepositActivity.this, "Error adding income: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Method to clear input fields
    private void clearFields() {
        incomeAmount.setText("");
        incomeDescription.setText("");
        incomeCategory.setSelection(0);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_deposit;
    }
}
