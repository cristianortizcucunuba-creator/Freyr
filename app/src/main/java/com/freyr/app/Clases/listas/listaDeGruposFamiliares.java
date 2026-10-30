
package com.freyr.app.Clases.listas;

// 1. IMPORTACIONES
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.freyr.app.R;
import com.freyr.app.data.datos.Deuda;
import com.freyr.app.data.datos.Grupofamiliar;
import com.freyr.app.data.datos.Pago;
import com.freyr.app.data.repositorio.FreyrRepositorio;


// 2. CLASE PRINCIPAL
public class listaDeGruposFamiliares extends RecyclerView.Adapter<listaDeGruposFamiliares.ContenedorGrupo> {

    // 3. INTERFAZ DE ACCIONES
    // Define la acción que se ejecuta al seleccionar un grupo.
    public interface accionAlTocarGrupo {
        void alTocarGrupo(Grupofamiliar grupo);
    }


    // 4. DECLARACIÓN DE ATRIBUTOS
    private final Context context;
    private final accionAlTocarGrupo manejador;

    // Arreglos que almacenan los grupos, las deudas y los pagos.
    private Grupofamiliar[] groups = new Grupofamiliar[0];
    private Deuda[] todasLasDeudas = new Deuda[0];
    private Pago[] todosLosPagos = new Pago[0];

    // Identificador del usuario que inició sesión.
    private String idUsuarioActual = "";


    // 5. CONSTRUCTOR
    public listaDeGruposFamiliares(
            Context context,
            accionAlTocarGrupo manejador
    ) {
        this.context = context;
        this.manejador = manejador;
    }


    // 6. ACTUALIZACIÓN DE DATOS
    // Actualiza los grupos familiares que se mostrarán.
    public void setGrupos(Grupofamiliar[] groups) {
        this.groups = groups != null ? groups : new Grupofamiliar[0];
        notifyDataSetChanged();
    }

    // Actualiza las deudas disponibles.
    public void setDeudas(Deuda[] deudas) {
        this.todasLasDeudas = deudas != null ? deudas : new Deuda[0];
        notifyDataSetChanged();
    }

    // Actualiza los pagos disponibles.
    public void setPagos(Pago[] pagos) {
        this.todosLosPagos = pagos != null ? pagos : new Pago[0];
        notifyDataSetChanged();
    }

    // Guarda el identificador del usuario actual.
    public void setIdUsuarioActual(String idUsuarioActual) {
        this.idUsuarioActual = idUsuarioActual != null
                ? idUsuarioActual
                : "";

        notifyDataSetChanged();
    }


    // 7. CREACIÓN DE ELEMENTOS DE LA LISTA
    @NonNull
    @Override
    public ContenedorGrupo onCreateViewHolder(
            @NonNull ViewGroup contenedorPadre,
            int tipoVista
    ) {

        // Cargar el diseño XML de cada tarjeta de grupo familiar.
        View vista = LayoutInflater.from(context)
                .inflate(R.layout.item_family_group, contenedorPadre, false);

        return new ContenedorGrupo(vista);
    }


    // 8. ASIGNACIÓN DE DATOS A CADA ELEMENTO
    @Override
    public void onBindViewHolder(
            @NonNull ContenedorGrupo contenedor,
            int posicion
    ) {

        // Obtener el grupo correspondiente a la posición actual.
        Grupofamiliar grupo = groups[posicion];

        contenedor.txNombre.setText(grupo.getNombre());


        // 9. IDENTIFICACIÓN DEL ADMINISTRADOR
        // Comprobar si el usuario actual creó este grupo.
        boolean creador = grupo.getCreadorId() != null
                && grupo.getCreadorId().equals(idUsuarioActual);

        // Mostrar la etiqueta de administrador solamente al creador.
        contenedor.txAdmin.setVisibility(
                creador ? View.VISIBLE : View.GONE
        );


        // 10. CÁLCULO DE DEUDAS Y PAGOS
        // Variables utilizadas para calcular los valores económicos.
        int cantidadDeudas = 0;
        double totalDeudas = 0.0;
        double totalAbonado = 0.0;

        // Recorrer las deudas y seleccionar las que pertenecen al grupo.
        for (Deuda debt : todasLasDeudas) {

            if (grupo.getId().equals(debt.getGrupoFamiliarId())) {

                cantidadDeudas++;
                totalDeudas += debt.getMonto();

                // Sumar los pagos relacionados con la deuda actual.
                for (Pago payment : todosLosPagos) {

                    if (payment.getDeudaId().equals(debt.getId())) {
                        totalAbonado += payment.getMonto();
                    }
                }
            }
        }

        // Calcular el saldo pendiente sin permitir valores negativos.
        double saldoPendiente = Math.max(
                0.0,
                totalDeudas - totalAbonado
        );


        // 11. INFORMACIÓN DE LOS INTEGRANTES
        // Obtener la cantidad de integrantes del grupo.
        int cantidadMiembros = grupo.getMiembrosIds() != null
                ? grupo.getMiembrosIds().size()
                : 0;

        // Construir el texto con la cantidad de miembros.
        String textoMiembros = cantidadMiembros
                + (cantidadMiembros == 1 ? " miembro" : " miembros");

        // Construir el texto con la cantidad de deudas.
        String textoDeudas = cantidadDeudas
                + (cantidadDeudas == 1
                ? " deuda familiar"
                : " deudas familiares");

        contenedor.txCantidadMiembros.setText(
                textoMiembros + " · " + textoDeudas
        );


        // 12. ACTUALIZACIÓN DE VALORES MONETARIOS
        // Mostrar los valores económicos con el formato del proyecto.
        contenedor.txTotal.setText(
                FreyrRepositorio.formatearMoneda(totalDeudas)
        );

        contenedor.txAbonado.setText(
                FreyrRepositorio.formatearMoneda(totalAbonado)
        );

        contenedor.txPendiente.setText(
                FreyrRepositorio.formatearMoneda(saldoPendiente)
        );


        // 13. CONFIGURACIÓN DE EVENTOS
        // Abrir los detalles cuando se selecciona el grupo.
        contenedor.itemView.setOnClickListener(v -> {

            if (manejador != null) {
                manejador.alTocarGrupo(grupo);
            }
        });
    }


    // 14. CANTIDAD DE ELEMENTOS
    @Override
    public int getItemCount() {

        // Los arreglos utilizan length para conocer su tamaño.
        return groups.length;
    }


    // 15. CONTENEDOR DE VISTAS
    // Mantiene las referencias a los componentes de cada tarjeta.
    static class ContenedorGrupo extends RecyclerView.ViewHolder {

        TextView txNombre, txAdmin, txCantidadMiembros;
        TextView txTotal, txAbonado, txPendiente;

        public ContenedorGrupo(@NonNull View itemView) {
            super(itemView);

            // Vincular los componentes del diseño XML.
            txNombre = itemView.findViewById(R.id.tx_nom_grupo);
            txAdmin = itemView.findViewById(R.id.tx_etiq_admin);
            txCantidadMiembros = itemView.findViewById(R.id.tx_cantidad_miembros_grupo);

            txTotal = itemView.findViewById(R.id.tx_grupo_deuda_total);
            txAbonado = itemView.findViewById(R.id.tx_total_abonado_grupo);
            txPendiente = itemView.findViewById(R.id.tx_saldo_pendiente_grupo);
        }
    }
}
