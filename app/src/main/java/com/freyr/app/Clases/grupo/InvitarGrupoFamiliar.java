package com.freyr.app.Clases.grupo;

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

import com.freyr.app.FreyrApp;
import com.freyr.app.R;
import com.freyr.app.data.model.Grupofamiliar;
import com.freyr.app.data.model.Invitacion;
import com.freyr.app.data.model.Usuario;
import com.freyr.app.data.repositorio.FreyrRepositorio;
import com.freyr.app.Clases.listas.listaDeInvitaciones;

public class InvitarGrupoFamiliar extends Fragment {

    private RecyclerView rvInvitations;
    private TextView tvNoInvitations;
    private listaDeInvitaciones invitationAdapter;

    private FreyrRepositorio repository;
    private Invitacion[] allInvitations = new Invitacion[0];

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

        repository = ((FreyrApp) requireActivity()
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
        invitationAdapter = new listaDeInvitaciones(
                requireContext(),
                new listaDeInvitaciones.AccionesRespuestaInvitacion() {
                    @Override
                    public void Aceptar(Invitacion invitacion) {
                        repository.respondToInvitation(
                                invitacion.getId(),
                                true,
                                new FreyrRepositorio.Callback<Void>() {
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
                    public void Rechazar(Invitacion invitacion) {
                        repository.respondToInvitation(
                                invitacion.getId(),
                                false,
                                new FreyrRepositorio.Callback<Void>() {
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
                        invitationAdapter.setGrupos(
                                groups.toArray(new Grupofamiliar[0])
                        );
                    }
                }
        );

        repository.getAllUsersLive().observe(
                getViewLifecycleOwner(),
                users -> {
                    if (users != null) {
                        invitationAdapter.setUsuarios(
                                users.toArray(new Usuario[0])
                        );
                    }
                }
        );

        repository.getAllInvitationsLive().observe(
                getViewLifecycleOwner(),
                invitations -> {
                    if (invitations != null) {
                        allInvitations = invitations.toArray(new Invitacion[0]);

                        Invitacion[] pendingInvitations =
                                new Invitacion[allInvitations.length];
                        int count = 0;

                        for (Invitacion invitacion : allInvitations) {
                            if (invitacion.getInvitedUserId().equals(userId)
                                    && "pending".equalsIgnoreCase(
                                    invitacion.getStatus()
                            )) {
                                pendingInvitations[count] = invitacion;
                                count++;
                            }
                        }

                        Invitacion[] result = new Invitacion[count];
                        System.arraycopy(
                                pendingInvitations,
                                0,
                                result,
                                0,
                                count
                        );

                        invitationAdapter.setInvitaciones(result);
                        tvNoInvitations.setVisibility(
                                count == 0 ? View.VISIBLE : View.GONE
                        );
                    }
                }
        );
    }
}