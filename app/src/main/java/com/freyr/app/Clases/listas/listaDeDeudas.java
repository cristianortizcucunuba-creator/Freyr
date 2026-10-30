
package com.freyr.app.Clases.listas;

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
public class listaDeDeudas extends RecyclerView.Adapter<listaDeDeudas.ContenedorDeudas> {

    // 3. INTERFAZ DE ACCIONES
    // Define las acciones disponibles al seleccionar una deuda.
    public interface MonitorDeudas {

        // Abrir los detalles de una deuda.
        void tocDeuda(Deuda debt);

        // Registrar un pago de una deuda.
        void tocPagar(Deuda debt);
    }


    // 4. DECLARACIÓN DE ATRIBUTOS
    private final Context contexto;
    private final MonitorDeudas receptor;

    // Arreglos que almacenan las deudas, pagos, grupos y usuarios.
    // Inicialmente están vacíos y pueden actualizarse cuando llegan datos.
    private Deuda[] deudas = new Deuda[0];
    private Pago[] todosLosPagos = new Pago[0];
    private Grupofamiliar[] todosLosGrupos = new Grupofamiliar[0];
    private Usuario[] todosLosUsuarios = new Usuario[0];


    // 5. CONSTRUCTOR
    public listaDeDeudas(Context context, MonitorDeudas listener) {
        this.contexto = context;
        this.receptor = listener;
    }


    // 6. ACTUALIZACIÓN DE DATOS
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


    // 7. CREACIÓN DE ELEMENTOS DE LA LISTA
    @NonNull
    @Override
    public ContenedorDeudas onCreateViewHolder(
            @NonNull ViewGroup contPadre,
            int tipoVista
    ) {

        // Cargar el diseño XML de una tarjeta de deuda.
        View vista = LayoutInflater.from(contexto)
                .inflate(R.layout.item_debt, contPadre, false);

        return new ContenedorDeudas(vista);
    }


    // 8. ASIGNACIÓN DE DATOS A CADA ELEMENTO
    @Override
    public void onBindViewHolder(
            @NonNull ContenedorDeudas contenedor,
            int posicion
    ) {

        // Obtener la deuda correspondiente a esta posición.
        Deuda debt = deudas[posicion];

        contenedor.tvNombre.setText(debt.getNombre());

        // Mostrar la descripción solamente si contiene texto.
        if (debt.getDescripcion() != null
                && !debt.getDescripcion().trim().isEmpty()) {

            contenedor.tvDescrip.setText(debt.getDescripcion());
            contenedor.tvDescrip.setVisibility(View.VISIBLE);

        } else {
            contenedor.tvDescrip.setVisibility(View.GONE);
        }


        // 9. INFORMACIÓN DEL GRUPO Y RESPONSABLE
        // Construir el texto que identifica el grupo de la deuda.
        StringBuilder contexto = new StringBuilder();

        if (debt.getGrupoFamiliarId() != null) {

            String nomGrupo = "Grupo Familiar";

            // Buscar el grupo familiar asociado a la deuda.
            for (Grupofamiliar group : todosLosGrupos) {

                if (group.getId().equals(debt.getGrupoFamiliarId())) {
                    nomGrupo = group.getNombre();
                    break;
                }
            }

            contexto.append("Familia · ").append(nomGrupo);

        } else {
            contexto.append("Deuda Personal");
        }


        // Buscar al usuario responsable de la deuda.
        if (debt.getIdUsuarioAsignado() != null) {

            String nombreResponsable = null;

            for (Usuario user : todosLosUsuarios) {

                if (user.getId().equals(debt.getIdUsuarioAsignado())) {
                    nombreResponsable = user.getNomCompl();
                    break;
                }
            }

            if (nombreResponsable != null) {
                contexto.append(" · Responsable: ").append(nombreResponsable);
            }
        }

        contenedor.tvTipoDeuda.setText(contexto.toString());


        // 10. CÁLCULO DE LOS PAGOS Y SALDO PENDIENTE
        // Sumar todos los pagos relacionados con esta deuda.
        double abonado = 0.0;

        for (Pago payment : todosLosPagos) {

            if (payment.getDeudaId().equals(debt.getId())) {
                abonado += payment.getMonto();
            }
        }

        // Calcular cuánto dinero queda pendiente.
        double restante = Math.max(0.0, debt.getMonto() - abonado);

        // Determinar si la deuda ya está pagada.
        boolean estPagada = restante == 0.0
                || "abonado".equalsIgnoreCase(debt.getEstado());


        // 11. ACTUALIZACIÓN DE VALORES MONETARIOS
        contenedor.tvTotal.setText(
                FreyrRepositorio.formatearMoneda(debt.getMonto())
        );

        contenedor.tvAbonado.setText(
                FreyrRepositorio.formatearMoneda(abonado)
        );

        contenedor.tvRestante.setText(
                FreyrRepositorio.formatearMoneda(restante)
        );


        // 12. ACTUALIZACIÓN DEL ESTADO Y PROGRESO
        if (estPagada) {

            // Configurar apariencia de una deuda pagada.
            contenedor.tvEstado.setText("Pagado");

            contenedor.tvEstado.setTextColor(
                    ContextCompat.getColor(
                            this.contexto,
                            R.color.freyr_status_paid
                    )
            );

            contenedor.tvEstado.setBackgroundResource(
                    R.drawable.bg_status_paid
            );

            contenedor.btnPagar.setVisibility(View.GONE);
            contenedor.barProgreso.setProgress(100);

        } else {

            // Configurar apariencia de una deuda pendiente.
            contenedor.tvEstado.setText("Pendiente");

            contenedor.tvEstado.setTextColor(
                    ContextCompat.getColor(
                            this.contexto,
                            R.color.freyr_secondary
                    )
            );

            contenedor.tvEstado.setBackgroundResource(
                    R.drawable.bg_status_pending
            );

            contenedor.btnPagar.setVisibility(View.VISIBLE);

            // Calcular el porcentaje pagado y evitar dividir entre cero.
            int porcentaje = debt.getMonto() > 0
                    ? (int) Math.min(
                    100,
                    (abonado / debt.getMonto()) * 100
            )
                    : 0;

            contenedor.barProgreso.setProgress(porcentaje);
        }


        // 13. CONFIGURACIÓN DE EVENTOS
        // Abrir los detalles cuando se pulsa la tarjeta.
        contenedor.itemView.setOnClickListener(v -> {

            if (receptor != null) {
                receptor.tocDeuda(debt);
            }
        });

        // El botón de historial también abre los detalles.
        contenedor.btnHistorial.setOnClickListener(v -> {

            if (receptor != null) {
                receptor.tocDeuda(debt);
            }
        });

        // Solicitar el registro de un pago.
        contenedor.btnPagar.setOnClickListener(v -> {

            if (receptor != null) {
                receptor.tocPagar(debt);
            }
        });
    }


    // 14. CANTIDAD DE ELEMENTOS
    @Override
    public int getItemCount() {

        // Los arreglos utilizan length para indicar su tamaño.
        return deudas.length;
    }


    // 15. CONTENEDOR DE VISTAS
    // Conserva las referencias a los componentes de cada tarjeta.
    static class ContenedorDeudas extends RecyclerView.ViewHolder {

        TextView tvNombre, tvTipoDeuda, tvDescrip, tvEstado;
        TextView tvTotal, tvAbonado, tvRestante;

        ProgressBar barProgreso;

        Button btnHistorial, btnPagar;

        public ContenedorDeudas(@NonNull View vistaObj) {
            super(vistaObj);

            // Vincular los componentes del diseño XML.
            tvNombre = vistaObj.findViewById(R.id.tx_nombre_deuda);
            tvTipoDeuda = vistaObj.findViewById(R.id.tx_deuda_ambito);
            tvDescrip = vistaObj.findViewById(R.id.tx_descript_deuda);
            tvEstado = vistaObj.findViewById(R.id.tx_deuda_estado);

            tvTotal = vistaObj.findViewById(R.id.tx_deuda_total_grupo);
            tvAbonado = vistaObj.findViewById(R.id.tx_deuda_pagada);
            tvRestante = vistaObj.findViewById(R.id.tx_monto_pendiente);

            barProgreso = vistaObj.findViewById(R.id.bar_monto_progreso);

            btnHistorial = vistaObj.findViewById(R.id.btn_monto_historial);
            btnPagar = vistaObj.findViewById(R.id.btn_pagar_deuda);
        }
    }
}
