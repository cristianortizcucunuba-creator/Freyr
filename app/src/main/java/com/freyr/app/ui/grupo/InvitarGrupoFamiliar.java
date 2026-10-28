package com.freyr.app.ui.grupo;

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
import com.freyr.app.ui.adapter.InvitacionesRecycler;

public class InvitarGrupoFamiliar extends Fragment {

    private RecyclerView rvInvitations;
    private TextView tvNoInvitations;
    private InvitacionesRecycler invitationAdapter;

    private FreyrRepository repository;
    private Invitation[] allInvitations = new Invitation[0];

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_invitations,
                container,
                false
        );

        repository = ((FreyrApplication) requireActivity()
                .getApplication()).getRepository();

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
        invitationAdapter = new InvitacionesRecycler(
                requireContext(),
                new InvitacionesRecycler.OnInvitationResponseListener() {
                    @Override
                    public void onAccept(Invitation invitation) {
                        repository.respondToInvitation(
                                invitation.getId(),
                                true,
                                new FreyrRepository.Callback<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                        if (getActivity() != null) {
                                            getActivity().runOnUiThread(() ->
                                                    Toast.makeText(
                                                            getActivity(),
                                                            "¡Invitación aceptada! Te has unido al grupo familiar.",
                                                            Toast.LENGTH_SHORT
                                                    ).show()
                                            );
                                        }
                                    }

                                    @Override
                                    public void onError(String error) {
                                        if (getActivity() != null) {
                                            getActivity().runOnUiThread(() ->
                                                    Toast.makeText(
                                                            getActivity(),
                                                            "No fue posible aceptar la invitación: " + error,
                                                            Toast.LENGTH_SHORT
                                                    ).show()
                                            );
                                        }
                                    }
                                }
                        );
                    }

                    @Override
                    public void onDecline(Invitation invitation) {
                        repository.respondToInvitation(
                                invitation.getId(),
                                false,
                                new FreyrRepository.Callback<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                        if (getActivity() != null) {
                                            getActivity().runOnUiThread(() ->
                                                    Toast.makeText(
                                                            getActivity(),
                                                            "Invitación rechazada.",
                                                            Toast.LENGTH_SHORT
                                                    ).show()
                                            );
                                        }
                                    }

                                    @Override
                                    public void onError(String error) {
                                        if (getActivity() != null) {
                                            getActivity().runOnUiThread(() ->
                                                    Toast.makeText(
                                                            getActivity(),
                                                            "No fue posible rechazar la invitación: " + error,
                                                            Toast.LENGTH_SHORT
                                                    ).show()
                                            );
                                        }
                                    }
                                }
                        );
                    }
                }
        );

        rvInvitations.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        rvInvitations.setAdapter(invitationAdapter);
    }

    private void observeData() {
        String userId = repository.getCurrentUserId();

        repository.getAllGroupsLive().observe(
                getViewLifecycleOwner(),
                groups -> {
                    if (groups != null) {
                        invitationAdapter.setGroups(
                                groups.toArray(new FamilyGroup[0])
                        );
                    }
                }
        );

        repository.getAllUsersLive().observe(
                getViewLifecycleOwner(),
                users -> {
                    if (users != null) {
                        invitationAdapter.setUsers(
                                users.toArray(new User[0])
                        );
                    }
                }
        );

        repository.getAllInvitationsLive().observe(
                getViewLifecycleOwner(),
                invitations -> {
                    if (invitations != null) {
                        allInvitations = invitations.toArray(new Invitation[0]);

                        Invitation[] pendingInvitations =
                                new Invitation[allInvitations.length];
                        int count = 0;

                        for (Invitation invitation : allInvitations) {
                            if (invitation.getInvitedUserId().equals(userId)
                                    && "pending".equalsIgnoreCase(
                                    invitation.getStatus()
                            )) {
                                pendingInvitations[count] = invitation;
                                count++;
                            }
                        }

                        Invitation[] result = new Invitation[count];
                        System.arraycopy(
                                pendingInvitations,
                                0,
                                result,
                                0,
                                count
                        );

                        invitationAdapter.setInvitations(result);
                        tvNoInvitations.setVisibility(
                                count == 0 ? View.VISIBLE : View.GONE
                        );
                    }
                }
        );
    }
}