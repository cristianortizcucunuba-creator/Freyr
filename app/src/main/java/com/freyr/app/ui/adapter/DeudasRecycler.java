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
import com.freyr.app.data.model.Deuda;
import com.freyr.app.data.model.Grupofamiliar;
import com.freyr.app.data.model.Pago;
import com.freyr.app.data.model.Usuario;
import com.freyr.app.data.repository.FreyrRepository;

public class DeudasRecycler extends RecyclerView.Adapter<DeudasRecycler.DebtViewHolder> {

    // Interfaz que define las acciones disponibles al seleccionar una deuda.
    public interface OnDebtClickListener {
        void onDebtClick(Deuda debt);
        void onPayClick(Deuda debt);
    }

    private final Context context;
    private final OnDebtClickListener listener;

    // Arreglos que almacenan las deudas, pagos, grupos y usuarios.
    // Inicialmente están vacíos y pueden reemplazarse cuando llegan datos.
    private Deuda[] debts = new Deuda[0];
    private Pago[] allPayments = new Pago[0];
    private Grupofamiliar[] allGroups = new Grupofamiliar[0];
    private Usuario[] allUsers = new Usuario[0];

    public DeudasRecycler(Context context, OnDebtClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    // Actualiza el arreglo de deudas y refresca la interfaz.
    public void setDebts(Deuda[] debts) {
        this.debts = debts != null ? debts : new Deuda[0];
        notifyDataSetChanged();
    }

    // Actualiza los pagos disponibles.
    public void setPayments(Pago[] payments) {
        this.allPayments = payments != null ? payments : new Pago[0];
        notifyDataSetChanged();
    }

    // Actualiza los grupos familiares disponibles.
    public void setGroups(Grupofamiliar[] groups) {
        this.allGroups = groups != null ? groups : new Grupofamiliar[0];
        notifyDataSetChanged();
    }

    // Actualiza los usuarios para mostrar sus nombres actuales.
    public void setUsers(Usuario[] users) {
        this.allUsers = users != null ? users : new Usuario[0];
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
        Deuda debt = debts[position];

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

        if (debt.getGrupoFamiliarId() != null) {

            String groupName = "Grupo Familiar";

            // Busca el grupo familiar asociado a la deuda.
            for (Grupofamiliar group : allGroups) {
                if (group.getId().equals(debt.getGrupoFamiliarId())) {
                    groupName = group.getNombre();
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

            for (Usuario user : allUsers) {
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

        for (Pago payment : allPayments) {
            if (payment.getDeudaId().equals(debt.getId())) {
                paid += payment.getMonto();
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

            tvName = itemView.findViewById(R.id.tx_nombre_deuda);
            tvScope = itemView.findViewById(R.id.tv_debt_scope);
            tvDesc = itemView.findViewById(R.id.tv_debt_desc);
            tvStatus = itemView.findViewById(R.id.tv_debt_status);

            tvTotal = itemView.findViewById(R.id.tx_deuda_total_grupo);
            tvPaid = itemView.findViewById(R.id.tv_debt_paid);
            tvRemaining = itemView.findViewById(R.id.tv_debt_remaining);

            pbProgress = itemView.findViewById(R.id.pb_debt_progress);

            btnHistory = itemView.findViewById(R.id.btn_debt_history);
            btnPay = itemView.findViewById(R.id.btn_debt_pay);
        }
    }
}
