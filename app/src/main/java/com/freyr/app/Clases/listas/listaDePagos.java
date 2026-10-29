
package com.freyr.app.Clases.listas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.freyr.app.R;
import com.freyr.app.data.datos.Pago;
import com.freyr.app.data.datos.Usuario;
import com.freyr.app.data.repositorio.FreyrRepositorio;

public class listaDePagos extends RecyclerView.Adapter<listaDePagos.ContenedorPago> {

    // Define la acción que se ejecuta al eliminar un pago.
    public interface AlEliminarPago {
        void alEliminarPago(Pago payment);
    }

    private final Context context;
    private final AlEliminarPago accionAlEliminar;

    // Arreglos que almacenan los pagos y los usuarios.
    private Pago[] payments = new Pago[0];
    private Usuario[] todosLosUsuarios = new Usuario[0];

    public listaDePagos(Context contexto, AlEliminarPago accionAlEliminar) {
        this.context = contexto;
        this.accionAlEliminar = accionAlEliminar;
    }

    // Actualiza el arreglo de pagos y refresca la pantalla.
    public void setPagos(Pago[] payments) {
        this.payments = payments != null
                ? payments : new Pago[0];

        notifyDataSetChanged();
    }

    // Actualiza el arreglo de usuarios y refresca la pantalla.
    public void setUsuarios(Usuario[] users) {
        this.todosLosUsuarios = users != null
                ? users : new Usuario[0];

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContenedorPago onCreateViewHolder(
            @NonNull ViewGroup contenedorPadre, int tipoVista) {

        // Carga el diseño XML de cada pago.
        View vista = LayoutInflater.from(context)
                .inflate(R.layout.item_payment, contenedorPadre, false);

        return new ContenedorPago(vista);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ContenedorPago contenedor, int posicion) {

        // Obtiene el pago correspondiente a esta posición.
        Pago payment = payments[posicion];

        // Muestra el valor del pago con formato de moneda.
        contenedor.txMonto.setText(
                FreyrRepositorio.formatearMoneda(payment.getMonto())
        );

        // Busca el nombre del usuario que realizó el pago.
        String nombreUsuario = "Usuario";

        for (Usuario user : todosLosUsuarios) {
            if (user.getId().equals(payment.getUsuarioId())) {
                nombreUsuario = user.getNomCompl();
                break;
            }
        }

        // Muestra quién pagó y la fecha del pago.
        contenedor.txInfo.setText(
                "Pagado por " + nombreUsuario + " · " + payment.getFecha()
        );

        // Muestra la descripción solo si existe y no está vacía.
        if (payment.getDescripcion() != null
                && !payment.getDescripcion().trim().isEmpty()) {

            contenedor.txDescrip.setText(
                    "\"" + payment.getDescripcion() + "\""
            );

            contenedor.txDescrip.setVisibility(View.VISIBLE);

        } else {
            // Oculta el campo cuando el pago no tiene descripción.
            contenedor.txDescrip.setVisibility(View.GONE);
        }

        // Ejecuta la acción de eliminar cuando se pulsa el botón.
        contenedor.btnEliminar.setOnClickListener(v -> {
            if (accionAlEliminar != null) {
                accionAlEliminar.alEliminarPago(payment);
            }
        });
    }

    @Override
    public int getItemCount() {
        // Devuelve la cantidad de pagos almacenados.
        return payments.length;
    }

    // Mantiene las referencias a las vistas de cada pago.
    static class ContenedorPago extends RecyclerView.ViewHolder {

        TextView txMonto, txInfo, txDescrip;
        ImageButton btnEliminar;

        public ContenedorPago(@NonNull View vistaElemento) {
            super(vistaElemento);

            txMonto = vistaElemento.findViewById(R.id.tx_monto_pago);
            txInfo = vistaElemento.findViewById(R.id.tx_informacion_pago);
            txDescrip = vistaElemento.findViewById(R.id.tx_descripcion_pago);
            btnEliminar = vistaElemento.findViewById(R.id.btn_eliminar_pago);
        }
    }
}
