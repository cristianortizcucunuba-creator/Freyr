
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
import com.freyr.app.data.model.Grupofamiliar;
import com.freyr.app.data.model.Invitation;
import com.freyr.app.data.model.Usuario;

public class listaDeInvitaciones extends RecyclerView.Adapter<listaDeInvitaciones.InvitationViewHolder> {

    // Define las acciones que se ejecutan al aceptar o rechazar una invitación.
    public interface OnInvitationResponseListener {
        void onAccept(Invitation invitation);
        void onDecline(Invitation invitation);
    }

    private final Context context;
    private final OnInvitationResponseListener listener;

    // Arreglos que almacenan las invitaciones, grupos familiares y usuarios.
    private Invitation[] invitations = new Invitation[0];
    private Grupofamiliar[] allGroups = new Grupofamiliar[0];
    private Usuario[] allUsers = new Usuario[0];

    public listaDeInvitaciones(Context context, OnInvitationResponseListener listener) {
        this.context = context;
        this.listener = listener;
    }

    // Actualiza las invitaciones que se mostrarán en pantalla.
    public void setInvitations(Invitation[] invitations) {
        this.invitations = invitations != null
                ? invitations : new Invitation[0];

        notifyDataSetChanged();
    }

    // Actualiza los grupos familiares disponibles.
    public void setGroups(Grupofamiliar[] groups) {
        this.allGroups = groups != null
                ? groups : new Grupofamiliar[0];

        notifyDataSetChanged();
    }

    // Actualiza la información de los usuarios.
    public void setUsers(Usuario[] users) {
        this.allUsers = users != null
                ? users : new Usuario[0];

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InvitationViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        // Carga el diseño XML correspondiente a una invitación.
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_invitation, parent, false);

        return new InvitationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull InvitationViewHolder holder, int position) {

        // Obtiene la invitación correspondiente a la posición actual.
        Invitation invitation = invitations[position];

        // Busca el nombre del grupo familiar al que pertenece la invitación.
        String groupName = "Grupo Familiar";

        for (Grupofamiliar group : allGroups) {
            if (group.getId().equals(invitation.getFamilyGroupId())) {
                groupName = group.getName();
                break;
            }
        }

        holder.tvGroupName.setText(groupName);

        // Busca al usuario que envió la invitación.
        String inviterName = "Un miembro familiar";

        for (Usuario user : allUsers) {
            if (user.getId().equals(invitation.getInvitedByUserId())) {
                inviterName = user.getNomCompl()
                        + " (@" + user.getNomUsuario() + ")";
                break;
            }
        }

        // Muestra quién envió la invitación.
        holder.tvFrom.setText("Invitado por " + inviterName);

        // Ejecuta la acción correspondiente al aceptar la invitación.
        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAccept(invitation);
            }
        });

        // Ejecuta la acción correspondiente al rechazar la invitación.
        holder.btnDecline.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDecline(invitation);
            }
        });
    }

    @Override
    public int getItemCount() {
        // Devuelve la cantidad de invitaciones almacenadas en el arreglo.
        return invitations.length;
    }

    // Mantiene las referencias a las vistas de cada invitación.
    static class InvitationViewHolder extends RecyclerView.ViewHolder {

        TextView tvGroupName, tvFrom;
        Button btnAccept, btnDecline;

        public InvitationViewHolder(@NonNull View itemView) {
            super(itemView);

            tvGroupName = itemView.findViewById(R.id.tv_invite_group_name);
            tvFrom = itemView.findViewById(R.id.tv_invite_from);

            btnAccept = itemView.findViewById(R.id.btn_accept_invite);
            btnDecline = itemView.findViewById(R.id.btn_decline_invite);
        }
    }
}
