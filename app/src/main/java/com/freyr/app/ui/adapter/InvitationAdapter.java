package com.freyr.app.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.freyr.app.R;
import com.freyr.app.data.model.FamilyGroup;
import com.freyr.app.data.model.Invitation;
import com.freyr.app.data.model.User;
import java.util.ArrayList;
import java.util.List;

public class InvitationAdapter extends RecyclerView.Adapter<InvitationAdapter.InvitationViewHolder> {

    public interface OnInvitationResponseListener {
        void onAccept(Invitation invitation);
        void onDecline(Invitation invitation);
    }

    private final Context context;
    private List<Invitation> invitations = new ArrayList<>();
    private List<FamilyGroup> allGroups = new ArrayList<>();
    private List<User> allUsers = new ArrayList<>();
    private final OnInvitationResponseListener listener;

    public InvitationAdapter(Context context, OnInvitationResponseListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setInvitations(List<Invitation> invitations) {
        this.invitations = invitations != null ? invitations : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setGroups(List<FamilyGroup> groups) {
        this.allGroups = groups != null ? groups : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setUsers(List<User> users) {
        this.allUsers = users != null ? users : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InvitationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_invitation, parent, false);
        return new InvitationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvitationViewHolder holder, int position) {
        Invitation invitation = invitations.get(position);

        String groupName = "Grupo Familiar";
        for (FamilyGroup g : allGroups) {
            if (g.getId().equals(invitation.getFamilyGroupId())) {
                groupName = g.getName();
                break;
            }
        }
        holder.tvGroupName.setText(groupName);

        String inviterName = "Un miembro familiar";
        for (User u : allUsers) {
            if (u.getId().equals(invitation.getInvitedByUserId())) {
                inviterName = u.getFullName() + " (@" + u.getUsername() + ")";
                break;
            }
        }
        holder.tvFrom.setText("Invitado por " + inviterName);

        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) listener.onAccept(invitation);
        });

        holder.btnDecline.setOnClickListener(v -> {
            if (listener != null) listener.onDecline(invitation);
        });
    }

    @Override
    public int getItemCount() {
        return invitations.size();
    }

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
