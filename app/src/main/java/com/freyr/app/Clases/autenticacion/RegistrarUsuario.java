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
import com.freyr.app.data.model.Usuario;
import com.freyr.app.data.repository.FreyrRepositorio;

// registro de nuevas cuentas de usuario en Freyr..
// Valida nombre completo, usuario único, correo y confirmación de contraseña

public class RegistrarUsuario extends AppCompatActivity {

    private EditText edtexNombre;
    private EditText edtexUsuario;
    private EditText edtextEmail;
    private EditText edtexContraseña;
    private EditText edtxtConfirmarContraseña;
    private Button btnRegistrar;
    private TextView txGoToLogin;
    private TextView txError;

    private FreyrRepositorio repositorio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        repositorio = ((FreyrApplication) getApplication()).getRepository();

        iniciarVistas();
        setupListeners();
    }

    private void iniciarVistas() {
        edtexNombre = findViewById(R.id.et_nombre_completo);
        edtexUsuario = findViewById(R.id.et_usuario);
        edtextEmail = findViewById(R.id.et_email);
        edtexContraseña = findViewById(R.id.et_contraseña);
        edtxtConfirmarContraseña = findViewById(R.id.et_confirmar_contraseña);
        btnRegistrar = findViewById(R.id.btn_registrar);
        txGoToLogin = findViewById(R.id.tx_go_to_login);
        txError = findViewById(R.id.tx_error);
    }

    private void setupListeners() {
        btnRegistrar.setOnClickListener(v -> registrar());
        txGoToLogin.setOnClickListener(v -> finish());
    }

    private void registrar() {
        String nombreCompleto = edtexNombre.getText().toString().trim();
        String usuario = edtexUsuario.getText().toString().trim();
        String email = edtextEmail.getText().toString().trim();
        String contraseña = edtexContraseña.getText().toString();
        String confirmarContraseña = edtxtConfirmarContraseña.getText().toString();

        if (nombreCompleto.isEmpty()) {
            showError("El nombre completo es requerido.");
            return;
        }
        if (usuario.isEmpty()) {
            showError("El nombre de usuario es requerido.");
            return;
        }
        if (email.isEmpty()) {
            showError("El correo electrónico es requerido.");
            return;
        }
        if (contraseña.length() < 6) {
            showError("La contraseña debe tener al menos 6 caracteres.");
            return;
        }
        if (!contraseña.equals(confirmarContraseña)) {
            showError("Las contraseñas no coinciden.");
            return;
        }

        txError.setVisibility(View.GONE);
        btnRegistrar.setEnabled(false);

        repositorio.register(nombreCompleto, usuario, email, contraseña, new FreyrRepositorio.Callback<Usuario>() {
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
