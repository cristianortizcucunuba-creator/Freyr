package com.freyr.app.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.freyr.app.R;
import com.freyr.app.data.model.Debt;
import com.freyr.app.data.model.FamilyGroup;
import com.freyr.app.data.model.Payment;
import com.freyr.app.data.model.User;
import com.freyr.app.data.repository.FreyrRepository;
import java.util.ArrayList;
import java.util.List;

public class DebtAdapter extends RecyclerView.Adapter<DebtAdapter.DebtViewHolder> {

    public interface OnDebtClickListener {
        void onDebtClick(Debt debt);
        void onPayClick(Debt debt);
    }

    private final Context context;
    private List<Debt> debts = new ArrayList<>();
    private List<Payment> allPayments = new ArrayList<>();
    private List<FamilyGroup> allGroups = new ArrayList<>();
    private List<User> allUsers = new ArrayList<>();
    private final OnDebtClickListener listener;

    public DebtAdapter(Context context, OnDebtClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setDebts(List<Debt> debts) {
        this.debts = debts != null ? debts : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setPayments(List<Payment> payments) {
        this.allPayments = payments != null ? payments : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setGroups(List<FamilyGroup> groups) {
        this.allGroups = groups != null ? groups : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Resuelve el problema donde al cambiar el nombre de un usuario en su perfil,
     * no se reflejaba en las deudas existentes. Al vincular la lista viva de usuarios,
     * siempre se busca dinámicamente el nombre más actualizado del usuario asignado.
     */
    public void setUsers(List<User> users) {
        this.allUsers = users != null ? users : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DebtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_debt, parent, false);
        return new DebtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DebtViewHolder holder, int position) {
        Debt debt = debts.get(position);
        holder.tvName.setText(debt.getName());

        if (debt.getDescription() != null && !debt.getDescription().trim().isEmpty()) {
            holder.tvDesc.setText(debt.getDescription());
            holder.tvDesc.setVisibility(View.VISIBLE);
        } else {
            holder.tvDesc.setVisibility(View.GONE);
        }

        // Ámbito y Responsable dinámico
        StringBuilder scopeText = new StringBuilder();
        if (debt.getFamilyGroupId() != null) {
            String groupName = "Grupo Familiar";
            for (FamilyGroup g : allGroups) {
                if (g.getId().equals(debt.getFamilyGroupId())) {
                    groupName = g.getName();
                    break;
                }
            }
            scopeText.append("Familia · ").append(groupName);
        } else {
            scopeText.append("Deuda Personal");
        }

        // Buscar el nombre del usuario asignado dinámicamente desde allUsers
        if (debt.getAssignedUserId() != null) {
            String assignedName = null;
            for (User u : allUsers) {
                if (u.getId().equals(debt.getAssignedUserId())) {
                    assignedName = u.getFullName();
                    break;
                }
            }
            if (assignedName != null) {
                scopeText.append(" · Responsable: ").append(assignedName);
            }
        }

        holder.tvScope.setText(scopeText.toString());

        // Calcular monto pagado
        double paid = 0.0;
        for (Payment p : allPayments) {
            if (p.getDebtId().equals(debt.getId())) {
                paid += p.getAmount();
            }
        }
        double remaining = Math.max(0.0, debt.getAmount() - paid);
        boolean isPaid = remaining == 0.0 || "paid".equalsIgnoreCase(debt.getStatus());

        holder.tvTotal.setText(FreyrRepository.formatCurrency(debt.getAmount()));
        holder.tvPaid.setText(FreyrRepository.formatCurrency(paid));
        holder.tvRemaining.setText(FreyrRepository.formatCurrency(remaining));

        if (isPaid) {
            holder.tvStatus.setText("Pagado");
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.freyr_status_paid));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_paid);
            holder.btnPay.setVisibility(View.GONE);
            holder.pbProgress.setProgress(100);
        } else {
            holder.tvStatus.setText("Pendiente");
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.freyr_secondary));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
            holder.btnPay.setVisibility(View.VISIBLE);
            int percent = debt.getAmount() > 0 ? (int) Math.min(100, (paid / debt.getAmount()) * 100) : 0;
            holder.pbProgress.setProgress(percent);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDebtClick(debt);
        });

        holder.btnHistory.setOnClickListener(v -> {
            if (listener != null) listener.onDebtClick(debt);
        });

        holder.btnPay.setOnClickListener(v -> {
            if (listener != null) listener.onPayClick(debt);
        });
    }

    @Override
    public int getItemCount() {
        return debts.size();
    }

    static class DebtViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvScope, tvDesc, tvStatus;
        TextView tvTotal, tvPaid, tvRemaining;
        ProgressBar pbProgress;
        Button btnHistory, btnPay;

        public DebtViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_debt_name);
            tvScope = itemView.findViewById(R.id.tv_debt_scope);
            tvDesc = itemView.findViewById(R.id.tv_debt_desc);
            tvStatus = itemView.findViewById(R.id.tv_debt_status);
            tvTotal = itemView.findViewById(R.id.tv_debt_total);
            tvPaid = itemView.findViewById(R.id.tv_debt_paid);
            tvRemaining = itemView.findViewById(R.id.tv_debt_remaining);
            pbProgress = itemView.findViewById(R.id.pb_debt_progress);
            btnHistory = itemView.findViewById(R.id.btn_debt_history);
            btnPay = itemView.findViewById(R.id.btn_debt_pay);
        }
    }
}
