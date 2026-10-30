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
import com.freyr.app.data.datos.Grupofamiliar;
import com.freyr.app.data.datos.Invitacion;
import com.freyr.app.data.datos.Usuario;
import com.freyr.app.data.repositorio.FreyrRepositorio;
import com.freyr.app.Clases.listas.listaDeInvitaciones;

public class InvitarGrupoFamiliar extends Fragment {

    private RecyclerView rvInvitaciones;
    private TextView tvSinInvitaciones;
    private listaDeInvitaciones adaptadorInvitaciones;

    private FreyrRepositorio repositorio;
    private Invitacion[] todasLasInvitaciones = new Invitacion[0];

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflador,
            @Nullable ViewGroup contenedor,
            @Nullable Bundle estadoGuardado
    ) {
        View vista = inflador.inflate(
                R.layout.fragment_invitations,
                contenedor,
                false
        );

        repositorio = ((FreyrApp) requireActivity()
                .getApplication()).getRepositorio();

        inicializarVistas(vista);
        configurarLista();
        observarDatos();

        return vista;
    }

    private void inicializarVistas(View vista) {
        rvInvitaciones = vista.findViewById(R.id.rv_invitaciones);
        tvSinInvitaciones = vista.findViewById(R.id.tv_sin_invitaciones);
    }

    private void configurarLista() {
        adaptadorInvitaciones = new listaDeInvitaciones(
                requireContext(),
                new listaDeInvitaciones.AccionesRespuestaInvitacion() {
                    @Override
                    public void Aceptar(Invitacion invitacion) {
                        repositorio.responderInvitacion(
                                invitacion.getId(),
                                true,
                                new FreyrRepositorio.Callback<Void>() {
                                    @Override
                                    public void alTenerExito(Void resultado) {
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
                                    public void alOcurrirError(String error) {
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
                        repositorio.responderInvitacion(
                                invitacion.getId(),
                                false,
                                new FreyrRepositorio.Callback<Void>() {
                                    @Override
                                    public void alTenerExito(Void result) {
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
                                    public void alOcurrirError(String error) {
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

        rvInvitaciones.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        rvInvitaciones.setAdapter(adaptadorInvitaciones);
    }

    private void observarDatos() {
        repositorio.observarTodosLosGrupos().observe(
                getViewLifecycleOwner(),
                groups -> {
                    if (groups != null) {
                        adaptadorInvitaciones.setGrupos(
                                groups.toArray(new Grupofamiliar[0])
                        );
                    }
                }
        );

        repositorio.observarTodosLosUsuarios().observe(
                getViewLifecycleOwner(),
                users -> {
                    if (users != null) {
                        adaptadorInvitaciones.setUsuarios(
                                users.toArray(new Usuario[0])
                        );
                    }
                }
        );

        repositorio.observarTodasLasInvitaciones().observe(
                getViewLifecycleOwner(),
                invitaciones -> {
                    if (invitaciones != null) {
                        todasLasInvitaciones = invitaciones.toArray(new Invitacion[0]);

                        Invitacion[] invitacionesPendientes =
                                new Invitacion[todasLasInvitaciones.length];
                        int cantidad = 0;

                        String idUsuarioActual = repositorio.obtenerIdUsuarioActual();

                        for (Invitacion invitacion : todasLasInvitaciones) {
                            if (idUsuarioActual != null
                                    && idUsuarioActual.equals(invitacion.getIdUsuarioInvitado())
                                    && "pendiente".equalsIgnoreCase(invitacion.getEstado())) {

                                invitacionesPendientes[cantidad] = invitacion;
                                cantidad++;
                            }
                        }

                        Invitacion[] resultado = new Invitacion[cantidad];
                        System.arraycopy(
                                invitacionesPendientes ,
                                0,
                                resultado,
                                0,
                                cantidad
                        );

                        adaptadorInvitaciones.setInvitaciones(resultado);
                        tvSinInvitaciones.setVisibility(
                                cantidad == 0 ? View.VISIBLE : View.GONE
                        );
                    }
                }
        );
    }
}