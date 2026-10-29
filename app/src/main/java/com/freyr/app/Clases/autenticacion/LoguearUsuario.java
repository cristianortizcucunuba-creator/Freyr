package com.freyr.app.Clases.autenticacion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.freyr.app.FreyrApp;
import com.freyr.app.R;
import com.freyr.app.data.model.Usuario;
import com.freyr.app.data.repositorio.FreyrRepositorio;
import com.freyr.app.Clases.ActividadPrincipal;

 // Inicio de sesión de usuario en Freyr.
 // Demostracion para pruebas con los perfiles puestos.

public class LoguearUsuario extends AppCompatActivity {

    private EditText edtextNombre;
    private EditText edtextContraseña;
    private Button btnLogin;
    private TextView txGoToRegister;
    private TextView txError;

    private Button btnJuan;
    private Button btnMaria;
    private Button btnCarlos;

    private FreyrRepositorio repositorio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        repositorio = ((FreyrApp) getApplication()).getRepository();

        iniciarVistas();
        setupListeners();
    }

    private void iniciarVistas() {
        edtextNombre = findViewById(R.id.et_nombre);
        edtextContraseña = findViewById(R.id.et_contraseña);
        btnLogin = findViewById(R.id.btn_login);
        txGoToRegister = findViewById(R.id.tx_go_to_register);
        txError = findViewById(R.id.tx_error);

        btnJuan = findViewById(R.id.btn_juan);
        btnMaria = findViewById(R.id.btn_maria);
        btnCarlos = findViewById(R.id.btn_carlos);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> iniciarSesion());

        txGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoguearUsuario.this, RegistrarUsuario.class);
            startActivity(intent);
        });

        btnJuan.setOnClickListener(v -> {
            edtextNombre.setText("juan");
            edtextContraseña.setText("contraseña123");
            iniciarSesion();
        });

        btnMaria.setOnClickListener(v -> {
            edtextNombre.setText("maria");
            edtextContraseña.setText("contraseña123");
            iniciarSesion();
        });

        btnCarlos.setOnClickListener(v -> {
            edtextNombre.setText("carlos");
            edtextContraseña.setText("password123");
            iniciarSesion();
        });
    }

    private void iniciarSesion() {
        String nombre = edtextNombre.getText().toString().trim();
        String password = edtextContraseña.getText().toString();

        if (nombre.isEmpty()) {
            txError.setText("Por favor ingresa tu nombre de usuario o correo.");
            txError.setVisibility(View.VISIBLE);
            return;
        }

        txError.setVisibility(View.GONE);
        btnLogin.setEnabled(false);

        repositorio.login(nombre, password, new FreyrRepositorio.Callback<Usuario>() {
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
                    txError.setText(error);
                    txError.setVisibility(View.VISIBLE);
                });
            }
        });
    }
}

