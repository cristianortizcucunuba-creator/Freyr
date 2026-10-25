package com.freyr.app.ui.debt;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.freyr.app.FreyrApplication;
import com.freyr.app.R;
import com.freyr.app.data.model.Debt;
import com.freyr.app.data.model.Payment;
import com.freyr.app.data.repository.FreyrRepository;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Actividad para registrar abonos y pagos parciales o totales a una deuda.
 * Calcula en tiempo real el saldo restante proyectado y actualiza automáticamente
 * el estado a pagada cuando el saldo llega a cero.
 */
public class AddPaymentActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvForDebtName;
    private TextView tvStatusTotal;
    private TextView tvStatusPaid;
    private TextView tvStatusRemaining;
    private TextView tvError;
    private TextView btnPayFull;
    private TextView tvProjectedRemaining;

    private EditText etPaymentAmount;
    private EditText etPaymentDate;
    private EditText etPaymentDesc;
    private Button btnConfirmPayment;

    private FreyrRepository repository;
    private String debtId;
    private Debt currentDebt;
    private double currentRemaining = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_payment);

        repository = ((FreyrApplication) getApplication()).getRepository();
        debtId = getIntent().getStringExtra("debt_id");

        if (debtId == null) {
            finish();
            return;
        }

        initViews();
        setupListeners();
        observeData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvForDebtName = findViewById(R.id.tv_for_debt_name);
        tvStatusTotal = findViewById(R.id.tv_status_total);
        tvStatusPaid = findViewById(R.id.tv_status_paid);
        tvStatusRemaining = findViewById(R.id.tv_status_remaining);
        tvError = findViewById(R.id.tv_error);
        btnPayFull = findViewById(R.id.btn_pay_full);
        tvProjectedRemaining = findViewById(R.id.tv_projected_remaining);

        etPaymentAmount = findViewById(R.id.et_payment_amount);
        etPaymentDate = findViewById(R.id.et_payment_date);
        etPaymentDesc = findViewById(R.id.et_payment_desc);
        btnConfirmPayment = findViewById(R.id.btn_confirm_payment);

        // Precargar la fecha actual (AAAA-MM-DD)
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        etPaymentDate.setText(today);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnPayFull.setOnClickListener(v -> {
            if (currentRemaining > 0) {
                if (currentRemaining % 1.0 == 0.0) {
                    etPaymentAmount.setText(String.valueOf((long) currentRemaining));
                } else {
                    etPaymentAmount.setText(String.valueOf(currentRemaining));
                }
            }
        });

        etPaymentAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateProjectedRemaining();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnConfirmPayment.setOnClickListener(v -> savePayment());
    }

    private void observeData() {
        repository.getAllDebtsLive().observe(this, debts -> {
            if (debts != null) {
                for (Debt d : debts) {
                    if (d.getId().equals(debtId)) {
                        currentDebt = d;
                        tvForDebtName.setText("Para: " + d.getName());
                        tvStatusTotal.setText(FreyrRepository.formatCurrency(d.getAmount()));
                        recalculateBalances();
                        break;
                    }
                }
            }
        });

        repository.getPaymentsForDebtLive(debtId).observe(this, payments -> {
            if (payments != null) {
                recalculateBalances();
            }
        });
    }

    private void recalculateBalances() {
        if (currentDebt == null) return;
        List<Payment> payments = repository.getPaymentsForDebtLive(debtId).getValue();
        double paid = 0.0;
        if (payments != null) {
            for (Payment p : payments) {
                paid += p.getAmount();
            }
        }
        currentRemaining = Math.max(0.0, currentDebt.getAmount() - paid);

        tvStatusPaid.setText(FreyrRepository.formatCurrency(paid));
        tvStatusRemaining.setText(FreyrRepository.formatCurrency(currentRemaining));
        updateProjectedRemaining();
    }

    private void updateProjectedRemaining() {
        String amtStr = etPaymentAmount.getText().toString().trim();
        if (amtStr.isEmpty()) {
            tvProjectedRemaining.setText("");
            return;
        }

        try {
            double entered = Double.parseDouble(amtStr);
            double projected = Math.max(0.0, currentRemaining - entered);
            String text = "Saldo pendiente proyectado: " + FreyrRepository.formatCurrency(projected);
            if (entered >= currentRemaining && currentRemaining > 0) {
                text += " (Se marcará como PAGADA)";
            }
            tvProjectedRemaining.setText(text);
        } catch (NumberFormatException e) {
            tvProjectedRemaining.setText("");
        }
    }

    private void savePayment() {
        tvError.setVisibility(View.GONE);

        String amtStr = etPaymentAmount.getText().toString().trim();
        if (amtStr.isEmpty()) {
            tvError.setText("Por favor ingresa un monto.");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amtStr);
        } catch (NumberFormatException e) {
            tvError.setText("Monto numérico no válido.");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        if (amount <= 0.0) {
            tvError.setText("El monto del abono debe ser mayor a 0.");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        String date = etPaymentDate.getText().toString().trim();
        if (date.isEmpty()) {
            date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        }

        String description = etPaymentDesc.getText().toString().trim();

        btnConfirmPayment.setEnabled(false);

        repository.addPayment(debtId, amount, date, description, new FreyrRepository.Callback<Payment>() {
            @Override
            public void onSuccess(Payment result) {
                runOnUiThread(() -> {
                    Toast.makeText(AddPaymentActivity.this, "¡Abono registrado exitosamente!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnConfirmPayment.setEnabled(true);
                    tvError.setText(error);
                    tvError.setVisibility(View.VISIBLE);
                });
            }
        });
    }
}

