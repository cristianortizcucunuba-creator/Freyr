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

import com.freyr.app.FreyrApplication;
import com.freyr.app.R;
import com.freyr.app.data.model.Deuda;
import com.freyr.app.data.model.Grupofamiliar;
import com.freyr.app.data.model.Pago;
import com.freyr.app.data.repository.FreyrRepository;
import com.freyr.app.Clases.listas.listaDeGruposFamiliares;

public class GruposFamiliares extends Fragment {

    private Button btnCreateGroup;
    private RecyclerView rvFamilyGroups;
    private listaDeGruposFamiliares groupAdapter;

    private FreyrRepository repository;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_family_groups,
                container,
                false
        );

        repository = ((FreyrApplication) requireActivity()
                .getApplication()).getRepository();

        initViews(view);
        setupListeners();
        setupRecyclerView();
        observeData();

        return view;
    }

    private void initViews(View view) {
        btnCreateGroup = view.findViewById(R.id.btn_create_group);
        rvFamilyGroups = view.findViewById(R.id.rv_family_groups);
    }

    private void setupListeners() {
        btnCreateGroup.setOnClickListener(v -> {
            Intent intent = new Intent(
                    getActivity(),
                    CrearGrupoFamiliar.class
            );
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        groupAdapter = new listaDeGruposFamiliares(
                requireContext(),
                group -> {
                    Intent intent = new Intent(
                            getActivity(),
                            ConfigurarCuentaFamiliar.class
                    );
                    intent.putExtra("group_id", group.getId());
                    startActivity(intent);
                }
        );

        rvFamilyGroups.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        rvFamilyGroups.setAdapter(groupAdapter);
    }

    private void observeData() {
        String uid = repository.getCurrentUserId();
        groupAdapter.setIdUsuarioActual(uid);

        repository.getAllGroupsLive().observe(
                getViewLifecycleOwner(),
                groups -> {
                    if (groups != null) {
                        // Filtra los grupos del usuario en un arreglo.
                        Grupofamiliar[] matchingGroups =
                                new Grupofamiliar[groups.size()];
                        int count = 0;

                        for (Grupofamiliar group : groups) {
                            if (group.getMiembrosIds() != null
                                    && group.getMiembrosIds().contains(uid)) {
                                matchingGroups[count] = group;
                                count++;
                            }
                        }

                        // Ajusta el tamaño al número de grupos encontrados.
                        Grupofamiliar[] userGroups = new Grupofamiliar[count];
                        System.arraycopy(
                                matchingGroups,
                                0,
                                userGroups,
                                0,
                                count
                        );

                        groupAdapter.setGrupos(userGroups);
                    }
                }
        );

        repository.getAllDebtsLive().observe(
                getViewLifecycleOwner(),
                debts -> {
                    if (debts != null) {
                        groupAdapter.setDeudas(
                                debts.toArray(new Deuda[0])
                        );
                    }
                }
        );

        repository.getAllPaymentsLive().observe(
                getViewLifecycleOwner(),
                payments -> {
                    if (payments != null) {
                        groupAdapter.setPagos(
                                payments.toArray(new Pago[0])
                        );
                    }
                }
        );
    }
}