
package com.freyr.app.ui.adaptadores_interfaz;

// 1. IMPORTACIONES
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
import com.freyr.app.data.datos.Deuda;
import com.freyr.app.data.datos.Grupofamiliar;
import com.freyr.app.data.datos.Pago;
import com.freyr.app.data.datos.Usuario;
import com.freyr.app.data.repositorio.FreyrRepositorio;


// 2. CLASE PRINCIPAL
public class AdaptadorDeudas
        extends RecyclerView.Adapter<AdaptadorDeudas.ContenedorDeuda> {

    // 3. INTERFAZ: ACCIONES DISPONIBLES
    // Define las acciones que se pueden realizar sobre una deuda.
    public interface AccionesDeuda {

        void alSeleccionarDeuda(Deuda debt);

        void alSolicitarPago(Deuda debt);
    }


    // 4. ATRIBUTOS
    private final Context contexto;
    private final AccionesDeuda acciones;

    // Arreglos que almacenan las deudas, pagos, grupos y usuarios.
    // Inicialmente están vacíos y pueden reemplazarse cuando llegan datos.
    private Deuda[] debts = new Deuda[0];
    private Pago[] todosLosPagos = new Pago[0];
    private Grupofamiliar[] todosLosGrupos = new Grupofamiliar[0];
    private Usuario[] todosLosUsuarios = new Usuario[0];


    // 5. CONSTRUCTOR
    public AdaptadorDeudas(Context contexto, AccionesDeuda acciones) {
        this.contexto = contexto;
        this.acciones = acciones;
    }


    // 6. ACTUALIZACIÓN DE DATOS

    // Actualiza el arreglo de deudas y refresca la interfaz.
    public void setDeudas(Deuda[] debts) {
        this.debts = debts != null ? debts : new Deuda[0];
        notifyDataSetChanged();
    }

    // Actualiza los pagos disponibles.
    public void setPago(Pago[] payments) {
        this.todosLosPagos = payments != null ? payments : new Pago[0];
        notifyDataSetChanged();
    }

    // Actualiza los grupos familiares disponibles.
    public void setGrupo(Grupofamiliar[] groups) {
        this.todosLosGrupos = groups != null ? groups : new Grupofamiliar[0];
        notifyDataSetChanged();
    }

    // Actualiza los usuarios para mostrar sus nombres actuales.
    public void setUsuario(Usuario[] users) {
        this.todosLosUsuarios = users != null ? users : new Usuario[0];
        notifyDataSetChanged();
    }


    // 7. CREACIÓN DEL CONTENEDOR DE VISTAS
    @NonNull
    @Override
    public ContenedorDeuda onCreateViewHolder(
            @NonNull ViewGroup padre, int tipoVista) {

        // Carga el diseño XML de una tarjeta de deuda.
        View vista = LayoutInflater.from(contexto)
                .inflate(R.layout.item_debt, padre, false);

        return new ContenedorDeuda(vista);
    }


    // 8. ASIGNACIÓN DE DATOS A CADA TARJETA
    @Override
    public void onBindViewHolder(
            @NonNull ContenedorDeuda contenedor, int posicion) {

        // Obtiene la deuda correspondiente a esta posición.
        Deuda debt = debts[posicion];

        contenedor.txNombre.setText(debt.getNombre());

        // Muestra la descripción solamente si contiene texto.
        if (debt.getDescripcion() != null
                && !debt.getDescripcion().trim().isEmpty()) {

            contenedor.txDescrip.setText(debt.getDescripcion());
            contenedor.txDescrip.setVisibility(View.VISIBLE);

        } else {
            contenedor.txDescrip.setVisibility(View.GONE);
        }


        // 9. IDENTIFICACIÓN DEL ÁMBITO DE LA DEUDA
        // Construye el texto que identifica el grupo de la deuda.
        StringBuilder textoAmbito = new StringBuilder();

        if (debt.getGrupoFamiliarId() != null) {

            String nombreGrupo = "Grupo Familiar";

            // Busca el grupo familiar asociado a la deuda.
            for (Grupofamiliar group : todosLosGrupos) {
                if (group.getId().equals(debt.getGrupoFamiliarId())) {
                    nombreGrupo = group.getNombre();
                    break;
                }
            }

            textoAmbito.append("Familia · ").append(nombreGrupo);

        } else {
            textoAmbito.append("Deuda Personal");
        }


        // 10. IDENTIFICACIÓN DEL RESPONSABLE
        // Busca al usuario responsable de la deuda.
        if (debt.getIdUsuarioAsignado() != null) {

            String nombreResponsable = null;

            for (Usuario user : todosLosUsuarios) {
                if (user.getId().equals(debt.getIdUsuarioAsignado())) {
                    nombreResponsable = user.getNomCompl();
                    break;
                }
            }

            if (nombreResponsable != null) {
                textoAmbito.append(" · Responsable: ")
                        .append(nombreResponsable);
            }
        }

        contenedor.txAmbito.setText(textoAmbito.toString());


        // 11. CÁLCULO DE LOS PAGOS
        // Suma todos los pagos relacionados con esta deuda.
        double montoPagado = 0.0;

        for (Pago payment : todosLosPagos) {
            if (payment.getDeudaId().equals(debt.getId())) {
                montoPagado += payment.getMonto();
            }
        }

        // Calcula cuánto dinero queda pendiente.
        double remaining = Math.max(
                0.0, debt.getMonto() - montoPagado);

        boolean isPaid = remaining == 0.0
                || "montoPagado".equalsIgnoreCase(debt.getEstado());


        // 12. PRESENTACIÓN DE LOS VALORES MONETARIOS
        contenedor.txTotal.setText(
                FreyrRepositorio.formatearMoneda(debt.getMonto()));

        contenedor.txPagado.setText(
                FreyrRepositorio.formatearMoneda(montoPagado));

        contenedor.txPendiente.setText(
                FreyrRepositorio.formatearMoneda(remaining));


        // 13. ACTUALIZACIÓN DEL ESTADO Y LA BARRA DE PROGRESO
        if (isPaid) {

            contenedor.txEstado.setText("Pagado");

            contenedor.txEstado.setTextColor(
                    ContextCompat.getColor(
                            contexto, R.color.freyr_status_paid));

            contenedor.txEstado.setBackgroundResource(
                    R.drawable.bg_status_paid);

            contenedor.btnPagar.setVisibility(View.GONE);
            contenedor.barProgreso.setProgress(100);

        } else {

            contenedor.txEstado.setText("Pendiente");

            contenedor.txEstado.setTextColor(
                    ContextCompat.getColor(
                            contexto, R.color.freyr_secondary));

            contenedor.txEstado.setBackgroundResource(
                    R.drawable.bg_status_pending);

            contenedor.btnPagar.setVisibility(View.VISIBLE);

            // Calcula el porcentaje pagado y evita dividir entre cero.
            int porcentajePagado = debt.getMonto() > 0
                    ? (int) Math.min(
                    100, (montoPagado / debt.getMonto()) * 100)
                    : 0;

            contenedor.barProgreso.setProgress(porcentajePagado);
        }


        // 14. CONFIGURACIÓN DE EVENTOS
        // Abre los detalles cuando se pulsa la tarjeta.
        contenedor.itemView.setOnClickListener(v -> {
            if (acciones != null) {
                acciones.alSeleccionarDeuda(debt);
            }
        });

        // El botón de historial abre los detalles de la deuda.
        contenedor.btnHistorial.setOnClickListener(v -> {
            if (acciones != null) {
                acciones.alSeleccionarDeuda(debt);
            }
        });

        // Solicita registrar un pago.
        contenedor.btnPagar.setOnClickListener(v -> {
            if (acciones != null) {
                acciones.alSolicitarPago(debt);
            }
        });
    }


    // 15. CANTIDAD DE ELEMENTOS
    @Override
    public int getItemCount() {

        // Los arreglos utilizan length para indicar su tamaño.
        return debts.length;
    }


    // 16. CONTENEDOR DE VISTAS
    // Conserva las referencias a las vistas de cada tarjeta.
    static class ContenedorDeuda extends RecyclerView.ViewHolder {

        TextView txNombre, txAmbito, txDescrip, txEstado;
        TextView txTotal, txPagado, txPendiente;

        ProgressBar barProgreso;

        Button btnHistorial, btnPagar;

        public ContenedorDeuda(@NonNull View itemView) {
            super(itemView);

            txNombre = itemView.findViewById(R.id.tx_nombre_deuda);
            txAmbito = itemView.findViewById(R.id.tx_deuda_ambito);
            txDescrip = itemView.findViewById(R.id.tx_descript_deuda);
            txEstado = itemView.findViewById(R.id.tx_deuda_estado);

            txTotal = itemView.findViewById(R.id.tx_deuda_total_grupo);
            txPagado = itemView.findViewById(R.id.tx_deuda_pagada);
            txPendiente = itemView.findViewById(R.id.tx_monto_pendiente);

            barProgreso = itemView.findViewById(R.id.bar_monto_progreso);

            btnHistorial = itemView.findViewById(R.id.btn_monto_historial);
            btnPagar = itemView.findViewById(R.id.btn_pagar_deuda);
        }
    }
}
