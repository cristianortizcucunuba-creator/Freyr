
package com.freyr.app.Clases.deuda;

// ==========================================
// 1. IMPORTACIONES
// ==========================================

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.freyr.app.FreyrApp;
import com.freyr.app.R;
import com.freyr.app.Clases.listas.listaDePagos;
import com.freyr.app.data.datos.Deuda;
import com.freyr.app.data.datos.Pago;
import com.freyr.app.data.datos.Usuario;
import com.freyr.app.data.repositorio.FreyrRepositorio;


// ==========================================
// 2. CLASE PRINCIPAL
// ==========================================

public class DetallesDeuda extends AppCompatActivity {

    // ------------------------------------------
    // 2.1. Componentes de la interfaz
    // ------------------------------------------

    private ImageButton btnBack;
    private ImageButton btnDeleteDebt;

    private TextView tvTitle;
    private TextView tvDebtName;
    private TextView tvDebtDesc;
    private TextView tvDebtStatus;
    private TextView tvAssignedInfo;

    private TextView tvTotalAmt;
    private TextView tvPaidAmt;
    private TextView tvRemainingAmt;

    private ProgressBar pbDetailProgress;

    private TextView tvInstallmentsCount;
    private TextView tvNoPayments;

    private Button btnAddPayment;
    private RecyclerView rvPayments;

    // Adaptador que muestra los abonos de la deuda.
    private listaDePagos paymentAdapter;


    // ------------------------------------------
    // 2.2. Datos y repositorio
    // ------------------------------------------

    private FreyrRepositorio repository;

    private String debtId;
    private Deuda currentDebt;

    // Arreglos que almacenan los datos recibidos.
    private Usuario[] allUsers = new Usuario[0];
    private Pago[] allPayments = new Pago[0];


    // ==========================================
    // 3. CICLO DE VIDA DE LA ACTIVIDAD
    // ==========================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debt_detail);

        // Obtener el repositorio y el identificador de la deuda.
        repository = ((FreyrApp) getApplication()).getRepositorio();
        debtId = getIntent().getStringExtra("debt_id");

        // Si no se recibió un identificador, cerrar la actividad.
        if (debtId == null) {
            finish();
            return;
        }

        // Inicializar los componentes y configurar la pantalla.
        initViews();
        setupListeners();
        setupRecyclerView();
        observeData();
    }


    // ==========================================
    // 4. INICIALIZACIÓN DE COMPONENTES
    // ==========================================

    private void initViews() {

        // Componentes principales.
        btnBack = findViewById(R.id.btn_atras);
        btnDeleteDebt = findViewById(R.id.btn_delete_debt);

        tvTitle = findViewById(R.id.tv_title);
        tvDebtName = findViewById(R.id.tx_nombre_deuda);
        tvDebtDesc = findViewById(R.id.tx_descript_deuda);
        tvDebtStatus = findViewById(R.id.tx_deuda_estado);
        tvAssignedInfo = findViewById(R.id.tv_assigned_info);

        // Información de los valores de la deuda.
        tvTotalAmt = findViewById(R.id.tx_etiqueta_admin);
        tvPaidAmt = findViewById(R.id.tv_paid_amt);
        tvRemainingAmt = findViewById(R.id.tv_remaining_amt);

        pbDetailProgress = findViewById(R.id.pb_detail_progress);

        // Componentes del historial de abonos.
        tvInstallmentsCount = findViewById(R.id.tv_installments_count);
        tvNoPayments = findViewById(R.id.tv_no_payments);

        btnAddPayment = findViewById(R.id.btn_add_payment);
        rvPayments = findViewById(R.id.rv_payments);
    }


    // ==========================================
    // 5. CONFIGURACIÓN DE EVENTOS
    // ==========================================

    private void setupListeners() {

        // Regresar a la pantalla anterior.
        btnBack.setOnClickListener(v -> finish());


        // ------------------------------------------
        // 5.1. Eliminar deuda
        // ------------------------------------------

        btnDeleteDebt.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Eliminar deuda")
                    .setMessage(
                            "¿Estás seguro de que deseas eliminar esta deuda y su historial de abonos?"
                    )
                    .setPositiveButton("Eliminar", (dialog, which) -> {

                        repository.eliminarDeuda(
                                debtId,
                                new FreyrRepositorio.Callback<Void>() {

                                    @Override
                                    public void alTenerExito(Void result) {

                                        runOnUiThread(() -> {

                                            Toast.makeText(
                                                    DetallesDeuda.this,
                                                    "Deuda eliminada",
                                                    Toast.LENGTH_SHORT
                                            ).show();

                                            finish();
                                        });
                                    }

                                    @Override
                                    public void alOcurrirError(String error) {
                                    }
                                }
                        );
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });


        // ------------------------------------------
        // 5.2. Registrar un nuevo abono
        // ------------------------------------------

        btnAddPayment.setOnClickListener(v -> {

            Intent intent = new Intent(
                    DetallesDeuda.this,
                    RegistrarAbonosDeuda.class
            );

            intent.putExtra("debt_id", debtId);
            startActivity(intent);
        });
    }


    // ==========================================
    // 6. CONFIGURACIÓN DEL RECYCLERVIEW
    // ==========================================

    private void setupRecyclerView() {

        // Crear el adaptador y configurar la acción de eliminar abonos.
        paymentAdapter = new listaDePagos(this, payment -> {

            new AlertDialog.Builder(this)
                    .setTitle("Eliminar abono")
                    .setMessage(
                            "¿Deseas eliminar este abono de "
                                    + FreyrRepositorio.formatearMoneda(payment.getMonto())
                                    + "?"
                    )
                    .setPositiveButton(
                            "Eliminar",
                            (dialog, which) ->
                                    repository.eliminarPago(payment.getId(), debtId, null)
                    )
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        // Configurar el RecyclerView.
        rvPayments.setLayoutManager(new LinearLayoutManager(this));
        rvPayments.setAdapter(paymentAdapter);
    }


    // ==========================================
    // 7. OBSERVACIÓN DE DATOS
    // ==========================================

    private void observeData() {

        // ------------------------------------------
        // 7.1. Observar todos los usuarios
        // ------------------------------------------

        repository.observarTodosLosUsuarios().observe(this, users -> {

            if (users != null) {

                // Convertir la lista recibida en un arreglo.
                allUsers = users.toArray(new Usuario[0]);

                paymentAdapter.setUsuarios(allUsers);
                updateAssignedLabel();
            }
        });


        // ------------------------------------------
        // 7.2. Observar todas las deudas
        // ------------------------------------------

        repository.observarTodasLasDeudas().observe(this, debts -> {

            if (debts != null) {

                for (Deuda debt : debts) {

                    if (debt.getId().equals(debtId)) {

                        currentDebt = debt;

                        updateDebtInfo();
                        updateAssignedLabel();

                        break;
                    }
                }
            }
        });


        // ------------------------------------------
        // 7.3. Observar los abonos de la deuda
        // ------------------------------------------

        repository.observarPagosDeDeuda(debtId).observe(this, payments -> {

            if (payments != null) {

                // Convertir la lista de pagos en un arreglo.
                allPayments = payments.toArray(new Pago[0]);

                paymentAdapter.setPagos(allPayments);

                // Actualizar la cantidad de abonos.
                int paymentCount = allPayments.length;

                String countLabel = paymentCount == 1
                        ? "ABONO"
                        : "ABONOS";

                tvInstallmentsCount.setText(
                        "HISTORIAL DE ABONOS (" + paymentCount + " " + countLabel + ")"
                );

                // Mostrar u ocultar el mensaje cuando no hay abonos.
                tvNoPayments.setVisibility(
                        paymentCount == 0 ? View.VISIBLE : View.GONE
                );

                updateDebtInfo();
            }
        });
    }


    // ==========================================
    // 8. ACTUALIZAR INFORMACIÓN DE LA DEUDA
    // ==========================================

    private void updateDebtInfo() {

        if (currentDebt == null) {
            return;
        }

        // ------------------------------------------
        // 8.1. Mostrar nombre y descripción
        // ------------------------------------------

        tvTitle.setText(currentDebt.getNombre());
        tvDebtName.setText(currentDebt.getNombre());

        String description = currentDebt.getDescripcion();

        if (description != null && !description.trim().isEmpty()) {

            tvDebtDesc.setText(description);
            tvDebtDesc.setVisibility(View.VISIBLE);

        } else {

            tvDebtDesc.setVisibility(View.GONE);
        }


        // ------------------------------------------
        // 8.2. Calcular valores de la deuda
        // ------------------------------------------

        double total = currentDebt.getMonto();
        double paid = 0.0;

        // Sumar todos los abonos registrados.
        for (Pago payment : allPayments) {
            paid += payment.getMonto();
        }

        // Calcular el saldo restante.
        double remaining = Math.max(0.0, total - paid);

        boolean isPaid = remaining == 0.0
                || "paid".equalsIgnoreCase(currentDebt.getEstado());


        // ------------------------------------------
        // 8.3. Mostrar los valores monetarios
        // ------------------------------------------

        tvTotalAmt.setText(FreyrRepositorio.formatearMoneda(total));
        tvPaidAmt.setText(FreyrRepositorio.formatearMoneda(paid));
        tvRemainingAmt.setText(FreyrRepositorio.formatearMoneda(remaining));


        // ------------------------------------------
        // 8.4. Actualizar el estado de la deuda
        // ------------------------------------------

        if (isPaid) {

            // La deuda ya está pagada.
            tvDebtStatus.setText("PAGADA EN SU TOTALIDAD");

            tvDebtStatus.setTextColor(
                    ContextCompat.getColor(this, R.color.freyr_status_paid)
            );

            tvDebtStatus.setBackgroundResource(R.drawable.bg_status_paid);

            btnAddPayment.setVisibility(View.GONE);
            pbDetailProgress.setProgress(100);

        } else {

            // La deuda todavía tiene un saldo pendiente.
            tvDebtStatus.setText("PENDIENTE");

            tvDebtStatus.setTextColor(
                    ContextCompat.getColor(this, R.color.freyr_primary)
            );

            tvDebtStatus.setBackgroundResource(R.drawable.bg_status_pending);

            btnAddPayment.setVisibility(View.VISIBLE);

            // Calcular el porcentaje de progreso.
            int percent = total > 0
                    ? (int) Math.min(100, (paid / total) * 100)
                    : 0;

            pbDetailProgress.setProgress(percent);
        }
    }


    // ==========================================
    // 9. ACTUALIZAR INFORMACIÓN DEL RESPONSABLE
    // ==========================================

    // Muestra el nombre de la persona responsable de la deuda.
    private void updateAssignedLabel() {

        if (currentDebt == null) {
            return;
        }

        String assignedUserId = currentDebt.getIdUsuarioAsignado();

        // Si no hay un usuario asignado, la deuda es compartida.
        if (assignedUserId == null) {

            tvAssignedInfo.setText(
                    "Responsable: Toda la familia (Compartida)"
            );

            return;
        }

        // Valor inicial por si no se encuentra al usuario.
        String name = "Miembro (" + assignedUserId + ")";

        // Buscar el usuario responsable dentro del arreglo.
        for (Usuario user : allUsers) {

            if (user.getId().equals(assignedUserId)) {

                name = user.getNomCompl()
                        + " (@"
                        + user.getNomUsuario()
                        + ")";

                break;
            }
        }

        tvAssignedInfo.setText("Responsable: " + name);
    }
}
