package com.example.penny;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ir.mahozad.android.PieChart;
import ir.mahozad.android.PieChart.Slice;
import android.graphics.Typeface;
import androidx.core.content.ContextCompat;

import ir.mahozad.android.PieChart;

public class SummaryActivity extends BaseActivity {

    private TextView totalIncome;
    private TextView totalExpenses;
    private TextView balance;
    private PieChart pieChartView;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize UI elements
        totalIncome = findViewById(R.id.summary_total_income);
        totalExpenses = findViewById(R.id.summary_total_expenses);
        balance = findViewById(R.id.summary_balance);
        pieChartView = findViewById(R.id.pie_chart_view);
        mAuth = FirebaseAuth.getInstance();
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Fetch data from Firestore and display summary
        fetchIncomeAndExpenseData();
    }

    private void fetchIncomeAndExpenseData() {
        FirebaseUser mUser = mAuth.getCurrentUser();
        String userId;
        if(mUser != null){
            userId = mUser.getUid();
        } else{
            Toast.makeText(SummaryActivity.this, "You need to login.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SummaryActivity.this, LoginActivity.class));
            finish();
            return;
        }

        db.collection("incomes").whereEqualTo("userId", userId).get().addOnCompleteListener(incomeTask -> {
            if (incomeTask.isSuccessful()) {
                double totalIncomeValue = 0.0;
                for (QueryDocumentSnapshot document : incomeTask.getResult()) {
                    Double amountValue = document.getDouble("amount");
                    totalIncomeValue += (amountValue != null) ? amountValue : 0.0;
                }

                double finalTotalIncomeValue = totalIncomeValue;
                db.collection("expenses").whereEqualTo("userId", userId).get().addOnCompleteListener(expenseTask -> {
                    if (expenseTask.isSuccessful()) {
                        double totalExpensesValue = 0.0;
                        Map<String, Float> categoryTotals = new HashMap<>();

                        for (QueryDocumentSnapshot document : expenseTask.getResult()) {
                            String category = document.getString("category");
                            Double amountValue = document.getDouble("amount");
                            float amount = (amountValue != null) ? amountValue.floatValue() : 0.0f;

                            totalExpensesValue += amount;

                            // Sum amounts by category
                            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0f) + amount);
                        }

                        // Create slices from aggregated category totals
                        List<Slice> slices = new ArrayList<>();
                        for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
                            String category = entry.getKey();
                            float amount = entry.getValue();
                            slices.add(new Slice(
                                    amount / (float) totalExpensesValue, // Fraction calculation
                                    getColorForCategory(category),
                                    getColorForCategory(category),
                                    category,
                                    null,
                                    0.0f,
                                    Typeface.DEFAULT_BOLD,
                                    null,
                                    null,
                                    null,
                                    null,
                                    0.0f,
                                    PieChart.IconPlacement.BOTTOM,
                                    null,
                                    null,
                                    category,
                                    getColorForCategory(category),
                                    40.0f,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    8.0f,
                                    1.0f,
                                    1.0f,
                                    1.0f
                            ));
                        }

                        double balanceValue = finalTotalIncomeValue - totalExpensesValue;
                        totalIncome.setText("Total Income: $" + String.format(Locale.getDefault(), "%.2f", finalTotalIncomeValue));
                        totalExpenses.setText("Total Expenses: $" + String.format(Locale.getDefault(), "%.2f", totalExpensesValue));
                        balance.setText("Balance: $" + String.format(Locale.getDefault(), "%.2f", balanceValue));

                        // Set up PieChart
                        pieChartView.setSlices(slices);
                        pieChartView.setLegendEnabled(true);
                        pieChartView.setLabelType(PieChart.LabelType.OUTSIDE);
                    } else {
                        Toast.makeText(SummaryActivity.this, "Failed to load expense data.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(SummaryActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(SummaryActivity.this, "Failed to load income data.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(SummaryActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


    // Helper method to assign colors for categories
    private int getColorForCategory(String category) {
        switch (category != null ? category.toLowerCase() : "") {
            case "groceries":
                return getResources().getColor(android.R.color.holo_purple);
            case "transportation":
                return getResources().getColor(android.R.color.holo_green_dark);
            case "utilities":
                return getResources().getColor(android.R.color.holo_blue_light);
            case "entertainment":
                return getResources().getColor(android.R.color.holo_green_light);
            case "rent":
                return getResources().getColor(android.R.color.holo_orange_light);
            case "medical":
                return getResources().getColor(android.R.color.holo_red_light);
            case "miscellaneous":
                return getResources().getColor(android.R.color.holo_blue_dark);
            default:
                return getResources().getColor(android.R.color.black);
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_summary;
    }
}
