
package com.freyr.app.Clases.listas;

// 1. IMPORTACIONES
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


// 2. CLASE PRINCIPAL
public class listaDePagos extends RecyclerView.Adapter<listaDePagos.ContenedorPago> {

    // 3. INTERFAZ PARA ELIMINAR PAGOS
    public interface AlEliminarPago {
        void alEliminarPago(Pago payment);
    }


    // 4. ATRIBUTOS
    private final Context context;
    private final AlEliminarPago accionAlEliminar;

    // Arreglos que almacenan los pagos y los usuarios.
    private Pago[] payments = new Pago[0];
    private Usuario[] todosLosUsuarios = new Usuario[0];


    // 5. CONSTRUCTOR
    public listaDePagos(Context contexto, AlEliminarPago accionAlEliminar) {
        this.context = contexto;
        this.accionAlEliminar = accionAlEliminar;
    }


    // 6. ACTUALIZACIÓN DE DATOS

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


    // 7. CREACIÓN DE LAS VISTAS DE CADA PAGO
    @NonNull
    @Override
    public ContenedorPago onCreateViewHolder(
            @NonNull ViewGroup contenedorPadre, int tipoVista) {

        // Carga el diseño XML correspondiente a cada pago.
        View vista = LayoutInflater.from(context)
                .inflate(R.layout.item_payment, contenedorPadre, false);

        return new ContenedorPago(vista);
    }


    // 8. ASIGNACIÓN DE DATOS A CADA PAGO
    @Override
    public void onBindViewHolder(
            @NonNull ContenedorPago contenedor, int posicion) {

        // Obtiene el pago correspondiente a esta posición.
        Pago payment = payments[posicion];


        // 8.1. MOSTRAR EL MONTO DEL PAGO
        contenedor.txMonto.setText(
                FreyrRepositorio.formatearMoneda(payment.getMonto())
        );


        // 8.2. BUSCAR AL USUARIO QUE REALIZÓ EL PAGO
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


        // 8.3. MOSTRAR LA DESCRIPCIÓN DEL PAGO
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


        // 8.4. CONFIGURAR EL BOTÓN PARA ELIMINAR
        contenedor.btnEliminar.setOnClickListener(v -> {
            if (accionAlEliminar != null) {
                accionAlEliminar.alEliminarPago(payment);
            }
        });
    }


    // 9. CANTIDAD DE ELEMENTOS
    @Override
    public int getItemCount() {
        // Devuelve la cantidad de pagos almacenados.
        return payments.length;
    }


    // 10. CONTENEDOR DE VISTAS
    static class ContenedorPago extends RecyclerView.ViewHolder {

        TextView txMonto, txInfo, txDescrip;
        ImageButton btnEliminar;

        public ContenedorPago(@NonNull View vistaElemento) {
            super(vistaElemento);

            // Relaciona los componentes de Java con los elementos del XML.
            txMonto = vistaElemento.findViewById(R.id.tx_monto_pago);
            txInfo = vistaElemento.findViewById(R.id.tx_informacion_pago);
            txDescrip = vistaElemento.findViewById(R.id.tx_descripcion_pago);
            btnEliminar = vistaElemento.findViewById(R.id.btn_eliminar_pago);
        }
    }
}
