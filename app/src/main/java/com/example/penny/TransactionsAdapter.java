package com.example.penny;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder> {

    private List<TransactionsActivity.Transaction> transactions;
    private OnItemClickListener onItemClickListener;

    public TransactionsAdapter(List<TransactionsActivity.Transaction> transactions, OnItemClickListener onItemClickListener) {
        this.transactions = transactions;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionsActivity.Transaction transaction = transactions.get(position);
        String date = new SimpleDateFormat("MMM dd", Locale.getDefault()).format(new Date(transaction.getTimestamp()));
        String symbol = transaction.getType().equals("income") ? "➕" : "➖";
        String description = transaction.getDesc().length() > 8 ? transaction.getDesc().substring(0, 8) + ".." : transaction.getDesc();

        String displayText = symbol + " $" + String.format(Locale.getDefault(), "%.2f", transaction.getAmount())
                + " (" + transaction.getCategory() + ") " + description + " | " + date;

        holder.transactionTextView.setText(displayText);

        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(position));
        holder.itemView.setOnLongClickListener(v -> {
            onItemClickListener.onItemLongClick(position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView transactionTextView;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionTextView = itemView.findViewById(android.R.id.text1);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }
}
