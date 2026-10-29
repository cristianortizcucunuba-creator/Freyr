package com.freyr.app.Clases.deuda;

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

import com.freyr.app.FreyrApplication;
import com.freyr.app.R;
import com.freyr.app.data.model.Deuda;
import com.freyr.app.data.model.Pago;
import com.freyr.app.data.model.Usuario;
import com.freyr.app.data.repository.FreyrRepositorio;
import com.freyr.app.Clases.listas.listaDePagos;

public class DetallesDeuda extends AppCompatActivity {

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
    private listaDePagos paymentAdapter;

    private FreyrRepositorio repository;
    private String debtId;
    private Deuda currentDebt;

    // Conservan en arreglos los datos recibidos.
    private Usuario[] allUsers = new Usuario[0];
    private Pago[] allPayments = new Pago[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debt_detail);

        repository = ((FreyrApplication) getApplication()).getRepository();
        debtId = getIntent().getStringExtra("debt_id");

        if (debtId == null) {
            finish();
            return;
        }

        initViews();
        setupListeners();
        setupRecyclerView();
        observeData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_volver);
        btnDeleteDebt = findViewById(R.id.btn_delete_debt);
        tvTitle = findViewById(R.id.tv_title);
        tvDebtName = findViewById(R.id.tx_nombre_deuda);
        tvDebtDesc = findViewById(R.id.tv_debt_desc);
        tvDebtStatus = findViewById(R.id.tv_debt_status);
        tvAssignedInfo = findViewById(R.id.tv_assigned_info);
        tvTotalAmt = findViewById(R.id.tx_etiqueta_admin);
        tvPaidAmt = findViewById(R.id.tv_paid_amt);
        tvRemainingAmt = findViewById(R.id.tv_remaining_amt);
        pbDetailProgress = findViewById(R.id.pb_detail_progress);

        tvInstallmentsCount = findViewById(R.id.tv_installments_count);
        tvNoPayments = findViewById(R.id.tv_no_payments);
        btnAddPayment = findViewById(R.id.btn_add_payment);
        rvPayments = findViewById(R.id.rv_payments);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnDeleteDebt.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Eliminar deuda")
                    .setMessage("¿Estás seguro de que deseas eliminar esta deuda y su historial de abonos?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        repository.deleteDebt(
                                debtId,
                                new FreyrRepositorio.Callback<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {
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
                                    public void onError(String error) {
                                    }
                                }
                        );
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        btnAddPayment.setOnClickListener(v -> {
            Intent intent = new Intent(
                    DetallesDeuda.this,
                    RegistrarAbonosDeuda.class
            );
            intent.putExtra("debt_id", debtId);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        paymentAdapter = new listaDePagos(this, payment -> {
            new AlertDialog.Builder(this)
                    .setTitle("Eliminar abono")
                    .setMessage(
                            "¿Deseas eliminar este abono de "
                                    + FreyrRepositorio.formatCurrency(payment.getMonto())
                                    + "?"
                    )
                    .setPositiveButton("Eliminar", (dialog, which) ->
                            repository.deletePayment(payment.getId(), debtId, null)
                    )
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        rvPayments.setLayoutManager(new LinearLayoutManager(this));
        rvPayments.setAdapter(paymentAdapter);
    }

    private void observeData() {
        repository.getAllUsersLive().observe(this, users -> {
            if (users != null) {
                // El repositorio entrega una lista; la convertimos a arreglo.
                allUsers = users.toArray(new Usuario[0]);

                paymentAdapter.setUsuarios(allUsers);
                updateAssignedLabel();
            }
        });

        repository.getAllDebtsLive().observe(this, debts -> {
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

        repository.getPaymentsForDebtLive(debtId).observe(this, payments -> {
            if (payments != null) {
                // El adapter trabaja con arreglos.
                allPayments = payments.toArray(new Pago[0]);
                paymentAdapter.setPagos(allPayments);

                int paymentCount = allPayments.length;
                String countLabel = paymentCount == 1 ? "ABONO" : "ABONOS";
                tvInstallmentsCount.setText(
                        "HISTORIAL DE ABONOS (" + paymentCount + " " + countLabel + ")"
                );

                tvNoPayments.setVisibility(
                        paymentCount == 0 ? View.VISIBLE : View.GONE
                );

                updateDebtInfo();
            }
        });
    }

    private void updateDebtInfo() {
        if (currentDebt == null) {
            return;
        }

        tvTitle.setText(currentDebt.getName());
        tvDebtName.setText(currentDebt.getName());

        String description = currentDebt.getDescription();
        if (description != null && !description.trim().isEmpty()) {
            tvDebtDesc.setText(description);
            tvDebtDesc.setVisibility(View.VISIBLE);
        } else {
            tvDebtDesc.setVisibility(View.GONE);
        }

        double total = currentDebt.getAmount();
        double paid = 0.0;

        for (Pago payment : allPayments) {
            paid += payment.getMonto();
        }

        double remaining = Math.max(0.0, total - paid);
        boolean isPaid = remaining == 0.0
                || "paid".equalsIgnoreCase(currentDebt.getStatus());

        tvTotalAmt.setText(FreyrRepositorio.formatCurrency(total));
        tvPaidAmt.setText(FreyrRepositorio.formatCurrency(paid));
        tvRemainingAmt.setText(FreyrRepositorio.formatCurrency(remaining));

        if (isPaid) {
            tvDebtStatus.setText("PAGADA EN SU TOTALIDAD");
            tvDebtStatus.setTextColor(
                    ContextCompat.getColor(this, R.color.freyr_status_paid)
            );
            tvDebtStatus.setBackgroundResource(R.drawable.bg_status_paid);
            btnAddPayment.setVisibility(View.GONE);
            pbDetailProgress.setProgress(100);
        } else {
            tvDebtStatus.setText("PENDIENTE");
            tvDebtStatus.setTextColor(
                    ContextCompat.getColor(this, R.color.freyr_primary)
            );
            tvDebtStatus.setBackgroundResource(R.drawable.bg_status_pending);
            btnAddPayment.setVisibility(View.VISIBLE);

            int percent = total > 0
                    ? (int) Math.min(100, (paid / total) * 100)
                    : 0;
            pbDetailProgress.setProgress(percent);
        }
    }

    // Actualiza el nombre de la persona responsable, si corresponde.
    private void updateAssignedLabel() {
        if (currentDebt == null) {
            return;
        }

        String assignedUserId = currentDebt.getAssignedUserId();
        if (assignedUserId == null) {
            tvAssignedInfo.setText(
                    "Responsable: Toda la familia (Compartida)"
            );
            return;
        }

        String name = "Miembro (" + assignedUserId + ")";
        for (Usuario user : allUsers) {
            if (user.getId().equals(assignedUserId)) {
                name = user.getNomCompl() + " (@" + user.getNomUsuario() + ")";
                break;
            }
        }

        tvAssignedInfo.setText("Responsable: " + name);
    }
}