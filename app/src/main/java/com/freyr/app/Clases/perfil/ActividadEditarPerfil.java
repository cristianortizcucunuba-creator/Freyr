package com.freyr.app.Clases.perfil;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.freyr.app.FreyrApplication;
import com.freyr.app.R;
import com.freyr.app.data.model.Usuario;
import com.freyr.app.data.repository.FreyrRepositorio;

/**
 * Actividad para la modificación y actualización del perfil del usuario en sesión.
 * Permite cambiar nombre completo, nombre de usuario, correo electrónico y contraseña.
 */
public class ActividadEditarPerfil extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvFeedback;
    private EditText etFullName;
    private EditText etUsername;
    private EditText etEmail;
    private EditText etPassword;
    private Button btnSaveProfile;

    private FreyrRepositorio repository;
    private Usuario currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        repository = ((FreyrApplication) getApplication()).getRepository();

        initViews();
        setupListeners();
        loadProfile();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_volver);
        tvFeedback = findViewById(R.id.tv_feedback);
        etFullName = findViewById(R.id.et_nombre_completo);
        etUsername = findViewById(R.id.et_usuario);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_contraseña);
        btnSaveProfile = findViewById(R.id.btn_save_profile);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void loadProfile() {
        repository.getAllUsersLive().observe(this, users -> {
            if (users != null) {
                String curId = repository.getCurrentUserId();
                for (Usuario u : users) {
                    if (u.getId().equals(curId)) {
                        currentUser = u;
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

    private void saveProfile() {
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (fullName.isEmpty()) {
            showFeedback("El nombre completo es requerido.", false);
            etFullName.requestFocus();
            return;
        }

        if (username.isEmpty()) {
            showFeedback("El nombre de usuario es requerido.", false);
            etUsername.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            showFeedback("El correo electrónico es requerido.", false);
            etEmail.requestFocus();
            return;
        }

        if (!password.isEmpty() && password.length() < 6) {
            showFeedback("La nueva contraseña debe tener al menos 6 caracteres.", false);
            etPassword.requestFocus();
            return;
        }

        btnSaveProfile.setEnabled(false);

        repository.updateProfile(fullName, username, email, password, new FreyrRepositorio.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    btnSaveProfile.setEnabled(true);
                    Toast.makeText(ActividadEditarPerfil.this, "¡Perfil actualizado exitosamente!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnSaveProfile.setEnabled(true);
                    showFeedback(error, false);
                });
            }
        });
    }

    private void showFeedback(String msg, boolean isSuccess) {
        tvFeedback.setText(msg);
        tvFeedback.setTextColor(ContextCompat.getColor(this, isSuccess ? R.color.freyr_status_paid : R.color.freyr_primary));
        tvFeedback.setVisibility(View.VISIBLE);
    }
}

