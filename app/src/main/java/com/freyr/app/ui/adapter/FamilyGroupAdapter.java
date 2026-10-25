package com.freyr.app.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.freyr.app.R;
import com.freyr.app.data.model.Debt;
import com.freyr.app.data.model.FamilyGroup;
import com.freyr.app.data.model.Payment;
import com.freyr.app.data.repository.FreyrRepository;
import java.util.ArrayList;
import java.util.List;

public class FamilyGroupAdapter extends RecyclerView.Adapter<FamilyGroupAdapter.GroupViewHolder> {

    public interface OnGroupClickListener {
        void onGroupClick(FamilyGroup group);
    }

    private final Context context;
    private List<FamilyGroup> groups = new ArrayList<>();
    private List<Debt> allDebts = new ArrayList<>();
    private List<Payment> allPayments = new ArrayList<>();
    private String currentUserId = "";
    private final OnGroupClickListener listener;

    public FamilyGroupAdapter(Context context, OnGroupClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setGroups(List<FamilyGroup> groups) {
        this.groups = groups != null ? groups : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setDebts(List<Debt> debts) {
        this.allDebts = debts != null ? debts : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setPayments(List<Payment> payments) {
        this.allPayments = payments != null ? payments : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId != null ? currentUserId : "";
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_family_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        FamilyGroup group = groups.get(position);
        holder.tvName.setText(group.getName());

        boolean isCreator = group.getCreatorId() != null && group.getCreatorId().equals(currentUserId);
        holder.tvAdmin.setVisibility(isCreator ? View.VISIBLE : View.GONE);

        // Group debts count & figures
        int debtsCount = 0;
        double totalDebt = 0.0;
        double totalPaid = 0.0;

        for (Debt d : allDebts) {
            if (group.getId().equals(d.getFamilyGroupId())) {
                debtsCount++;
                totalDebt += d.getAmount();
                for (Payment p : allPayments) {
                    if (p.getDebtId().equals(d.getId())) {
                        totalPaid += p.getAmount();
                    }
                }
            }
        }
        double remaining = Math.max(0.0, totalDebt - totalPaid);

        int memberCount = group.getMemberIds() != null ? group.getMemberIds().size() : 0;
        holder.tvMembersCount.setText(memberCount + (memberCount == 1 ? " miembro" : " miembros") + " · " + debtsCount + (debtsCount == 1 ? " deuda familiar" : " deudas familiares"));

        holder.tvTotal.setText(FreyrRepository.formatCurrency(totalDebt));
        holder.tvPaid.setText(FreyrRepository.formatCurrency(totalPaid));
        holder.tvRemaining.setText(FreyrRepository.formatCurrency(remaining));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onGroupClick(group);
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAdmin, tvMembersCount;
        TextView tvTotal, tvPaid, tvRemaining;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_group_name);
            tvAdmin = itemView.findViewById(R.id.tv_admin_badge);
            tvMembersCount = itemView.findViewById(R.id.tv_group_members_count);
            tvTotal = itemView.findViewById(R.id.tv_group_total_debt);
            tvPaid = itemView.findViewById(R.id.tv_group_total_paid);
            tvRemaining = itemView.findViewById(R.id.tv_group_total_remaining);
        }
    }
}
