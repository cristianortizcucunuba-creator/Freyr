package com.freyr.app.Clases.autenticacion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.freyr.app.Clases.ActividadPrincipal;
import com.freyr.app.FreyrApplication;
import com.freyr.app.R;
import com.freyr.app.Clases.ActividadPrincipal;
import com.freyr.app.data.model.Usuario;
import com.freyr.app.data.repository.FreyrRepository;

/**
 * Actividad para el registro de nuevas cuentas de usuario en Freyr.
 * Valida nombre completo, usuario único, correo y confirmación de contraseña
 * antes de registrar e iniciar la sesión automáticamente.
 */
public class RegistrarUsuario extends AppCompatActivity {

    private EditText etFullName;
    private EditText etUsername;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnRegister;
    private TextView tvGoToLogin;
    private TextView tvError;

    private FreyrRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        repository = ((FreyrApplication) getApplication()).getRepository();

        initViews();
        setupListeners();
    }

    private void initViews() {
        etFullName = findViewById(R.id.et_full_name);
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvGoToLogin = findViewById(R.id.tv_go_to_login);
        tvError = findViewById(R.id.tx_error);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> doRegister());
        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void doRegister() {
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

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

        tvError.setVisibility(View.GONE);
        btnRegister.setEnabled(false);

        repository.register(fullName, username, email, password, new FreyrRepository.Callback<Usuario>() {
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
                    btnRegister.setEnabled(true);
                    showError(error);
                });
            }
        });
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
