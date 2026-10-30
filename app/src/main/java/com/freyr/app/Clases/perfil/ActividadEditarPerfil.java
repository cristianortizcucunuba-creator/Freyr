
package com.freyr.app.Clases.perfil;

// 1. IMPORTACIONES
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.freyr.app.FreyrApp;
import com.freyr.app.R;
import com.freyr.app.data.datos.Usuario;
import com.freyr.app.data.repositorio.FreyrRepositorio;


/**
 * Actividad encargada de modificar y actualizar el perfil del usuario
 * que tiene la sesión iniciada.
 *
 * Permite cambiar el nombre completo, nombre de usuario,
 * correo electrónico y contraseña.
 */


// 2. CLASE PRINCIPAL
public class ActividadEditarPerfil extends AppCompatActivity {


    // 3. ATRIBUTOS

    // Botón para regresar a la pantalla anterior.
    private ImageButton btnBack;

    // Texto para mostrar mensajes de información o error.
    private TextView tvFeedback;

    // Campos para modificar los datos del perfil.
    private EditText etFullName;
    private EditText etUsername;
    private EditText etEmail;
    private EditText etPassword;

    // Botón para guardar los cambios.
    private Button btnSaveProfile;

    // Repositorio para acceder y modificar los datos.
    private FreyrRepositorio repository;

    // Usuario que tiene la sesión iniciada.
    private Usuario currentUser;


    // 4. CICLO DE VIDA DE LA ACTIVIDAD
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Obtiene el repositorio desde la aplicación.
        repository = ((FreyrApp) getApplication()).getRepositorio();

        // Inicializa los componentes y configura sus acciones.
        initViews();
        setupListeners();

        // Carga la información del usuario actual.
        loadProfile();
    }


    // 5. INICIALIZACIÓN DE VISTAS
    private void initViews() {

        btnBack = findViewById(R.id.btn_atras);
        tvFeedback = findViewById(R.id.tv_feedback);

        etFullName = findViewById(R.id.et_nombre_completo);
        etUsername = findViewById(R.id.et_usuario);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_contraseña);

        btnSaveProfile = findViewById(R.id.btn_save_profile);
    }


    // 6. CONFIGURACIÓN DE EVENTOS
    private void setupListeners() {

        // Regresa a la pantalla anterior.
        btnBack.setOnClickListener(v -> finish());

        // Guarda los cambios del perfil.
        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }


    // 7. CARGAR INFORMACIÓN DEL PERFIL
    private void loadProfile() {

        // Observa los usuarios registrados en la aplicación.
        repository.observarTodosLosUsuarios().observe(this, users -> {

            if (users != null) {

                // Obtiene el identificador del usuario actual.
                String curId = repository.obtenerIdUsuarioActual();

                // Busca al usuario que tiene la sesión iniciada.
                for (Usuario u : users) {

                    if (u.getId().equals(curId)) {

                        currentUser = u;

                        // Carga los datos únicamente si el campo
                        // del nombre completo todavía está vacío.
                        if (etFullName.getText().toString().isEmpty()) {

                            etFullName.setText(u.getNomCompl());
                            etUsername.setText(u.getNomUsuario());
                            etEmail.setText(u.getEmail());
                        }

                        break;
                    }
                }
            }
        });
    }


    // 8. GUARDAR CAMBIOS DEL PERFIL
    private void saveProfile() {

        // Obtiene los datos introducidos en los campos.
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();


        // 8.1. VALIDAR NOMBRE COMPLETO
        if (fullName.isEmpty()) {
            showFeedback("El nombre completo es requerido.", false);
            etFullName.requestFocus();
            return;
        }


        // 8.2. VALIDAR NOMBRE DE USUARIO
        if (username.isEmpty()) {
            showFeedback("El nombre de usuario es requerido.", false);
            etUsername.requestFocus();
            return;
        }


        // 8.3. VALIDAR CORREO ELECTRÓNICO
        if (email.isEmpty()) {
            showFeedback("El correo electrónico es requerido.", false);
            etEmail.requestFocus();
            return;
        }


        // 8.4. VALIDAR CONTRASEÑA
        // Si se introduce una contraseña nueva, debe tener
        // al menos seis caracteres.
        if (!password.isEmpty() && password.length() < 6) {
            showFeedback(
                    "La nueva contraseña debe tener al menos 6 caracteres.",
                    false
            );

            etPassword.requestFocus();
            return;
        }


        // 8.5. ACTUALIZAR PERFIL
        // Desactiva el botón mientras se procesa la actualización.
        btnSaveProfile.setEnabled(false);

        repository.actualizarPerfil(
                fullName,
                username,
                email,
                password,
                new FreyrRepositorio.Callback<Void>() {

                    @Override
                    public void alTenerExito(Void result) {

                        runOnUiThread(() -> {

                            // Reactiva el botón.
                            btnSaveProfile.setEnabled(true);

                            // Informa que el perfil se actualizó correctamente.
                            Toast.makeText(
                                    ActividadEditarPerfil.this,
                                    "¡Perfil actualizado exitosamente!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            // Cierra la actividad.
                            finish();
                        });
                    }

                    @Override
                    public void alOcurrirError(String error) {

                        runOnUiThread(() -> {

                            // Reactiva el botón para permitir otro intento.
                            btnSaveProfile.setEnabled(true);

                            // Muestra el mensaje de error.
                            showFeedback(error, false);
                        });
                    }
                }
        );
    }


    // 9. MOSTRAR MENSAJES DE INFORMACIÓN O ERROR
    private void showFeedback(String msg, boolean isSuccess) {

        tvFeedback.setText(msg);

        // Cambia el color según el resultado de la operación.
        tvFeedback.setTextColor(
                ContextCompat.getColor(
                        this,
                        isSuccess
                                ? R.color.freyr_status_paid
                                : R.color.freyr_primary
                )
        );

        // Hace visible el mensaje.
        tvFeedback.setVisibility(View.VISIBLE);
    }
}
