
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
import com.freyr.app.data.repositorio.FreyrRepositorio;

public class listaDeGruposFamiliares extends RecyclerView.Adapter<listaDeGruposFamiliares.ContenedorGrupo> {

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

    public listaDeGruposFamiliares(Context context, accionAlTocarGrupo manejador) {
        this.context = context;
        this.manejador = manejador;
    }

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
        this.idUsuarioActual = idUsuarioActual != null ? idUsuarioActual : "";
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContenedorGrupo onCreateViewHolder(
            @NonNull ViewGroup contenedorPadre, int tipoVista) {

        // Carga el diseño XML de cada tarjeta de grupo familiar.
        View vista = LayoutInflater.from(context)
                .inflate(R.layout.item_family_group, contenedorPadre, false);

        return new ContenedorGrupo(vista);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ContenedorGrupo contenedor, int posicion) {

        // Obtiene el grupo correspondiente a la posición actual.
        Grupofamiliar grupo = groups[posicion];

        contenedor.txNombre.setText(grupo.getNombre());

        // Comprueba si el usuario actual creó este grupo.
        boolean creador = grupo.getCreadorId() != null
                && grupo.getCreadorId().equals(idUsuarioActual);

        // Muestra la etiqueta de administrador solamente al creador.
        contenedor.txAdmin.setVisibility(
                creador ? View.VISIBLE : View.GONE);

        // Variables para calcular las deudas y los pagos del grupo.
        int cantidadDeudas = 0;
        double totalDeudas = 0.0;
        double totalAbonado = 0.0;

        // Recorre las deudas y selecciona las que pertenecen al grupo.
        for (Deuda debt : todasLasDeudas) {

            if (grupo.getId().equals(debt.getGrupoFamiliarId())) {

                cantidadDeudas++;
                totalDeudas += debt.getMonto();

                // Suma los pagos relacionados con la deuda actual.
                for (Pago payment : todosLosPagos) {

                    if (payment.getDeudaId().equals(debt.getId())) {
                        totalAbonado += payment.getMonto();
                    }
                }
            }
        }

        // Calcula el saldo pendiente, sin permitir valores negativos.
        double saldoPendiente = Math.max(0.0, totalDeudas - totalAbonado);

        // Obtiene la cantidad de integrantes del grupo.
        int cantidadMiembros = grupo.getMiembrosIds() != null
                ? grupo.getMiembrosIds().size()
                : 0;

        // Construye un texto con el número de miembros y deudas.
        String textoMiembros = cantidadMiembros
                + (cantidadMiembros == 1 ? " miembro" : " miembros");

        String textoDeudas = cantidadDeudas
                + (cantidadDeudas == 1
                ? " deuda familiar"
                : " deudas familiares");

        contenedor.txCantidadMiembros.setText(textoMiembros + " · " + textoDeudas);

        // Muestra los valores económicos con el formato del proyecto.
        contenedor.txTotal.setText(
                FreyrRepositorio.formatCurrency(totalDeudas));

        contenedor.txAbonado.setText(
                FreyrRepositorio.formatCurrency(totalAbonado));

        contenedor.txPendiente.setText(
                FreyrRepositorio.formatCurrency(saldoPendiente));

        // Abre los detalles cuando se selecciona el grupo.
        contenedor.itemView.setOnClickListener(v -> {
            if (manejador != null) {
                manejador.alTocarGrupo(grupo);
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

        TextView txNombre, txAdmin, txCantidadMiembros;
        TextView txTotal, txAbonado, txPendiente;

        public ContenedorGrupo(@NonNull View itemView) {
            super(itemView);

            txNombre = itemView.findViewById(R.id.tx_nom_grupo);
            txAdmin = itemView.findViewById(R.id.tx_etiq_admin);
            txCantidadMiembros = itemView.findViewById(R.id.tx_cantidad_miembros_grupo);

            txTotal = itemView.findViewById(R.id.tx_grupo_deuda_total);
            txAbonado = itemView.findViewById(R.id.tx_total_abonado_grupo);
            txPendiente = itemView.findViewById(R.id.tx_saldo_pendiente_grupo);
        }
    }
}
