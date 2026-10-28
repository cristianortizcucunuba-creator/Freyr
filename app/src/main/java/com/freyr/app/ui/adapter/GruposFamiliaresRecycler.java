
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

public class GruposFamiliaresRecycler extends RecyclerView.Adapter<GruposFamiliaresRecycler.GroupViewHolder> {

    // Interfaz para responder cuando el usuario selecciona un grupo.
    public interface OnGroupClickListener {
        void onGroupClick(FamilyGroup group);
    }

    private final Context context;
    private final OnGroupClickListener listener;

    // Arreglos que almacenan los grupos, las deudas y los pagos.
    private FamilyGroup[] groups = new FamilyGroup[0];
    private Debt[] allDebts = new Debt[0];
    private Payment[] allPayments = new Payment[0];

    // Identificador del usuario que inició sesión.
    private String currentUserId = "";

    public GruposFamiliaresRecycler(Context context, OnGroupClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    // Actualiza los grupos familiares que se mostrarán.
    public void setGroups(FamilyGroup[] groups) {
        this.groups = groups != null ? groups : new FamilyGroup[0];
        notifyDataSetChanged();
    }

    // Actualiza las deudas disponibles.
    public void setDebts(Debt[] debts) {
        this.allDebts = debts != null ? debts : new Debt[0];
        notifyDataSetChanged();
    }

    // Actualiza los pagos disponibles.
    public void setPayments(Payment[] payments) {
        this.allPayments = payments != null ? payments : new Payment[0];
        notifyDataSetChanged();
    }

    // Guarda el identificador del usuario actual.
    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId != null ? currentUserId : "";
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        // Carga el diseño XML de cada tarjeta de grupo familiar.
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_family_group, parent, false);

        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull GroupViewHolder holder, int position) {

        // Obtiene el grupo correspondiente a la posición actual.
        FamilyGroup group = groups[position];

        holder.tvName.setText(group.getName());

        // Comprueba si el usuario actual creó este grupo.
        boolean isCreator = group.getCreatorId() != null
                && group.getCreatorId().equals(currentUserId);

        // Muestra la etiqueta de administrador solamente al creador.
        holder.tvAdmin.setVisibility(
                isCreator ? View.VISIBLE : View.GONE);

        // Variables para calcular las deudas y los pagos del grupo.
        int debtsCount = 0;
        double totalDebt = 0.0;
        double totalPaid = 0.0;

        // Recorre las deudas y selecciona las que pertenecen al grupo.
        for (Debt debt : allDebts) {

            if (group.getId().equals(debt.getFamilyGroupId())) {

                debtsCount++;
                totalDebt += debt.getAmount();

                // Suma los pagos relacionados con la deuda actual.
                for (Payment payment : allPayments) {

                    if (payment.getDebtId().equals(debt.getId())) {
                        totalPaid += payment.getAmount();
                    }
                }
            }
        }

        // Calcula el saldo pendiente, sin permitir valores negativos.
        double remaining = Math.max(0.0, totalDebt - totalPaid);

        // Obtiene la cantidad de integrantes del grupo.
        int memberCount = group.getMemberIds() != null
                ? group.getMemberIds().size()
                : 0;

        // Construye un texto con el número de miembros y deudas.
        String membersText = memberCount
                + (memberCount == 1 ? " miembro" : " miembros");

        String debtsText = debtsCount
                + (debtsCount == 1
                ? " deuda familiar"
                : " deudas familiares");

        holder.tvMembersCount.setText(membersText + " · " + debtsText);

        // Muestra los valores económicos con el formato del proyecto.
        holder.tvTotal.setText(
                FreyrRepository.formatCurrency(totalDebt));

        holder.tvPaid.setText(
                FreyrRepository.formatCurrency(totalPaid));

        holder.tvRemaining.setText(
                FreyrRepository.formatCurrency(remaining));

        // Abre los detalles cuando se selecciona el grupo.
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGroupClick(group);
            }
        });
    }

    @Override
    public int getItemCount() {
        // Los arreglos utilizan length para conocer su tamaño.
        return groups.length;
    }

    // Mantiene las referencias a las vistas de cada tarjeta.
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
