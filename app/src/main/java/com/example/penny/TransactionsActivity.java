package com.example.penny;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class TransactionsActivity extends BaseActivity {

    private RecyclerView transactionsRecyclerView;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<Transaction> transactions = new ArrayList<>();
    private List<String> transactionDocumentIds = new ArrayList<>();
    private TransactionsAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        transactionsRecyclerView = findViewById(R.id.transactions_recycler_view);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setupRecyclerView();
        fetchTransactionsFromFirestore();
    }

    private void setupRecyclerView() {
        adapter = new TransactionsAdapter(transactions, new TransactionsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                editTransaction(position);  // Opens the edit dialog
            }

            @Override
            public void onItemLongClick(int position) {
                new AlertDialog.Builder(TransactionsActivity.this)
                        .setTitle("Delete Transaction")
                        .setMessage("Are you sure you want to delete this transaction?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            String transactionId = transactionDocumentIds.get(position);
                            deleteTransaction(transactionId);
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionsRecyclerView.setAdapter(adapter);
    }

    private void fetchTransactionsFromFirestore() {
        FirebaseUser mUser = mAuth.getCurrentUser();
        if (mUser == null) {
            Toast.makeText(this, "You need to login.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String userId = mUser.getUid();
        retrieveTransactions("incomes", userId, "income");
        retrieveTransactions("expenses", userId, "expense");
    }

    private void retrieveTransactions(String collection, String userId, String type) {
        db.collection(collection)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            double amount = document.getDouble("amount") != null ? document.getDouble("amount") : 0.0;
                            long timestamp = document.getLong("timestamp") != null ? document.getLong("timestamp") : 0;
                            String category = document.getString("category");
                            String desc = document.getString("description");

                            transactions.add(new Transaction(document.getId(), amount, timestamp, category, type, desc));
                            transactionDocumentIds.add(document.getId());
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to load transactions from " + collection + ".", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void editTransaction(int position) {
        Transaction transaction = transactions.get(position);
        String transactionId = transactionDocumentIds.get(position);

        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_transaction, null);
        EditText amountInput = dialogView.findViewById(R.id.amount_input);
        EditText categoryInput = dialogView.findViewById(R.id.category_input);
        EditText descriptionInput = dialogView.findViewById(R.id.description_input);

        // Pre-fill with existing data
        amountInput.setText(String.valueOf(transaction.getAmount()));
        categoryInput.setText(transaction.getCategory());
        descriptionInput.setText(transaction.getDesc());

        // Show dialog
        new AlertDialog.Builder(this)
                .setTitle("Edit Transaction")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    // Retrieve updated values
                    double updatedAmount = Double.parseDouble(amountInput.getText().toString());
                    String updatedCategory = categoryInput.getText().toString();
                    String updatedDesc = descriptionInput.getText().toString();

                    // Update in Firestore
                    updateTransaction(transactionId, updatedAmount, updatedCategory, updatedDesc);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateTransaction(String transactionId, double amount, String category, String description) {
        String collection = transactions.get(transactionDocumentIds.indexOf(transactionId)).getType().equals("income") ? "incomes" : "expenses";

        db.collection(collection).document(transactionId)
                .update("amount", amount, "category", category, "description", description)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Transaction updated successfully.", Toast.LENGTH_SHORT).show();
                    refreshActivityAfterEdit();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update transaction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteTransaction(String transactionId) {
        db.collection("incomes").document(transactionId).delete()
                .addOnSuccessListener(aVoid -> refreshActivityAfterDelete())
                .addOnFailureListener(e -> {
                    db.collection("expenses").document(transactionId).delete()
                            .addOnSuccessListener(aVoid -> refreshActivityAfterDelete())
                            .addOnFailureListener(ee -> {
                                Toast.makeText(this, "Failed to delete transaction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                });
    }

    private void refreshActivityAfterEdit() {
        startActivity(new Intent(this, TransactionsActivity.class));
        finish();
    }

    private void refreshActivityAfterDelete() {
        Toast.makeText(this, "Transaction deleted successfully.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, SummaryActivity.class));
        finish();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_transactions;
    }

    public static class Transaction {
        private final String id;
        private final double amount;
        private final long timestamp;
        private final String category;
        private final String type;
        private final String description;

        public Transaction(String id, double amount, long timestamp, String category, String type, String desc) {
            this.id = id;
            this.amount = amount;
            this.timestamp = timestamp;
            this.category = category;
            this.type = type;
            this.description = desc;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public double getAmount() {
            return amount;
        }

        public String getCategory() {
            return category;
        }

        public String getType() {
            return type;
        }

        public String getDesc() {
            return description;
        }
    }
}
