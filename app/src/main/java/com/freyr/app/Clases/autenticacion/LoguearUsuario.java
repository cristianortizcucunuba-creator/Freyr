
package com.freyr.app.Clases.autenticacion;

// 1. Importaciones de Android
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

// 2. Importaciones de AndroidX
import androidx.appcompat.app.AppCompatActivity;

// 3. Importaciones del proyecto
import com.freyr.app.Clases.ActividadPrincipal;
import com.freyr.app.Clases.autenticacion.RegistrarUsuario;
import com.freyr.app.FreyrApp;
import com.freyr.app.R;
import com.freyr.app.data.datos.Usuario;
import com.freyr.app.data.repositorio.FreyrRepositorio;

// Inicio de sesión de usuario en Freyr.
public class LoguearUsuario extends AppCompatActivity {

    // -----------------------------------------
    // 4. Declaración de atributos
    // -----------------------------------------

    private EditText edtextNombre;
    private EditText edtextContraseña;

    private Button btnLogin;

    private TextView txGoToRegister;
    private TextView txError;

    private FreyrRepositorio repositorio;


    // -----------------------------------------
    // 5. Ciclo de vida de la actividad
    // -----------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        repositorio = ((FreyrApp) getApplication()).getRepositorio();

        iniciarVistas();
        setupListeners();
    }


    // -----------------------------------------
    // 6. Inicialización de componentes
    // -----------------------------------------

    private void iniciarVistas() {
        edtextNombre = findViewById(R.id.et_nombre);
        edtextContraseña = findViewById(R.id.et_contraseña);

        btnLogin = findViewById(R.id.btn_login);

        txGoToRegister = findViewById(R.id.tx_go_to_register);
        txError = findViewById(R.id.tx_error);
    }


    // -----------------------------------------
    // 7. Configuración de eventos
    // -----------------------------------------

    private void setupListeners() {

        btnLogin.setOnClickListener(v -> iniciarSesion());

        txGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LoguearUsuario.this,
                    RegistrarUsuario.class
            );

            startActivity(intent);
        });
    }


    // -----------------------------------------
    // 8. Lógica de inicio de sesión
    // -----------------------------------------

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

        repositorio.iniciarSesion(nombre, password, new FreyrRepositorio.Callback<Usuario>() {

            @Override
            public void alTenerExito(Usuario result) {

                runOnUiThread(() -> {

                    Intent intent = new Intent(
                            LoguearUsuario.this,
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

                    btnLogin.setEnabled(true);

                    txError.setText(error);
                    txError.setVisibility(View.VISIBLE);
                });
            }
        });
    }
}
