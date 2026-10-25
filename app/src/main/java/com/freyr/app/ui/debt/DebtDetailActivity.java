package com.freyr.app.ui.debt;

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
import com.freyr.app.data.model.Debt;
import com.freyr.app.data.model.FamilyGroup;
import com.freyr.app.data.model.Payment;
import com.freyr.app.data.model.User;
import com.freyr.app.data.repository.FreyrRepository;
import com.freyr.app.ui.adapter.PaymentAdapter;
import java.util.List;

/**
 * Actividad que despliega el detalle completo y desglose financiero de una deuda.
 * Presenta el progreso de amortización, saldo pendiente, usuario asignado y el historial
 * de abonos realizados con opciones para registrar nuevos pagos o eliminarlos.
 */
public class DebtDetailActivity extends AppCompatActivity {

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
    private PaymentAdapter paymentAdapter;

    private FreyrRepository repository;
    private String debtId;
    private Debt currentDebt;

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
        btnBack = findViewById(R.id.btn_back);
        btnDeleteDebt = findViewById(R.id.btn_delete_debt);
        tvTitle = findViewById(R.id.tv_title);
        tvDebtName = findViewById(R.id.tv_debt_name);
        tvDebtDesc = findViewById(R.id.tv_debt_desc);
        tvDebtStatus = findViewById(R.id.tv_debt_status);
        tvAssignedInfo = findViewById(R.id.tv_assigned_info);
        tvTotalAmt = findViewById(R.id.tv_total_amt);
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
                    repository.deleteDebt(debtId, new FreyrRepository.Callback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            runOnUiThread(() -> {
                                Toast.makeText(DebtDetailActivity.this, "Deuda eliminada", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }

                        @Override
                        public void onError(String error) {}
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
        });

        btnAddPayment.setOnClickListener(v -> {
            Intent intent = new Intent(DebtDetailActivity.this, AddPaymentActivity.class);
            intent.putExtra("debt_id", debtId);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        paymentAdapter = new PaymentAdapter(this, payment -> {
            new AlertDialog.Builder(this)
                .setTitle("Eliminar abono")
                .setMessage("¿Deseas eliminar este abono de " + FreyrRepository.formatCurrency(payment.getAmount()) + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    repository.deletePayment(payment.getId(), debtId, null);
                })
                .setNegativeButton("Cancelar", null)
                .show();
        });

        rvPayments.setLayoutManager(new LinearLayoutManager(this));
        rvPayments.setAdapter(paymentAdapter);
    }

    private void observeData() {
        repository.getAllUsersLive().observe(this, users -> {
            if (users != null) {
                paymentAdapter.setUsers(users);
                updateAssignedLabel(users);
            }
        });

        repository.getAllDebtsLive().observe(this, debts -> {
            if (debts != null) {
                for (Debt d : debts) {
                    if (d.getId().equals(debtId)) {
                        currentDebt = d;
                        updateDebtInfo();
                        break;
                    }
                }
            }
        });

        repository.getPaymentsForDebtLive(debtId).observe(this, payments -> {
            if (payments != null) {
                paymentAdapter.setPayments(payments);
                tvInstallmentsCount.setText("HISTORIAL DE ABONOS (" + payments.size() + " " + (payments.size() == 1 ? "ABONO" : "ABONOS") + ")");
                tvNoPayments.setVisibility(payments.isEmpty() ? View.VISIBLE : View.GONE);
                updateDebtInfo();
            }
        });
    }

    private void updateDebtInfo() {
        if (currentDebt == null) return;

        tvTitle.setText(currentDebt.getName());
        tvDebtName.setText(currentDebt.getName());

        if (currentDebt.getDescription() != null && !currentDebt.getDescription().trim().isEmpty()) {
            tvDebtDesc.setText(currentDebt.getDescription());
            tvDebtDesc.setVisibility(View.VISIBLE);
        } else {
            tvDebtDesc.setVisibility(View.GONE);
        }

        double total = currentDebt.getAmount();
        List<Payment> payments = repository.getPaymentsForDebtLive(debtId).getValue();
        double paid = 0.0;
        if (payments != null) {
            for (Payment p : payments) {
                paid += p.getAmount();
            }
        }
        double remaining = Math.max(0.0, total - paid);
        boolean isPaid = remaining == 0.0 || "paid".equalsIgnoreCase(currentDebt.getStatus());

        tvTotalAmt.setText(FreyrRepository.formatCurrency(total));
        tvPaidAmt.setText(FreyrRepository.formatCurrency(paid));
        tvRemainingAmt.setText(FreyrRepository.formatCurrency(remaining));

        if (isPaid) {
            tvDebtStatus.setText("PAGADA EN SU TOTALIDAD");
            tvDebtStatus.setTextColor(ContextCompat.getColor(this, R.color.freyr_status_paid));
            tvDebtStatus.setBackgroundResource(R.drawable.bg_status_paid);
            btnAddPayment.setVisibility(View.GONE);
            pbDetailProgress.setProgress(100);
        } else {
            tvDebtStatus.setText("PENDIENTE");
            tvDebtStatus.setTextColor(ContextCompat.getColor(this, R.color.freyr_primary));
            tvDebtStatus.setBackgroundResource(R.drawable.bg_status_pending);
            btnAddPayment.setVisibility(View.VISIBLE);
            int percent = total > 0 ? (int) Math.min(100, (paid / total) * 100) : 0;
            pbDetailProgress.setProgress(percent);
        }
    }

    private void updateAssignedLabel(List<User> users) {
        if (currentDebt == null) return;
        if (currentDebt.getAssignedUserId() == null) {
            tvAssignedInfo.setText("Responsable: Toda la familia (Compartida)");
        } else {
            String name = "Miembro (" + currentDebt.getAssignedUserId() + ")";
            for (User u : users) {
                if (u.getId().equals(currentDebt.getAssignedUserId())) {
                    name = u.getFullName() + " (@" + u.getUsername() + ")";
                    break;
                }
            }
            tvAssignedInfo.setText("Responsable: " + name);
        }
    }
}

