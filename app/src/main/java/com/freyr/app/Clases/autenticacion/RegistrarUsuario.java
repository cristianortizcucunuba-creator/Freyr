package com.freyr.app.Clases.autenticacion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.freyr.app.Clases.ActividadPrincipal;
import com.freyr.app.FreyrApp;
import com.freyr.app.R;
import com.freyr.app.data.model.Usuario;
import com.freyr.app.data.repository.FreyrRepositorio;

// registro de nuevas cuentas de usuario en Freyr.
// Valida nombre completo, usuario único, correo y confirmación de contraseña

public class RegistrarUsuario extends AppCompatActivity {

    private EditText etNombre;
    private EditText etNombreUsuario;
    private EditText etEmail;
    private EditText etContraseña;
    private EditText etConfirmarContraseña;
    private Button btnRegistrar;
    private TextView txGoToLogin;
    private TextView txError;

    private FreyrRepositorio repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        repository = ((FreyrApp) getApplication()).getRepositorio();

        initViews();
        setupListeners();
    }

    private void initViews() {
        etNombre = findViewById(R.id.et_full_name);
        etNombreUsuario = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etContraseña = findViewById(R.id.et_contraseña);
        etConfirmarContraseña = findViewById(R.id.et_confirm_password);
        btnRegistrar = findViewById(R.id.btn_register);
        txGoToLogin = findViewById(R.id.tx_go_to_login);
        txError = findViewById(R.id.tx_error);
    }

    private void setupListeners() {
        btnRegistrar.setOnClickListener(v -> doRegister());
        txGoToLogin.setOnClickListener(v -> finish());
    }

    private void doRegister() {
        String fullName = etNombre.getText().toString().trim();
        String username = etNombreUsuario.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etContraseña.getText().toString();
        String confirmPassword = etConfirmarContraseña.getText().toString();

        if (fullName.isEmpty()) {
            showError("El nombre completo es requerido.");
            return;
        }
        if (username.isEmpty()) {
            showError("El nombre de usuario es requerido.");
            return;
        }
        if (email.isEmpty()) {
            showError("El correo electrónico es requerido.");
            return;
        }
        if (password.length() < 6) {
            showError("La contraseña debe tener al menos 6 caracteres.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("Las contraseñas no coinciden.");
            return;
        }

        txError.setVisibility(View.GONE);
        btnRegistrar.setEnabled(false);

        repository.register(fullName, username, email, password, new FreyrRepositorio.Callback<Usuario>() {
            @Override
            public void onSuccess(Usuario result) {
                runOnUiThread(() -> {
                    Intent intent = new Intent(RegistrarUsuario.this, ActividadPrincipal.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnRegistrar.setEnabled(true);
                    showError(error);
                });
            }
        });
    }

    private void showError(String message) {
        txError.setText(message);
        txError.setVisibility(View.VISIBLE);
    }
}
