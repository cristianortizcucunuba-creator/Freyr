
package com.freyr.app.Clases.listas;

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

public class listaDeInvitaciones extends RecyclerView.Adapter<listaDeInvitaciones.ContenedorInvitacion> {

    // Define las acciones que se ejecutan al aceptar o rechazar una invitación.
    public interface AccionesRespuestaInvitacion {
        void Aceptar(Invitacion invitacion);
        void Rechazar(Invitacion invitacion);
    }

    private final Context contexto;
    private final AccionesRespuestaInvitacion accionRespuesta;

    // Arreglos que almacenan las invitaciones, grupos familiares y usuarios.
    private Invitacion[] invitaciones = new Invitacion[0];
    private Grupofamiliar[] todosLosGrupos = new Grupofamiliar[0];
    private Usuario[] todosLosUsuarios = new Usuario[0];

    public listaDeInvitaciones(Context context, AccionesRespuestaInvitacion accionRespuesta) {
        this.contexto = context;
        this.accionRespuesta = accionRespuesta;
    }

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

    @NonNull
    @Override
    public ContenedorInvitacion onCreateViewHolder(
            @NonNull ViewGroup contenedorPadre, int viewType) {

        // Carga el diseño XML correspondiente a una invitación.
        View vista = LayoutInflater.from(contexto)
                .inflate(R.layout.item_invitation, contenedorPadre, false);

        return new ContenedorInvitacion(vista);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ContenedorInvitacion contenedor, int posicion) {

        // Obtiene la invitación correspondiente a la posición actual.
        Invitacion invitacion = invitaciones[posicion];

        // Busca el nombre del grupo familiar al que pertenece la invitación.
        String nombreGrupo = "Grupo Familiar";

        for (Grupofamiliar group : todosLosGrupos) {
            if (group.getId().equals(invitacion.getGrupoFamiliarId())) {
                nombreGrupo = group.getNombre();
                break;
            }
        }

        contenedor.txNombreGrupo.setText(nombreGrupo);

        // Busca al usuario que envió la invitación.
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

        // Ejecuta la acción correspondiente al aceptar la invitación.
        contenedor.btnAceptar.setOnClickListener(v -> {
            if (accionRespuesta != null) {
                accionRespuesta.Aceptar(invitacion);
            }
        });

        // Ejecuta la acción correspondiente al rechazar la invitación.
        contenedor.btnRechazar.setOnClickListener(v -> {
            if (accionRespuesta != null) {
                accionRespuesta.Rechazar(invitacion);
            }
        });
    }

    @Override
    public int getItemCount() {
        // Devuelve la cantidad de invitaciones almacenadas en el arreglo.
        return invitaciones.length;
    }

    // Mantiene las referencias a las vistas de cada invitación.
    static class ContenedorInvitacion extends RecyclerView.ViewHolder {

        TextView txNombreGrupo, txRemitente;
        Button btnAceptar, btnRechazar;

        public ContenedorInvitacion(@NonNull View itemView) {
            super(itemView);

            txNombreGrupo = itemView.findViewById(R.id.tx_nombre_grupo_invitacion);
            txRemitente = itemView.findViewById(R.id.tx_remitente_invitacion);

            btnAceptar = itemView.findViewById(R.id.btn_aceptar_invitacion);
            btnRechazar = itemView.findViewById(R.id.btn_rechazar_invitacion);
        }
    }
}
