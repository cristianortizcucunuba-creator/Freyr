package com.freyr.app.Clases.grupo;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.freyr.app.FreyrApp;
import com.freyr.app.R;
import com.freyr.app.data.datos.Deuda;
import com.freyr.app.data.datos.Grupofamiliar;
import com.freyr.app.data.datos.Pago;
import com.freyr.app.data.repositorio.FreyrRepositorio;
import com.freyr.app.Clases.listas.listaDeGruposFamiliares;

public class GruposFamiliares extends Fragment {

    private Button btnCreargrupo;
    private RecyclerView rvGruposFamiliares;
    private listaDeGruposFamiliares adaptadorGrupos;

    private FreyrRepositorio repositorio;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflador,
            @Nullable ViewGroup contenedor,
            @Nullable Bundle estadoGuardado
    ) {
        View vista = inflador.inflate(
                R.layout.fragment_family_groups,
                contenedor,
                false
        );

        repositorio = ((FreyrApp) requireActivity()
                .getApplication()).getRepository();

        inicializarVistas(vista);
        configurarAcciones();
        configurarLista();
        observarDatos();

        return vista;
    }

    private void inicializarVistas(View vista) {
        btnCreargrupo = vista.findViewById(R.id.btn_crear_grupo);
        rvGruposFamiliares = vista.findViewById(R.id.rv_fam_grupo);
    }

    private void configurarAcciones() {
        btnCreargrupo.setOnClickListener(v -> {
            Intent intencion = new Intent(
                    getActivity(),
                    CrearGrupoFamiliar.class
            );
            startActivity(intencion);
        });
    }

    private void configurarLista() {
        adaptadorGrupos = new listaDeGruposFamiliares(
                requireContext(),
                grupo -> {
                    Intent intencion = new Intent(
                            getActivity(),
                            ConfigurarCuentaFamiliar.class
                    );
                    intencion.putExtra("group_id", grupo.getId());
                    startActivity(intencion);
                }
        );

        rvGruposFamiliares.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        rvGruposFamiliares.setAdapter(adaptadorGrupos);
    }

    private void observarDatos() {
        String uid = repositorio.getCurrentUserId();
        adaptadorGrupos.setIdUsuarioActual(uid);

        repositorio.getAllGroupsLive().observe(
                getViewLifecycleOwner(),
                groups -> {
                    if (groups != null) {
                        // Filtra los grupos del usuario en un arreglo.
                        Grupofamiliar[] gruposCoincidentes =
                                new Grupofamiliar[groups.size()];
                        int cantidad = 0;

                        for (Grupofamiliar grupo : groups) {
                            if (grupo.getMiembrosIds() != null
                                    && grupo.getMiembrosIds().contains(uid)) {
                                gruposCoincidentes[cantidad] = grupo;
                                cantidad++;
                            }
                        }

                        // Ajusta el tamaño al número de grupos encontrados.
                        Grupofamiliar[] gruposDelUsuario = new Grupofamiliar[cantidad];
                        System.arraycopy(
                                gruposCoincidentes,
                                0,
                                gruposDelUsuario,
                                0,
                                cantidad
                        );

                        adaptadorGrupos.setGrupos(gruposDelUsuario);
                    }
                }
        );

        repositorio.getAllDebtsLive().observe(
                getViewLifecycleOwner(),
                debts -> {
                    if (debts != null) {
                        adaptadorGrupos.setDeudas(
                                debts.toArray(new Deuda[0])
                        );
                    }
                }
        );

        repositorio.getAllPaymentsLive().observe(
                getViewLifecycleOwner(),
                payments -> {
                    if (payments != null) {
                        adaptadorGrupos.setPagos(
                                payments.toArray(new Pago[0])
                        );
                    }
                }
        );
    }
}