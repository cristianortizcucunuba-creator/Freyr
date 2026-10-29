
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

public class listaDeDeudas extends RecyclerView.Adapter<listaDeDeudas.ContenedorDeudas> {

    // Interfaz que define las acciones disponibles al seleccionar una deuda.
    public interface MonitorDeudas {
        void tocDeuda(Deuda debt);
        void tocPagar(Deuda debt);
    }

    private final Context contexto;
    private final MonitorDeudas receptor;

    // Arreglos que almacenan las deudas, pagos, grupos y usuarios.
    // Inicialmente, están vacíos y pueden reemplazarse cuando llegan datos.
    private Deuda[] deudas = new Deuda[0];
    private Pago[] todosLosPagos = new Pago[0];
    private Grupofamiliar[] todosLosGrupos = new Grupofamiliar[0];
    private Usuario[] todosLosUsuarios = new Usuario[0];

    public listaDeDeudas(Context context, MonitorDeudas listener) {
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
    public void setGrupos(Grupofamiliar[] group) {
        this.todosLosGrupos = group != null ? group : new Grupofamiliar[0];
        notifyDataSetChanged();
    }

    // Actualiza los usuarios para mostrar sus nombres actuales.
    public void setUsuarios(Usuario[] user) {
        this.todosLosUsuarios = user != null ? user : new Usuario[0];
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContenedorDeudas onCreateViewHolder(
            @NonNull ViewGroup contPadre, int tipoVista) {

        // Carga el diseño XML de una tarjeta de deuda.
        View vista = LayoutInflater.from(contexto)
                .inflate(R.layout.item_debt, contPadre, false);

        return new ContenedorDeudas(vista);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ContenedorDeudas contenedor, int posicion) {

        // Obtiene la deuda correspondiente a esta posición.
        Deuda debt = deudas[posicion];

        contenedor.tvNombre.setText(debt.getName());

        // Muestra la descripción solamente si contiene texto.
        if (debt.getDescription() != null
                && !debt.getDescription().trim().isEmpty()) {

            contenedor.tvDescrip.setText(debt.getDescription());
            contenedor.tvDescrip.setVisibility(View.VISIBLE);

        } else {
            contenedor.tvDescrip.setVisibility(View.GONE);
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

        contenedor.tvTipoDeuda.setText(contexto.toString());

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

        contenedor.tvAbonado.setText(
                FreyrRepository.formatCurrency(abonado));

        contenedor.tvRestante.setText(
                FreyrRepository.formatCurrency(restante));

        // Actualiza el estado y la barra de progreso.
        if (estPagada) {

            contenedor.tvEstado.setText("Pagado");

            contenedor.tvEstado.setTextColor(
                    ContextCompat.getColor(
                            this.contexto, R.color.freyr_status_paid));

            contenedor.tvEstado.setBackgroundResource(
                    R.drawable.bg_status_paid);

            contenedor.btnPagar.setVisibility(View.GONE);
            contenedor.barProgreso.setProgress(100);

        } else {

            contenedor.tvEstado.setText("Pendiente");

            contenedor.tvEstado.setTextColor(
                    ContextCompat.getColor(
                            this.contexto, R.color.freyr_secondary));

            contenedor.tvEstado.setBackgroundResource(
                    R.drawable.bg_status_pending);

            contenedor.btnPagar.setVisibility(View.VISIBLE);

            // Calcula el porcentaje pagado y evita dividir entre cero.
            int porcentaje = debt.getAmount() > 0
                    ? (int) Math.min(
                    100, (abonado / debt.getAmount()) * 100)
                    : 0;

            contenedor.barProgreso.setProgress(porcentaje);
        }

        // Abre los detalles cuando se pulsa la tarjeta.
        contenedor.itemView.setOnClickListener(v -> {
            if (receptor != null) {
                receptor.tocDeuda(debt);
            }
        });

        // El botón de historial abre los detalles de la deuda.
        contenedor.btnHistorial.setOnClickListener(v -> {
            if (receptor != null) {
                receptor.tocDeuda(debt);
            }
        });

        // Solicita registrar un pago.
        contenedor.btnPagar.setOnClickListener(v -> {
            if (receptor != null) {
                receptor.tocPagar(debt);
            }
        });
    }

    @Override
    public int getItemCount() {
        // Los arreglos utilizan length para indicar su tamaño.
        return deudas.length;
    }

    // Conserva las referencias a las vistas de cada tarjeta.
    static class ContenedorDeudas extends RecyclerView.ViewHolder {

        TextView tvNombre, tvTipoDeuda, tvDescrip, tvEstado;
        TextView tvTotal, tvAbonado, tvRestante;
        ProgressBar barProgreso;
        Button btnHistorial, btnPagar;

        public ContenedorDeudas(@NonNull View vistaObj) {
            super(vistaObj);

            tvNombre = vistaObj.findViewById(R.id.tv_debt_name);
            tvTipoDeuda = vistaObj.findViewById(R.id.tv_debt_scope);
            tvDescrip = vistaObj.findViewById(R.id.tv_debt_desc);
            tvEstado = vistaObj.findViewById(R.id.tv_debt_status);

            tvTotal = vistaObj.findViewById(R.id.tv_debt_total);
            tvAbonado = vistaObj.findViewById(R.id.tv_debt_paid);
            tvRestante = vistaObj.findViewById(R.id.tv_debt_remaining);

            barProgreso = vistaObj.findViewById(R.id.pb_debt_progress);

            btnHistorial = vistaObj.findViewById(R.id.btn_debt_history);
            btnPagar = vistaObj.findViewById(R.id.btn_debt_pay);
        }
    }
}
