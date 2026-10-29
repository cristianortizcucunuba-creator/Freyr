package com.freyr.app.Clases.deuda;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.freyr.app.FreyrApplication;
import com.freyr.app.R;
import com.freyr.app.data.model.Deuda;
import com.freyr.app.data.model.Pago;
import com.freyr.app.data.repository.FreyrRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegistrarAbonosDeuda extends AppCompatActivity {

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
    private Deuda currentDebt;
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
        tvError = findViewById(R.id.tx_error);
        btnPayFull = findViewById(R.id.btn_pay_full);
        tvProjectedRemaining = findViewById(R.id.tv_projected_remaining);

        etPaymentAmount = findViewById(R.id.et_payment_amount);
        etPaymentDate = findViewById(R.id.et_payment_date);
        etPaymentDesc = findViewById(R.id.et_payment_desc);
        btnConfirmPayment = findViewById(R.id.btn_confirm_payment);

        // Precarga la fecha actual.
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
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
            public void beforeTextChanged(
                    CharSequence text,
                    int start,
                    int count,
                    int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence text,
                    int start,
                    int before,
                    int count
            ) {
                updateProjectedRemaining();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        btnConfirmPayment.setOnClickListener(v -> savePayment());
    }

    private void observeData() {
        repository.getAllDebtsLive().observe(this, debts -> {
            if (debts != null) {
                for (Deuda debt : debts) {
                    if (debt.getId().equals(debtId)) {
                        currentDebt = debt;
                        tvForDebtName.setText("Para: " + debt.getName());
                        tvStatusTotal.setText(
                                FreyrRepository.formatCurrency(debt.getAmount())
                        );
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
        if (currentDebt == null) {
            return;
        }

        // El repositorio entrega una lista; se convierte a arreglo para recorrerla.
        java.util.List<Pago> paymentList =
                repository.getPaymentsForDebtLive(debtId).getValue();

        Pago[] payments = paymentList == null
                ? new Pago[0]
                : paymentList.toArray(new Pago[0]);

        double paid = 0.0;
        for (Pago payment : payments) {
            paid += payment.getAmount();
        }

        currentRemaining = Math.max(0.0, currentDebt.getAmount() - paid);

        tvStatusPaid.setText(FreyrRepository.formatCurrency(paid));
        tvStatusRemaining.setText(
                FreyrRepository.formatCurrency(currentRemaining)
        );
        updateProjectedRemaining();
    }

    private void updateProjectedRemaining() {
        String amountText = etPaymentAmount.getText().toString().trim();

        if (amountText.isEmpty()) {
            tvProjectedRemaining.setText("");
            return;
        }

        try {
            double entered = Double.parseDouble(amountText);
            double projected = Math.max(0.0, currentRemaining - entered);

            String message = "Saldo pendiente proyectado: "
                    + FreyrRepository.formatCurrency(projected);

            if (entered >= currentRemaining && currentRemaining > 0) {
                message += " (Se marcará como PAGADA)";
            }

            tvProjectedRemaining.setText(message);
        } catch (NumberFormatException e) {
            tvProjectedRemaining.setText("");
        }
    }

    private void savePayment() {
        tvError.setVisibility(View.GONE);

        String amountText = etPaymentAmount.getText().toString().trim();
        if (amountText.isEmpty()) {
            showError("Por favor ingresa un monto.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            showError("Monto numérico no válido.");
            return;
        }

        if (amount <= 0.0) {
            showError("El monto del abono debe ser mayor a 0.");
            return;
        }

        String date = etPaymentDate.getText().toString().trim();
        if (date.isEmpty()) {
            date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(new Date());
        }

        String description = etPaymentDesc.getText().toString().trim();

        btnConfirmPayment.setEnabled(false);

        repository.addPayment(
                debtId,
                amount,
                date,
                description,
                new FreyrRepository.Callback<Pago>() {
                    @Override
                    public void onSuccess(Pago result) {
                        runOnUiThread(() -> {
                            Toast.makeText(
                                    RegistrarAbonosDeuda.this,
                                    "¡Abono registrado exitosamente!",
                                    Toast.LENGTH_SHORT
                            ).show();
                            finish();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            btnConfirmPayment.setEnabled(true);
                            showError(error);
                        });
                    }
                }
        );
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}