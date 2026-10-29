package com.freyr.app.Clases.deuda;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.freyr.app.FreyrApp;
import com.freyr.app.R;
import com.freyr.app.data.datos.Deuda;
import com.freyr.app.data.datos.Pago;
import com.freyr.app.data.repositorio.FreyrRepositorio;
import com.freyr.app.Clases.listas.listaDeDeudas;

public class FinanzasPersonales extends Fragment {

    private Button btnAddPersonalDebt;
    private RadioGroup rgFilter;
    private RecyclerView rvPersonalDebts;
    private listaDeDeudas debtAdapter;

    private FreyrRepositorio repository;

    // Guarda las deudas y los pagos en arreglos.
    private Deuda[] allDebts = new Deuda[0];
    private Pago[] allPayments = new Pago[0];
    private String currentFilter = "all";

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_personal_finances,
                container,
                false
        );

        repository = ((FreyrApp) requireActivity()
                .getApplication()).getRepositorio();

        initViews(view);
        setupListeners();
        setupRecyclerView();
        observeData();

        return view;
    }

    private void initViews(View view) {
        btnAddPersonalDebt = view.findViewById(R.id.btn_add_personal_debt);
        rgFilter = view.findViewById(R.id.rg_debt_filter);
        rvPersonalDebts = view.findViewById(R.id.rv_personal_debts);
    }

    private void setupListeners() {
        btnAddPersonalDebt.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AñadirDeuda.class);
            startActivity(intent);
        });

        rgFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_filter_pending) {
                currentFilter = "pending";
            } else if (checkedId == R.id.rb_filter_paid) {
                currentFilter = "paid";
            } else {
                currentFilter = "all";
            }

            updateFilteredList();
        });
    }

    private void setupRecyclerView() {
        debtAdapter = new listaDeDeudas(
                requireContext(),
                new listaDeDeudas.MonitorDeudas() {
                    @Override
                    public void tocDeuda(Deuda debt) {
                        Intent intent = new Intent(
                                getActivity(),
                                DetallesDeuda.class
                        );
                        intent.putExtra("debt_id", debt.getId());
                        startActivity(intent);
                    }

                    @Override
                    public void tocPagar(Deuda debt) {
                        Intent intent = new Intent(
                                getActivity(),
                                RegistrarAbonosDeuda.class
                        );
                        intent.putExtra("debt_id", debt.getId());
                        startActivity(intent);
                    }
                }
        );

        rvPersonalDebts.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        rvPersonalDebts.setAdapter(debtAdapter);
    }

    private void observeData() {
        repository.observarTodasLasDeudas().observe(getViewLifecycleOwner(), debts -> {
            if (debts != null) {
                // Convierte los datos del repositorio a arreglos.
                allDebts = debts.toArray(new Deuda[0]);
                updateFilteredList();
            }
        });

        repository.observarTodosLosPagos().observe(
                getViewLifecycleOwner(),
                payments -> {
                    if (payments != null) {
                        allPayments = payments.toArray(new Pago[0]);
                        debtAdapter.setPagos(allPayments);
                        updateFilteredList();
                    }
                }
        );
    }

    // Filtra las deudas personales según el estado seleccionado.
    private void updateFilteredList() {
        String uid = repository.obtenerIdUsuarioActual();

        // El arreglo temporal puede contener como máximo todas las deudas.
        Deuda[] filteredDebts = new Deuda[allDebts.length];
        int count = 0;

        for (Deuda debt : allDebts) {
            boolean isPersonal = debt.getGrupoFamiliarId() == null
                    && (uid.equals(debt.getIdUsuarioAsignado())
                    || uid.equals(debt.getCreadoPor()));

            if (!isPersonal) {
                continue;
            }

            double paid = 0.0;
            for (Pago payment : allPayments) {
                if (payment.getDeudaId().equals(debt.getId())) {
                    paid += payment.getMonto();
                }
            }

            double remaining = Math.max(0.0, debt.getMonto() - paid);
            boolean isPaid = remaining == 0.0
                    || "paid".equalsIgnoreCase(debt.getEstado());

            if ("pending".equals(currentFilter) && !isPaid) {
                filteredDebts[count++] = debt;
            } else if ("paid".equals(currentFilter) && isPaid) {
                filteredDebts[count++] = debt;
            } else if ("all".equals(currentFilter)) {
                filteredDebts[count++] = debt;
            }
        }

        // Envía al adapter un arreglo del tamaño justo.
        Deuda[] result = new Deuda[count];
        System.arraycopy(filteredDebts, 0, result, 0, count);
        debtAdapter.setDeudas(result);
    }
}