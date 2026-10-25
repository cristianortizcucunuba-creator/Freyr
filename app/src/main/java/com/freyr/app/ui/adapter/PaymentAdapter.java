package com.freyr.app.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.freyr.app.R;
import com.freyr.app.data.model.Payment;
import com.freyr.app.data.model.User;
import com.freyr.app.data.repository.FreyrRepository;
import java.util.ArrayList;
import java.util.List;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder> {

    public interface OnDeletePaymentListener {
        void onDeletePayment(Payment payment);
    }

    private final Context context;
    private List<Payment> payments = new ArrayList<>();
    private List<User> allUsers = new ArrayList<>();
    private final OnDeletePaymentListener deleteListener;

    public PaymentAdapter(Context context, OnDeletePaymentListener deleteListener) {
        this.context = context;
        this.deleteListener = deleteListener;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments != null ? payments : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setUsers(List<User> users) {
        this.allUsers = users != null ? users : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_payment, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Payment payment = payments.get(position);
        holder.tvAmount.setText(FreyrRepository.formatCurrency(payment.getAmount()));

        String userName = "Usuario";
        for (User u : allUsers) {
            if (u.getId().equals(payment.getUserId())) {
                userName = u.getFullName();
                break;
            }
        }
        holder.tvInfo.setText("Pagado por " + userName + " · " + payment.getDate());

        if (payment.getDescription() != null && !payment.getDescription().trim().isEmpty()) {
            holder.tvDesc.setText("\"" + payment.getDescription() + "\"");
            holder.tvDesc.setVisibility(View.VISIBLE);
        } else {
            holder.tvDesc.setVisibility(View.GONE);
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDeletePayment(payment);
        });
    }

    @Override
    public int getItemCount() {
        return payments.size();
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView tvAmount, tvInfo, tvDesc;
        ImageButton btnDelete;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tv_payment_amount);
            tvInfo = itemView.findViewById(R.id.tv_payment_info);
            tvDesc = itemView.findViewById(R.id.tv_payment_desc);
            btnDelete = itemView.findViewById(R.id.btn_delete_payment);
        }
    }
}
