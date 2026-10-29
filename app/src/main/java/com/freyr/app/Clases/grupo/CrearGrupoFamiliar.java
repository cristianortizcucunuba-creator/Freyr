package com.freyr.app.Clases.grupo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.freyr.app.FreyrApp;
import com.freyr.app.R;
import com.freyr.app.data.model.Grupofamiliar;
import com.freyr.app.data.repositorio.FreyrRepositorio;

/**
 * Actividad para la fundación de un nuevo grupo familiar.
 * Permite registrar el nombre del grupo y asigna automáticamente al usuario
 * creador como su primer miembro y administrador.
 */
public class CrearGrupoFamiliar extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvError;
    private EditText etGroupName;
    private Button btnSaveGroup;
    private FreyrRepositorio repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_family_group);

        repository = ((FreyrApp) getApplication()).getRepository();

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_volver);
        tvError = findViewById(R.id.tx_error);
        etGroupName = findViewById(R.id.et_group_name);
        btnSaveGroup = findViewById(R.id.btn_save_group);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSaveGroup.setOnClickListener(v -> {
            String name = etGroupName.getText().toString().trim();
            if (name.isEmpty()) {
                tvError.setText("Por favor ingresa un nombre para el grupo familiar.");
                tvError.setVisibility(View.VISIBLE);
                etGroupName.requestFocus();
                return;
            }

            tvError.setVisibility(View.GONE);
            btnSaveGroup.setEnabled(false);

            repository.createFamilyGroup(name, new FreyrRepositorio.Callback<Grupofamiliar>() {
                @Override
                public void onSuccess(Grupofamiliar result) {
                    runOnUiThread(() -> {
                        Toast.makeText(CrearGrupoFamiliar.this, "¡Grupo familiar creado exitosamente!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        btnSaveGroup.setEnabled(true);
                        tvError.setText(error);
                        tvError.setVisibility(View.VISIBLE);
                    });
                }
            });
        });
    }
}

