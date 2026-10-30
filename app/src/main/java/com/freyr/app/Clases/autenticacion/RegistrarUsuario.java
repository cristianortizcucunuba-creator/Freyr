
package com.freyr.app.Clases.autenticacion;

// -----------------------------------------
// 1. Importaciones de Android
// -----------------------------------------

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

// -----------------------------------------
// 2. Importaciones de AndroidX
// -----------------------------------------

import androidx.appcompat.app.AppCompatActivity;

// -----------------------------------------
// 3. Importaciones del proyecto
// -----------------------------------------

import com.freyr.app.Clases.ActividadPrincipal;
import com.freyr.app.FreyrApp;
import com.freyr.app.R;
import com.freyr.app.data.datos.Usuario;
import com.freyr.app.data.repositorio.FreyrRepositorio;


// Registro de nuevas cuentas de usuario en Freyr.
// Valida nombre completo, usuario único, correo y confirmación de contraseña.

public class RegistrarUsuario extends AppCompatActivity {

    // -----------------------------------------
    // 4. Declaración de atributos
    // -----------------------------------------

    private EditText edtexNombre;
    private EditText edtexUsuario;
    private EditText edtextEmail;
    private EditText edtexContraseña;
    private EditText edtxtConfirmarContraseña;

    private Button btnRegistrar;

    private TextView txGoToLogin;
    private TextView txError;

    private FreyrRepositorio repositorio;


    // -----------------------------------------
    // 5. Ciclo de vida de la actividad
    // -----------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        repositorio = ((FreyrApp) getApplication()).getRepositorio();

        iniciarVistas();
        setupListeners();
    }


    // -----------------------------------------
    // 6. Inicialización de componentes
    // -----------------------------------------

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


    // -----------------------------------------
    // 7. Configuración de eventos
    // -----------------------------------------

    private void setupListeners() {

        btnRegistrar.setOnClickListener(v -> registrar());

        txGoToLogin.setOnClickListener(v -> finish());
    }


    // -----------------------------------------
    // 8. Lógica de registro de usuario
    // -----------------------------------------

    private void registrar() {

        String nombreCompleto = edtexNombre.getText().toString().trim();
        String usuario = edtexUsuario.getText().toString().trim();
        String email = edtextEmail.getText().toString().trim();
        String contraseña = edtexContraseña.getText().toString();
        String confirmarContraseña = edtxtConfirmarContraseña.getText().toString();


        // Validación de campos obligatorios

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


        // Oculta errores y desactiva el botón durante el registro

        txError.setVisibility(View.GONE);
        btnRegistrar.setEnabled(false);


        // Envía los datos al repositorio para registrar al usuario

        repositorio.registrarUsuario(
                nombreCompleto,
                usuario,
                email,
                contraseña,
                new FreyrRepositorio.Callback<Usuario>() {

                    @Override
                    public void alTenerExito(Usuario result) {

                        runOnUiThread(() -> {

                            Intent intent = new Intent(
                                    RegistrarUsuario.this,
                                    ActividadPrincipal.class
                            );

                            intent.setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                            );

                            startActivity(intent);
                            finish();
                        });
                    }

                    @Override
                    public void alOcurrirError(String error) {

                        runOnUiThread(() -> {

                            btnRegistrar.setEnabled(true);
                            showError(error);
                        });
                    }
                }
        );
    }


    // -----------------------------------------
    // 9. Método para mostrar errores
    // -----------------------------------------

    private void showError(String message) {

        txError.setText(message);
        txError.setVisibility(View.VISIBLE);
    }
}
