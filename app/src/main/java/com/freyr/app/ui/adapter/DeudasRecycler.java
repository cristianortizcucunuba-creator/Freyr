
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

//chiques help no se nada

public class DeudasRecycler extends RecyclerView.Adapter<DeudasRecycler.DebtViewHolder> {

    // Interfaz que define las acciones disponibles al seleccionar una deuda.
    public interface OnDebtClickListener {
        void onDebtClick(Debt debt);
        void onPayClick(Debt debt);
    }

    private final Context context;
    private final OnDebtClickListener listener;

    // Arreglos que almacenan las deudas, pagos, grupos y usuarios.
    // Inicialmente están vacíos y pueden reemplazarse cuando llegan datos.
    private Debt[] debts = new Debt[0];
    private Payment[] allPayments = new Payment[0];
    private FamilyGroup[] allGroups = new FamilyGroup[0];
    private User[] allUsers = new User[0];

    public DeudasRecycler(Context context, OnDebtClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    // Actualiza el arreglo de deudas y refresca la interfaz.
    public void setDebts(Debt[] debts) {
        this.debts = debts != null ? debts : new Debt[0];
        notifyDataSetChanged();
    }

    // Actualiza los pagos disponibles.
    public void setPayments(Payment[] payments) {
        this.allPayments = payments != null ? payments : new Payment[0];
        notifyDataSetChanged();
    }

    // Actualiza los grupos familiares disponibles.
    public void setGroups(FamilyGroup[] groups) {
        this.allGroups = groups != null ? groups : new FamilyGroup[0];
        notifyDataSetChanged();
    }

    // Actualiza los usuarios para mostrar sus nombres actuales.
    public void setUsers(User[] users) {
        this.allUsers = users != null ? users : new User[0];
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DebtViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        // Carga el diseño XML de una tarjeta de deuda.
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_debt, parent, false);

        return new DebtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull DebtViewHolder holder, int position) {

        // Obtiene la deuda correspondiente a esta posición.
        Debt debt = debts[position];

        holder.tvName.setText(debt.getName());

        // Muestra la descripción solamente si contiene texto.
        if (debt.getDescription() != null
                && !debt.getDescription().trim().isEmpty()) {

            holder.tvDesc.setText(debt.getDescription());
            holder.tvDesc.setVisibility(View.VISIBLE);

        } else {
            holder.tvDesc.setVisibility(View.GONE);
        }

        // Construye el texto que identifica el grupo de la deuda.
        StringBuilder scopeText = new StringBuilder();

        if (debt.getFamilyGroupId() != null) {

            String groupName = "Grupo Familiar";

            // Busca el grupo familiar asociado a la deuda.
            for (FamilyGroup group : allGroups) {
                if (group.getId().equals(debt.getFamilyGroupId())) {
                    groupName = group.getName();
                    break;
                }
            }

            scopeText.append("Familia · ").append(groupName);

        } else {
            scopeText.append("Deuda Personal");
        }

        // Busca al usuario responsable de la deuda.
        if (debt.getAssignedUserId() != null) {

            String assignedName = null;

            for (User user : allUsers) {
                if (user.getId().equals(debt.getAssignedUserId())) {
                    assignedName = user.getNomCompl();
                    break;
                }
            }

            if (assignedName != null) {
                scopeText.append(" · Responsable: ").append(assignedName);
            }
        }

        holder.tvScope.setText(scopeText.toString());

        // Suma todos los pagos relacionados con esta deuda.
        double paid = 0.0;

        for (Payment payment : allPayments) {
            if (payment.getDebtId().equals(debt.getId())) {
                paid += payment.getAmount();
            }
        }

        // Calcula cuánto dinero queda pendiente.
        double remaining = Math.max(0.0, debt.getAmount() - paid);

        boolean isPaid = remaining == 0.0
                || "paid".equalsIgnoreCase(debt.getStatus());

        // Presenta los valores monetarios.
        holder.tvTotal.setText(
                FreyrRepository.formatCurrency(debt.getAmount()));

        holder.tvPaid.setText(
                FreyrRepository.formatCurrency(paid));

        holder.tvRemaining.setText(
                FreyrRepository.formatCurrency(remaining));

        // Actualiza el estado y la barra de progreso.
        if (isPaid) {

            holder.tvStatus.setText("Pagado");

            holder.tvStatus.setTextColor(
                    ContextCompat.getColor(
                            context, R.color.freyr_status_paid));

            holder.tvStatus.setBackgroundResource(
                    R.drawable.bg_status_paid);

            holder.btnPay.setVisibility(View.GONE);
            holder.pbProgress.setProgress(100);

        } else {

            holder.tvStatus.setText("Pendiente");

            holder.tvStatus.setTextColor(
                    ContextCompat.getColor(
                            context, R.color.freyr_secondary));

            holder.tvStatus.setBackgroundResource(
                    R.drawable.bg_status_pending);

            holder.btnPay.setVisibility(View.VISIBLE);

            // Calcula el porcentaje pagado y evita dividir entre cero.
            int percent = debt.getAmount() > 0
                    ? (int) Math.min(
                    100, (paid / debt.getAmount()) * 100)
                    : 0;

            holder.pbProgress.setProgress(percent);
        }

        // Abre los detalles cuando se pulsa la tarjeta.
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDebtClick(debt);
            }
        });

        // El botón de historial abre los detalles de la deuda.
        holder.btnHistory.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDebtClick(debt);
            }
        });

        // Solicita registrar un pago.
        holder.btnPay.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPayClick(debt);
            }
        });
    }

    @Override
    public int getItemCount() {
        // Los arreglos utilizan length para indicar su tamaño.
        return debts.length;
    }

    // Conserva las referencias a las vistas de cada tarjeta.
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
