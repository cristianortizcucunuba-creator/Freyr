
package com.freyr.app.Clases.listas;

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

public class listaDeudas extends RecyclerView.Adapter<listaDeudas.DebtViewHolder> {

    // Interfaz que define las acciones disponibles al seleccionar una deuda.
    public interface OnDebtClickListener {
        void onDebtClick(Deuda debt);
        void onPayClick(Deuda debt);
    }

    private final Context contexto;
    private final OnDebtClickListener receptor;

    // Arreglos que almacenan las deudas, pagos, grupos y usuarios.
    // Inicialmente están vacíos y pueden reemplazarse cuando llegan datos.
    private Deuda[] deudas = new Deuda[0];
    private Pago[] todosLosPagos = new Pago[0];
    private Grupofamiliar[] todosLosGrupos = new Grupofamiliar[0];
    private Usuario[] todosLosUsuarios = new Usuario[0];

    public listaDeudas(Context context, OnDebtClickListener listener) {
        this.contexto = context;
        this.receptor = listener;
    }

    // Actualiza el arreglo de deudas y refresca la interfaz.
    public void setDeudas(Deuda[] deudas) {
        this.deudas = deudas != null ? deudas : new Deuda[0];
        notifyDataSetChanged();
    }

    // Actualiza los pagos disponibles.
    public void setPagos(Pago[] pagos) {
        this.todosLosPagos = pagos != null ? pagos : new Pago[0];
        notifyDataSetChanged();
    }

    // Actualiza los grupos familiares disponibles.
    public void setGrupos(Grupofamiliar[] grupo) {
        this.todosLosGrupos = grupo != null ? grupo : new Grupofamiliar[0];
        notifyDataSetChanged();
    }

    // Actualiza los usuarios para mostrar sus nombres actuales.
    public void setUsuarios(Usuario[] user) {
        this.todosLosUsuarios = user != null ? user : new Usuario[0];
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DebtViewHolder onCreateViewHolder(
            @NonNull ViewGroup contPadre, int tipoVista) {

        // Carga el diseño XML de una tarjeta de deuda.
        View vista = LayoutInflater.from(contexto)
                .inflate(R.layout.item_debt, contPadre, false);

        return new DebtViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(
            @NonNull DebtViewHolder contenedor, int posicion) {

        // Obtiene la deuda correspondiente a esta posición.
        Deuda debt = deudas[posicion];

        contenedor.tvName.setText(debt.getName());

        // Muestra la descripción solamente si contiene texto.
        if (debt.getDescription() != null
                && !debt.getDescription().trim().isEmpty()) {

            contenedor.tvDesc.setText(debt.getDescription());
            contenedor.tvDesc.setVisibility(View.VISIBLE);

        } else {
            contenedor.tvDesc.setVisibility(View.GONE);
        }

        // Construye el texto que identifica el grupo de la deuda.
        StringBuilder contexto = new StringBuilder();

        if (debt.getFamilyGroupId() != null) {

            String nomGrupo = "Grupo Familiar";

            // Busca el grupo familiar asociado a la deuda.
            for (Grupofamiliar group : todosLosGrupos) {
                if (group.getId().equals(debt.getFamilyGroupId())) {
                    nomGrupo = group.getName();
                    break;
                }
            }

            contexto.append("Familia · ").append(nomGrupo);

        } else {
            contexto.append("Deuda Personal");
        }

        // Busca al usuario responsable de la deuda.
        if (debt.getAssignedUserId() != null) {

            String nombreResponsable = null;

            for (Usuario user : todosLosUsuarios) {
                if (user.getId().equals(debt.getAssignedUserId())) {
                    nombreResponsable = user.getNomCompl();
                    break;
                }
            }

            if (nombreResponsable != null) {
                contexto.append(" · Responsable: ").append(nombreResponsable);
            }
        }

        contenedor.tvScope.setText(contexto.toString());

        // Suma todos los pagos relacionados con esta deuda.
        double abonado = 0.0;

        for (Pago payment : todosLosPagos) {
            if (payment.getDebtId().equals(debt.getId())) {
                abonado += payment.getAmount();
            }
        }

        // Calcula cuánto dinero queda pendiente.
        double restante = Math.max(0.0, debt.getAmount() - abonado);

        boolean estPagada = restante == 0.0
                || "abonado".equalsIgnoreCase(debt.getStatus());

        // Presenta los valores monetarios.
        contenedor.tvTotal.setText(
                FreyrRepository.formatCurrency(debt.getAmount()));

        contenedor.tvPaid.setText(
                FreyrRepository.formatCurrency(abonado));

        contenedor.tvRemaining.setText(
                FreyrRepository.formatCurrency(restante));

        // Actualiza el estado y la barra de progreso.
        if (estPagada) {

            contenedor.tvStatus.setText("Pagado");

            contenedor.tvStatus.setTextColor(
                    ContextCompat.getColor(
                            this.contexto, R.color.freyr_status_paid));

            contenedor.tvStatus.setBackgroundResource(
                    R.drawable.bg_status_paid);

            contenedor.btnPay.setVisibility(View.GONE);
            contenedor.pbProgress.setProgress(100);

        } else {

            contenedor.tvStatus.setText("Pendiente");

            contenedor.tvStatus.setTextColor(
                    ContextCompat.getColor(
                            this.contexto, R.color.freyr_secondary));

            contenedor.tvStatus.setBackgroundResource(
                    R.drawable.bg_status_pending);

            contenedor.btnPay.setVisibility(View.VISIBLE);

            // Calcula el porcentaje pagado y evita dividir entre cero.
            int porcentaje = debt.getAmount() > 0
                    ? (int) Math.min(
                    100, (abonado / debt.getAmount()) * 100)
                    : 0;

            contenedor.pbProgress.setProgress(porcentaje);
        }

        // Abre los detalles cuando se pulsa la tarjeta.
        contenedor.itemView.setOnClickListener(v -> {
            if (receptor != null) {
                receptor.onDebtClick(debt);
            }
        });

        // El botón de historial abre los detalles de la deuda.
        contenedor.btnHistory.setOnClickListener(v -> {
            if (receptor != null) {
                receptor.onDebtClick(debt);
            }
        });

        // Solicita registrar un pago.
        contenedor.btnPay.setOnClickListener(v -> {
            if (receptor != null) {
                receptor.onPayClick(debt);
            }
        });
    }

    @Override
    public int getItemCount() {
        // Los arreglos utilizan length para indicar su tamaño.
        return deudas.length;
    }

    // Conserva las referencias a las vistas de cada tarjeta.
    static class DebtViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvScope, tvDesc, tvStatus;
        TextView tvTotal, tvPaid, tvRemaining;
        ProgressBar pbProgress;
        Button btnHistory, btnPay;

        public DebtViewHolder(@NonNull View vistaObj) {
            super(vistaObj);

            tvName = vistaObj.findViewById(R.id.tv_debt_name);
            tvScope = vistaObj.findViewById(R.id.tv_debt_scope);
            tvDesc = vistaObj.findViewById(R.id.tv_debt_desc);
            tvStatus = vistaObj.findViewById(R.id.tv_debt_status);

            tvTotal = vistaObj.findViewById(R.id.tv_debt_total);
            tvPaid = vistaObj.findViewById(R.id.tv_debt_paid);
            tvRemaining = vistaObj.findViewById(R.id.tv_debt_remaining);

            pbProgress = vistaObj.findViewById(R.id.pb_debt_progress);

            btnHistory = vistaObj.findViewById(R.id.btn_debt_history);
            btnPay = vistaObj.findViewById(R.id.btn_debt_pay);
        }
    }
}
