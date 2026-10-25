package com.freyr.app.ui.group;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.freyr.app.FreyrApplication;
import com.freyr.app.R;
import com.freyr.app.data.model.FamilyGroup;
import com.freyr.app.data.model.Invitation;
import com.freyr.app.data.model.User;
import com.freyr.app.data.repository.FreyrRepository;
import com.freyr.app.ui.adapter.InvitationAdapter;
import java.util.ArrayList;
import java.util.List;

public class InvitationsFragment extends Fragment {

    private RecyclerView rvInvitations;
    private TextView tvNoInvitations;
    private InvitationAdapter invitationAdapter;

    private FreyrRepository repository;
    private List<Invitation> allInvitations = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invitations, container, false);
        repository = ((FreyrApplication) requireActivity().getApplication()).getRepository();

        initViews(view);
        setupRecyclerView();
        observeData();

        return view;
    }

    private void initViews(View view) {
        rvInvitations = view.findViewById(R.id.rv_invitations);
        tvNoInvitations = view.findViewById(R.id.tv_no_invitations);
    }

    private void setupRecyclerView() {
        invitationAdapter = new InvitationAdapter(requireContext(), new InvitationAdapter.OnInvitationResponseListener() {
            @Override
            public void onAccept(Invitation invitation) {
                repository.respondToInvitation(invitation.getId(), true, new FreyrRepository.Callback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() ->
                                Toast.makeText(getActivity(), "¡Invitación aceptada! Te has unido al grupo familiar.", Toast.LENGTH_SHORT).show()
                            );
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() ->
                                Toast.makeText(getActivity(), "No fue posible aceptar la invitación: " + error, Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                });
            }

            @Override
            public void onDecline(Invitation invitation) {
                repository.respondToInvitation(invitation.getId(), false, new FreyrRepository.Callback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() ->
                                Toast.makeText(getActivity(), "Invitación rechazada.", Toast.LENGTH_SHORT).show()
                            );
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() ->
                                Toast.makeText(getActivity(), "No fue posible rechazar la invitación: " + error, Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                });
            }
        });

        rvInvitations.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvInvitations.setAdapter(invitationAdapter);
    }

    private void observeData() {
        String uid = repository.getCurrentUserId();

        repository.getAllGroupsLive().observe(getViewLifecycleOwner(), groups -> {
            if (groups != null) invitationAdapter.setGroups(groups);
        });

        repository.getAllUsersLive().observe(getViewLifecycleOwner(), users -> {
            if (users != null) invitationAdapter.setUsers(users);
        });

        repository.getAllInvitationsLive().observe(getViewLifecycleOwner(), invitations -> {
            if (invitations != null) {
                allInvitations = invitations;
                List<Invitation> pending = new ArrayList<>();
                for (Invitation inv : invitations) {
                    if (inv.getInvitedUserId().equals(uid) && "pending".equalsIgnoreCase(inv.getStatus())) {
                        pending.add(inv);
                    }
                }

                invitationAdapter.setInvitations(pending);
                tvNoInvitations.setVisibility(pending.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }
}
