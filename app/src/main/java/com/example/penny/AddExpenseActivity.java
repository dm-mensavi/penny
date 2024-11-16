package com.example.penny;

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

public class AddExpenseActivity extends BaseActivity {

    private EditText expenseAmount;
    private EditText expenseDescription;
    private Spinner expenseCategory;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize UI elements
        expenseAmount = findViewById(R.id.expense_amount);
        expenseDescription = findViewById(R.id.expense_description);
        expenseCategory = findViewById(R.id.expense_category);
        Button addExpenseButton = findViewById(R.id.add_expense_button);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set onClick listener for the button
        addExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    saveExpenseToFirestore();
                }
            }
        });
    }

    // Method to validate input
    private boolean validateInput() {
        if (expenseAmount.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter an amount.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (expenseDescription.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter a description.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Method to save expense data to Firestore
    private void saveExpenseToFirestore() {
        String amount = expenseAmount.getText().toString().trim();
        String description = expenseDescription.getText().toString().trim();
        String category = expenseCategory.getSelectedItem().toString();
        // Get the current user ID from Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        Map<String, Object> expense = new HashMap<>();
        expense.put("amount", Double.parseDouble(amount));
        expense.put("description", description);
        expense.put("category", category);
        expense.put("timestamp", System.currentTimeMillis());
        expense.put("userId", userId); // Include userId field

        db.collection("expenses")
                .add(expense)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddExpenseActivity.this, "Expense added successfully!", Toast.LENGTH_SHORT).show();
                    clearFields();
                    startActivity(new Intent(AddExpenseActivity.this, TransactionsActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddExpenseActivity.this, "Failed to add expense.", Toast.LENGTH_SHORT).show();
                });
    }

    // Method to clear input fields
    private void clearFields() {
        expenseAmount.setText("");
        expenseDescription.setText("");
        expenseCategory.setSelection(0);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_add_expense;
    }
}
