
package com.freyr.app.Clases.deuda;

// ==========================================
// 1. IMPORTACIONES
// ==========================================

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.freyr.app.FreyrApp;
import com.freyr.app.R;
import com.freyr.app.data.datos.Deuda;
import com.freyr.app.data.datos.Pago;
import com.freyr.app.data.repositorio.FreyrRepositorio;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


// ==========================================
// 2. CLASE PRINCIPAL
// ==========================================

public class RegistrarAbonosDeuda extends AppCompatActivity {

    // ------------------------------------------
    // 2.1. Componentes de la interfaz
    // ------------------------------------------

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


    // ------------------------------------------
    // 2.2. Repositorio y datos de la deuda
    // ------------------------------------------

    private FreyrRepositorio repository;

    private String debtId;
    private Deuda currentDebt;

    // Saldo pendiente actual de la deuda.
    private double currentRemaining = 0.0;


    // ==========================================
    // 3. CICLO DE VIDA DE LA ACTIVIDAD
    // ==========================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_payment);

        // Obtener el repositorio y el identificador de la deuda.
        repository = ((FreyrApp) getApplication()).getRepositorio();
        debtId = getIntent().getStringExtra("debt_id");

        // Si no se recibió un identificador, cerrar la actividad.
        if (debtId == null) {
            finish();
            return;
        }

        // Inicializar y configurar los componentes.
        initViews();
        setupListeners();
        observeData();
    }


    // ==========================================
    // 4. INICIALIZACIÓN DE COMPONENTES
    // ==========================================

    private void initViews() {

        // ------------------------------------------
        // 4.1. Componentes informativos
        // ------------------------------------------

        btnBack = findViewById(R.id.btn_atras);

        tvForDebtName = findViewById(R.id.tv_for_debt_name);
        tvStatusTotal = findViewById(R.id.tv_status_total);
        tvStatusPaid = findViewById(R.id.tv_status_paid);
        tvStatusRemaining = findViewById(R.id.tv_status_remaining);

        tvError = findViewById(R.id.tx_error);
        btnPayFull = findViewById(R.id.btn_pay_full);
        tvProjectedRemaining = findViewById(R.id.tv_projected_remaining);


        // ------------------------------------------
        // 4.2. Campos para registrar el abono
        // ------------------------------------------

        etPaymentAmount = findViewById(R.id.et_payment_amount);
        etPaymentDate = findViewById(R.id.et_payment_date);
        etPaymentDesc = findViewById(R.id.et_payment_desc);

        btnConfirmPayment = findViewById(R.id.btn_confirm_payment);


        // ------------------------------------------
        // 4.3. Precargar la fecha actual
        // ------------------------------------------

        String today = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());

        etPaymentDate.setText(today);
    }


    // ==========================================
    // 5. CONFIGURACIÓN DE EVENTOS
    // ==========================================

    private void setupListeners() {

        // ------------------------------------------
        // 5.1. Regresar a la pantalla anterior
        // ------------------------------------------

        btnBack.setOnClickListener(v -> finish());


        // ------------------------------------------
        // 5.2. Completar el monto total pendiente
        // ------------------------------------------

        btnPayFull.setOnClickListener(v -> {

            if (currentRemaining > 0) {

                if (currentRemaining % 1.0 == 0.0) {

                    etPaymentAmount.setText(
                            String.valueOf((long) currentRemaining)
                    );

                } else {

                    etPaymentAmount.setText(
                            String.valueOf(currentRemaining)
                    );
                }
            }
        });


        // ------------------------------------------
        // 5.3. Detectar cambios en el monto ingresado
        // ------------------------------------------

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


        // ------------------------------------------
        // 5.4. Confirmar el registro del abono
        // ------------------------------------------

        btnConfirmPayment.setOnClickListener(v -> savePayment());
    }


    // ==========================================
    // 6. OBSERVACIÓN DE DATOS
    // ==========================================

    private void observeData() {

        // ------------------------------------------
        // 6.1. Observar las deudas
        // ------------------------------------------

        repository.observarTodasLasDeudas().observe(this, debts -> {

            if (debts != null) {

                for (Deuda debt : debts) {

                    if (debt.getId().equals(debtId)) {

                        currentDebt = debt;

                        tvForDebtName.setText(
                                "Para: " + debt.getNombre()
                        );

                        tvStatusTotal.setText(
                                FreyrRepositorio.formatearMoneda(debt.getMonto())
                        );

                        recalculateBalances();
                        break;
                    }
                }
            }
        });


        // ------------------------------------------
        // 6.2. Observar los abonos de la deuda
        // ------------------------------------------

        repository.observarPagosDeDeuda(debtId).observe(this, payments -> {

            if (payments != null) {
                recalculateBalances();
            }
        });
    }


    // ==========================================
    // 7. RECALCULAR LOS SALDOS DE LA DEUDA
    // ==========================================

    private void recalculateBalances() {

        if (currentDebt == null) {
            return;
        }

        // ------------------------------------------
        // 7.1. Obtener los pagos registrados
        // ------------------------------------------

        // El repositorio entrega una lista.
        // Se convierte a un arreglo para recorrer sus elementos.

        java.util.List<Pago> paymentList =
                repository.observarPagosDeDeuda(debtId).getValue();

        Pago[] payments = paymentList == null
                ? new Pago[0]
                : paymentList.toArray(new Pago[0]);


        // ------------------------------------------
        // 7.2. Calcular el total abonado
        // ------------------------------------------

        double paid = 0.0;

        for (Pago payment : payments) {
            paid += payment.getMonto();
        }


        // ------------------------------------------
        // 7.3. Calcular el saldo restante
        // ------------------------------------------

        currentRemaining = Math.max(
                0.0,
                currentDebt.getMonto() - paid
        );


        // ------------------------------------------
        // 7.4. Actualizar los valores en pantalla
        // ------------------------------------------

        tvStatusPaid.setText(
                FreyrRepositorio.formatearMoneda(paid)
        );

        tvStatusRemaining.setText(
                FreyrRepositorio.formatearMoneda(currentRemaining)
        );

        updateProjectedRemaining();
    }


    // ==========================================
    // 8. ACTUALIZAR EL SALDO PROYECTADO
    // ==========================================

    private void updateProjectedRemaining() {

        // Obtener el monto que el usuario está escribiendo.
        String amountText = etPaymentAmount.getText()
                .toString()
                .trim();

        // Si el campo está vacío, limpiar el mensaje.
        if (amountText.isEmpty()) {
            tvProjectedRemaining.setText("");
            return;
        }

        try {

            // Convertir el texto a un valor numérico.
            double entered = Double.parseDouble(amountText);

            // Calcular cuánto quedaría pendiente después del abono.
            double projected = Math.max(
                    0.0,
                    currentRemaining - entered
            );

            String message = "Saldo pendiente proyectado: "
                    + FreyrRepositorio.formatearMoneda(projected);

            // Avisar si el abono cubriría toda la deuda.
            if (entered >= currentRemaining && currentRemaining > 0) {
                message += " (Se marcará como PAGADA)";
            }

            tvProjectedRemaining.setText(message);

        } catch (NumberFormatException e) {

            // Si el valor no es numérico, limpiar el mensaje.
            tvProjectedRemaining.setText("");
        }
    }


    // ==========================================
    // 9. GUARDAR UN NUEVO ABONO
    // ==========================================

    private void savePayment() {

        // Ocultar errores anteriores.
        tvError.setVisibility(View.GONE);


        // ------------------------------------------
        // 9.1. Validar el monto ingresado
        // ------------------------------------------

        String amountText = etPaymentAmount.getText()
                .toString()
                .trim();

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


        // ------------------------------------------
        // 9.2. Obtener fecha y descripción
        // ------------------------------------------

        String date = etPaymentDate.getText()
                .toString()
                .trim();

        // Si no se ingresó una fecha, utilizar la actual.
        if (date.isEmpty()) {

            date = new SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
            ).format(new Date());
        }

        String description = etPaymentDesc.getText()
                .toString()
                .trim();


        // ------------------------------------------
        // 9.3. Deshabilitar el botón de confirmación
        // ------------------------------------------

        btnConfirmPayment.setEnabled(false);


        // ------------------------------------------
        // 9.4. Registrar el abono en el repositorio
        // ------------------------------------------

        repository.registrarPago(
                debtId,
                amount,
                date,
                description,

                new FreyrRepositorio.Callback<Pago>() {

                    @Override
                    public void alTenerExito(Pago result) {

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
                    public void alOcurrirError(String error) {

                        runOnUiThread(() -> {

                            // Volver a habilitar el botón si ocurre un error.
                            btnConfirmPayment.setEnabled(true);

                            showError(error);
                        });
                    }
                }
        );
    }


    // ==========================================
    // 10. MOSTRAR MENSAJES DE ERROR
    // ==========================================

    private void showError(String message) {

        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
