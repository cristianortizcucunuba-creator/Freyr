
package com.freyr.app.Clases.deuda;

// ==========================================
// 1. IMPORTACIONES
// ==========================================

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
import com.freyr.app.Clases.listas.listaDeDeudas;
import com.freyr.app.data.datos.Deuda;
import com.freyr.app.data.datos.Pago;
import com.freyr.app.data.repositorio.FreyrRepositorio;


// ==========================================
// 2. CLASE PRINCIPAL
// ==========================================

public class FinanzasPersonales extends Fragment {

    // ------------------------------------------
    // 2.1. Componentes de la interfaz
    // ------------------------------------------

    private Button btnAñadirDeudaPersonal;
    private RadioGroup rgFilter;
    private RecyclerView rvPersonalDebts;

    // Adaptador encargado de mostrar las deudas.
    private listaDeDeudas debtAdapter;


    // ------------------------------------------
    // 2.2. Repositorio y datos
    // ------------------------------------------

    private FreyrRepositorio repository;

    // Arreglos que almacenan las deudas y los pagos.
    private Deuda[] allDebts = new Deuda[0];
    private Pago[] allPayments = new Pago[0];

    // Filtro seleccionado actualmente.
    private String currentFilter = "all";


    // ==========================================
    // 3. CREACIÓN DEL FRAGMENT
    // ==========================================

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        // Cargar el diseño XML del fragment.
        View view = inflater.inflate(
                R.layout.fragment_personal_finances,
                container,
                false
        );

        // Obtener el repositorio de la aplicación.
        repository = ((FreyrApp) requireActivity()
                .getApplication()).getRepositorio();

        // Inicializar y configurar los componentes.
        initViews(view);
        setupListeners();
        setupRecyclerView();
        observeData();

        return view;
    }


    // ==========================================
    // 4. INICIALIZACIÓN DE COMPONENTES
    // ==========================================

    private void initViews(View view) {

        btnAñadirDeudaPersonal = view.findViewById(
                R.id.btn_add_personal_debt
        );

        rgFilter = view.findViewById(
                R.id.rg_debt_filter
        );

        rvPersonalDebts = view.findViewById(
                R.id.rv_personal_debts
        );
    }


    // ==========================================
    // 5. CONFIGURACIÓN DE EVENTOS
    // ==========================================

    private void setupListeners() {

        // ------------------------------------------
        // 5.1. Añadir una nueva deuda personal
        // ------------------------------------------

        btnAñadirDeudaPersonal.setOnClickListener(v -> {

            Intent intent = new Intent(
                    getActivity(),
                    AñadirDeuda.class
            );

            startActivity(intent);
        });


        // ------------------------------------------
        // 5.2. Cambiar el filtro de las deudas
        // ------------------------------------------

        rgFilter.setOnCheckedChangeListener((group, checkedId) -> {

            if (checkedId == R.id.rb_filter_pending) {

                currentFilter = "pending";

            } else if (checkedId == R.id.rb_filter_paid) {

                currentFilter = "paid";

            } else {

                currentFilter = "all";
            }

            // Actualizar la lista según el filtro seleccionado.
            updateFilteredList();
        });
    }


    // ==========================================
    // 6. CONFIGURACIÓN DEL RECYCLERVIEW
    // ==========================================

    private void setupRecyclerView() {

        // Crear el adaptador y configurar sus acciones.
        debtAdapter = new listaDeDeudas(
                requireContext(),

                new listaDeDeudas.MonitorDeudas() {

                    // Abrir los detalles de una deuda.
                    @Override
                    public void tocDeuda(Deuda debt) {

                        Intent intent = new Intent(
                                getActivity(),
                                DetallesDeuda.class
                        );

                        intent.putExtra("debt_id", debt.getId());
                        startActivity(intent);
                    }


                    // Abrir la pantalla para registrar un abono.
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

        // Configurar el RecyclerView.
        rvPersonalDebts.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        rvPersonalDebts.setAdapter(debtAdapter);
    }


    // ==========================================
    // 7. OBSERVACIÓN DE DATOS
    // ==========================================

    private void observeData() {

        // ------------------------------------------
        // 7.1. Observar todas las deudas
        // ------------------------------------------

        repository.observarTodasLasDeudas().observe(
                getViewLifecycleOwner(),
                debts -> {

                    if (debts != null) {

                        // Convertir la lista recibida en un arreglo.
                        allDebts = debts.toArray(new Deuda[0]);

                        updateFilteredList();
                    }
                }
        );


        // ------------------------------------------
        // 7.2. Observar todos los pagos
        // ------------------------------------------

        repository.observarTodosLosPagos().observe(
                getViewLifecycleOwner(),
                payments -> {

                    if (payments != null) {

                        // Convertir la lista de pagos en un arreglo.
                        allPayments = payments.toArray(new Pago[0]);

                        debtAdapter.setPagos(allPayments);

                        updateFilteredList();
                    }
                }
        );
    }


    // ==========================================
    // 8. FILTRAR DEUDAS PERSONALES
    // ==========================================

    private void updateFilteredList() {

        // Obtener el identificador del usuario actual.
        String uid = repository.obtenerIdUsuarioActual();

        // Arreglo temporal con capacidad para todas las deudas.
        Deuda[] filteredDebts = new Deuda[allDebts.length];

        int count = 0;

        // Recorrer todas las deudas disponibles.
        for (Deuda debt : allDebts) {

            // Verificar que la deuda sea personal
            // y pertenezca al usuario actual.
            boolean isPersonal = debt.getGrupoFamiliarId() == null
                    && (uid.equals(debt.getIdUsuarioAsignado())
                    || uid.equals(debt.getCreadoPor()));

            if (!isPersonal) {
                continue;
            }


            // ------------------------------------------
            // 8.1. Calcular los pagos realizados
            // ------------------------------------------

            double paid = 0.0;

            for (Pago payment : allPayments) {

                if (payment.getDeudaId().equals(debt.getId())) {
                    paid += payment.getMonto();
                }
            }


            // ------------------------------------------
            // 8.2. Calcular el estado de la deuda
            // ------------------------------------------

            double remaining = Math.max(0.0, debt.getMonto() - paid);

            boolean isPaid = remaining == 0.0
                    || "paid".equalsIgnoreCase(debt.getEstado());


            // ------------------------------------------
            // 8.3. Aplicar el filtro seleccionado
            // ------------------------------------------

            if ("pending".equals(currentFilter) && !isPaid) {

                filteredDebts[count++] = debt;

            } else if ("paid".equals(currentFilter) && isPaid) {

                filteredDebts[count++] = debt;

            } else if ("all".equals(currentFilter)) {

                filteredDebts[count++] = debt;
            }
        }


        // ------------------------------------------
        // 8.4. Crear el arreglo con el tamaño exacto
        // ------------------------------------------

        Deuda[] result = new Deuda[count];

        System.arraycopy(
                filteredDebts,
                0,
                result,
                0,
                count
        );

        // Enviar las deudas filtradas al adaptador.
        debtAdapter.setDeudas(result);
    }
}
