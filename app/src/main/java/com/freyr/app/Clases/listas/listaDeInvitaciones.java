
package com.freyr.app.Clases.listas;

// 1. IMPORTACIONES
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.freyr.app.R;
import com.freyr.app.data.datos.Grupofamiliar;
import com.freyr.app.data.datos.Invitacion;
import com.freyr.app.data.datos.Usuario;


// 2. CLASE PRINCIPAL
public class listaDeInvitaciones extends RecyclerView.Adapter<listaDeInvitaciones.ContenedorInvitacion> {

    // 3. INTERFAZ PARA LAS ACCIONES DE LAS INVITACIONES
    public interface AccionesRespuestaInvitacion {
        void Aceptar(Invitacion invitacion);
        void Rechazar(Invitacion invitacion);
    }

    // 4. ATRIBUTOS
    private final Context contexto;
    private final AccionesRespuestaInvitacion accionRespuesta;

    // Arreglos que almacenan las invitaciones, grupos y usuarios.
    private Invitacion[] invitaciones = new Invitacion[0];
    private Grupofamiliar[] todosLosGrupos = new Grupofamiliar[0];
    private Usuario[] todosLosUsuarios = new Usuario[0];


    // 5. CONSTRUCTOR
    public listaDeInvitaciones(Context context, AccionesRespuestaInvitacion accionRespuesta) {
        this.contexto = context;
        this.accionRespuesta = accionRespuesta;
    }


    // 6. ACTUALIZACIÓN DE DATOS

    // Actualiza las invitaciones que se mostrarán en pantalla.
    public void setInvitaciones(Invitacion[] invitaciones) {
        this.invitaciones = invitaciones != null
                ? invitaciones : new Invitacion[0];

        notifyDataSetChanged();
    }

    // Actualiza los grupos familiares disponibles.
    public void setGrupos(Grupofamiliar[] groups) {
        this.todosLosGrupos = groups != null
                ? groups : new Grupofamiliar[0];

        notifyDataSetChanged();
    }

    // Actualiza la información de los usuarios.
    public void setUsuarios(Usuario[] users) {
        this.todosLosUsuarios = users != null
                ? users : new Usuario[0];

        notifyDataSetChanged();
    }


    // 7. CREACIÓN DE LAS VISTAS DE CADA INVITACIÓN
    @NonNull
    @Override
    public ContenedorInvitacion onCreateViewHolder(
            @NonNull ViewGroup contenedorPadre, int viewType) {

        // Carga el diseño XML correspondiente a una invitación.
        View vista = LayoutInflater.from(contexto)
                .inflate(R.layout.item_invitation, contenedorPadre, false);

        return new ContenedorInvitacion(vista);
    }


    // 8. ASIGNACIÓN DE DATOS A CADA INVITACIÓN
    @Override
    public void onBindViewHolder(
            @NonNull ContenedorInvitacion contenedor, int posicion) {

        // Obtiene la invitación correspondiente a la posición actual.
        Invitacion invitacion = invitaciones[posicion];


        // 8.1. BUSCAR EL NOMBRE DEL GRUPO
        String nombreGrupo = "Grupo Familiar";

        for (Grupofamiliar group : todosLosGrupos) {
            if (group.getId().equals(invitacion.getGrupoFamiliarId())) {
                nombreGrupo = group.getNombre();
                break;
            }
        }

        contenedor.txNombreGrupo.setText(nombreGrupo);


        // 8.2. BUSCAR AL USUARIO QUE ENVIÓ LA INVITACIÓN
        String nombreRemitente = "Un miembro familiar";

        for (Usuario user : todosLosUsuarios) {
            if (user.getId().equals(invitacion.getUsuarioInvitadorId())) {
                nombreRemitente = user.getNomCompl()
                        + " (@" + user.getNomUsuario() + ")";
                break;
            }
        }

        // Muestra quién envió la invitación.
        contenedor.txRemitente.setText("Invitado por " + nombreRemitente);


        // 8.3. CONFIGURAR EL BOTÓN PARA ACEPTAR
        contenedor.btnAceptar.setOnClickListener(v -> {
            if (accionRespuesta != null) {
                accionRespuesta.Aceptar(invitacion);
            }
        });


        // 8.4. CONFIGURAR EL BOTÓN PARA RECHAZAR
        contenedor.btnRechazar.setOnClickListener(v -> {
            if (accionRespuesta != null) {
                accionRespuesta.Rechazar(invitacion);
            }
        });
    }


    // 9. CANTIDAD DE ELEMENTOS
    @Override
    public int getItemCount() {
        // Devuelve la cantidad de invitaciones almacenadas.
        return invitaciones.length;
    }


    // 10. CONTENEDOR DE VISTAS
    static class ContenedorInvitacion extends RecyclerView.ViewHolder {

        TextView txNombreGrupo, txRemitente;
        Button btnAceptar, btnRechazar;

        public ContenedorInvitacion(@NonNull View itemView) {
            super(itemView);

            // Relaciona los componentes de Java con los elementos del XML.
            txNombreGrupo = itemView.findViewById(R.id.tx_nombre_grupo_invitacion);
            txRemitente = itemView.findViewById(R.id.tx_remitente_invitacion);

            btnAceptar = itemView.findViewById(R.id.btn_aceptar_invitacion);
            btnRechazar = itemView.findViewById(R.id.btn_rechazar_invitacion);
        }
    }
}
