
package com.freyr.app.Clases.listas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.freyr.app.R;
import com.freyr.app.data.model.Deuda;
import com.freyr.app.data.model.Grupofamiliar;
import com.freyr.app.data.model.Pago;
import com.freyr.app.data.repository.FreyrRepository;

public class listaGruposFamiliares extends RecyclerView.Adapter<listaGruposFamiliares.ContenedorGrupo> {

    // Interfaz para responder cuando el usuario selecciona un grupo.
    public interface accionAlTocarGrupo {
        void alTocarGrupo(Grupofamiliar grupo);
    }

    private final Context context;
    private final accionAlTocarGrupo manejador;

    // Arreglos que almacenan los grupos, las deudas y los pagos.
    private Grupofamiliar[] groups = new Grupofamiliar[0];
    private Deuda[] todasLasDeudas = new Deuda[0];
    private Pago[] todosLosPagos = new Pago[0];

    // Identificador del usuario que inició sesión.
    private String idUsuarioActual = "";

    public listaGruposFamiliares(Context context, accionAlTocarGrupo manejador) {
        this.context = context;
        this.manejador = manejador;
    }

    // Actualiza los grupos familiares que se mostrarán.
    public void setGroups(Grupofamiliar[] groups) {
        this.groups = groups != null ? groups : new Grupofamiliar[0];
        notifyDataSetChanged();
    }

    // Actualiza las deudas disponibles.
    public void setDebts(Deuda[] debts) {
        this.todasLasDeudas = debts != null ? debts : new Deuda[0];
        notifyDataSetChanged();
    }

    // Actualiza los pagos disponibles.
    public void setPayments(Pago[] payments) {
        this.todosLosPagos = payments != null ? payments : new Pago[0];
        notifyDataSetChanged();
    }

    // Guarda el identificador del usuario actual.
    public void setIdUsuarioActual(String idUsuarioActual) {
        this.idUsuarioActual = idUsuarioActual != null ? idUsuarioActual : "";
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContenedorGrupo onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        // Carga el diseño XML de cada tarjeta de grupo familiar.
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_family_group, parent, false);

        return new ContenedorGrupo(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ContenedorGrupo holder, int position) {

        // Obtiene el grupo correspondiente a la posición actual.
        Grupofamiliar group = groups[position];

        holder.tvName.setText(group.getName());

        // Comprueba si el usuario actual creó este grupo.
        boolean isCreator = group.getCreatorId() != null
                && group.getCreatorId().equals(idUsuarioActual);

        // Muestra la etiqueta de administrador solamente al creador.
        holder.tvAdmin.setVisibility(
                isCreator ? View.VISIBLE : View.GONE);

        // Variables para calcular las deudas y los pagos del grupo.
        int debtsCount = 0;
        double totalDebt = 0.0;
        double totalPaid = 0.0;

        // Recorre las deudas y selecciona las que pertenecen al grupo.
        for (Deuda debt : todasLasDeudas) {

            if (group.getId().equals(debt.getFamilyGroupId())) {

                debtsCount++;
                totalDebt += debt.getAmount();

                // Suma los pagos relacionados con la deuda actual.
                for (Pago payment : todosLosPagos) {

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
            if (manejador != null) {
                manejador.alTocarGrupo(group);
            }
        });
    }

    @Override
    public int getItemCount() {
        // Los arreglos utilizan length para conocer su tamaño.
        return groups.length;
    }

    // Mantiene las referencias a las vistas de cada tarjeta.
    static class ContenedorGrupo extends RecyclerView.ViewHolder {

        TextView tvName, tvAdmin, tvMembersCount;
        TextView tvTotal, tvPaid, tvRemaining;

        public ContenedorGrupo(@NonNull View itemView) {
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
