package com.freyr.app.Clases.autenticacion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.freyr.app.FreyrApplication;
import com.freyr.app.R;
import com.freyr.app.data.model.Usuario;
import com.freyr.app.data.repository.FreyrRepository;
import com.freyr.app.Clases.ActividadPrincipal;

/**
 * Actividad de inicio de sesión de usuario en Freyr.
 * Valida credenciales locales de acceso y ofrece botones de acceso rápido
 * demostrativo para pruebas inmediatas con los perfiles sembrados.
 */
public class LoguearUsuario extends AppCompatActivity {

    private EditText etIdentifier;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvGoToRegister;
    private TextView tvError;

    private Button btnDemoJuan;
    private Button btnDemoMaria;
    private Button btnDemoCarlos;

    private FreyrRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        repository = ((FreyrApplication) getApplication()).getRepository();

        initViews();
        setupListeners();
    }

    private void initViews() {
        etIdentifier = findViewById(R.id.et_identifier);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvGoToRegister = findViewById(R.id.tv_go_to_register);
        tvError = findViewById(R.id.tv_error);

        btnDemoJuan = findViewById(R.id.btn_demo_juan);
        btnDemoMaria = findViewById(R.id.btn_demo_maria);
        btnDemoCarlos = findViewById(R.id.btn_demo_carlos);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> doLogin());

        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoguearUsuario.this, RegistrarUsuario.class);
            startActivity(intent);
        });

        btnDemoJuan.setOnClickListener(v -> {
            etIdentifier.setText("juan");
            etPassword.setText("password123");
            doLogin();
        });

        btnDemoMaria.setOnClickListener(v -> {
            etIdentifier.setText("maria");
            etPassword.setText("password123");
            doLogin();
        });

        btnDemoCarlos.setOnClickListener(v -> {
            etIdentifier.setText("carlos");
            etPassword.setText("password123");
            doLogin();
        });
    }

    private void doLogin() {
        String identifier = etIdentifier.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (identifier.isEmpty()) {
            tvError.setText("Por favor ingresa tu nombre de usuario o correo.");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        tvError.setVisibility(View.GONE);
        btnLogin.setEnabled(false);

        repository.login(identifier, password, new FreyrRepository.Callback<Usuario>() {
            @Override
            public void onSuccess(Usuario result) {
                runOnUiThread(() -> {
                    Intent intent = new Intent(LoguearUsuario.this, ActividadPrincipal.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    tvError.setText(error);
                    tvError.setVisibility(View.VISIBLE);
                });
            }
        });
    }
}

