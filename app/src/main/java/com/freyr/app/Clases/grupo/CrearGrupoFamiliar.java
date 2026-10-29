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
import com.freyr.app.data.datos.Grupofamiliar;
import com.freyr.app.data.repositorio.FreyrRepositorio;

/**
 * Actividad para la fundación de un nuevo grupo familiar.
 * Permite registrar el nombre del grupo y asigna automáticamente al usuario
 * creador como su primer miembro y administrador.
 */
public class CrearGrupoFamiliar extends AppCompatActivity {

    private ImageButton btnVolv;
    private TextView txError;
    private EditText etNombreGrupo;
    private Button btnGuardarGrupo;
    private FreyrRepositorio repositorio;

    @Override
    protected void onCreate(Bundle estadoGuardado) {
        super.onCreate(estadoGuardado);
        setContentView(R.layout.activity_create_family_group);

        repositorio = ((FreyrApp) getApplication()).getRepositorio();

        inicializarVistas();
        configurarAcciones();
    }

    private void inicializarVistas() {
        btnVolv = findViewById(R.id.btn_atras);
        txError = findViewById(R.id.tx_error);
        etNombreGrupo = findViewById(R.id.et_name_grupo);
        btnGuardarGrupo = findViewById(R.id.btn_guardar_grupo);
    }

    private void configurarAcciones() {
        btnVolv.setOnClickListener(v -> finish());

        btnGuardarGrupo.setOnClickListener(v -> {
            String name = etNombreGrupo.getText().toString().trim();
            if (name.isEmpty()) {
                txError.setText("Por favor ingresa un nombre para el grupo familiar.");
                txError.setVisibility(View.VISIBLE);
                etNombreGrupo.requestFocus();
                return;
            }

            txError.setVisibility(View.GONE);
            btnGuardarGrupo.setEnabled(false);

            repositorio.crearGrupoFamiliar(name, new FreyrRepositorio.Callback<Grupofamiliar>() {
                @Override
                public void alTenerExito(Grupofamiliar grupoCreado) {
                    runOnUiThread(() -> {
                        Toast.makeText(CrearGrupoFamiliar.this, "¡Grupo familiar creado exitosamente!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                }

                @Override
                public void alOcurrirError(String error) {
                    runOnUiThread(() -> {
                        btnGuardarGrupo.setEnabled(true);
                        txError.setText(error);
                        txError.setVisibility(View.VISIBLE);
                    });
                }
            });
        });
    }
}

