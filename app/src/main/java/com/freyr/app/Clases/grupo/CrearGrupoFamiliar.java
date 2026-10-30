
package com.freyr.app.Clases.grupo;

// 1. IMPORTACIONES
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
 * Actividad para la creación de un nuevo grupo familiar.
 *
 * Permite registrar el nombre del grupo y asigna automáticamente
 * al usuario creador como su primer miembro y administrador.
 */
public class CrearGrupoFamiliar extends AppCompatActivity {

    // 2. DECLARACIÓN DE COMPONENTES
    private ImageButton btnVolv;
    private TextView txError;
    private EditText etNombreGrupo;
    private Button btnGuardarGrupo;

    // 3. REPOSITORIO
    private FreyrRepositorio repositorio;


    // 4. CICLO DE VIDA DE LA ACTIVIDAD
    @Override
    protected void onCreate(Bundle estadoGuardado) {
        super.onCreate(estadoGuardado);
        setContentView(R.layout.activity_create_family_group);

        // Obtener el repositorio de la aplicación
        repositorio = ((FreyrApp) getApplication()).getRepositorio();

        inicializarVistas();
        configurarAcciones();
    }


    // 5. INICIALIZACIÓN DE COMPONENTES
    private void inicializarVistas() {
        btnVolv = findViewById(R.id.btn_atras);
        txError = findViewById(R.id.tx_error);
        etNombreGrupo = findViewById(R.id.et_name_grupo);
        btnGuardarGrupo = findViewById(R.id.btn_guardar_grupo);
    }


    // 6. CONFIGURACIÓN DE EVENTOS
    private void configurarAcciones() {

        // Regresar a la pantalla anterior
        btnVolv.setOnClickListener(v -> finish());

        // Registrar un nuevo grupo familiar
        btnGuardarGrupo.setOnClickListener(v -> {

            // Obtener el nombre ingresado y eliminar espacios innecesarios
            String name = etNombreGrupo.getText().toString().trim();

            // Validar que el nombre no esté vacío
            if (name.isEmpty()) {
                txError.setText(
                        "Por favor ingresa un nombre para el grupo familiar."
                );

                txError.setVisibility(View.VISIBLE);
                etNombreGrupo.requestFocus();
                return;
            }

            // Ocultar errores anteriores y desactivar el botón
            txError.setVisibility(View.GONE);
            btnGuardarGrupo.setEnabled(false);

            // Solicitar al repositorio la creación del grupo
            repositorio.crearGrupoFamiliar(
                    name,
                    new FreyrRepositorio.Callback<Grupofamiliar>() {

                        // Se ejecuta cuando el grupo se crea correctamente
                        @Override
                        public void alTenerExito(Grupofamiliar grupoCreado) {
                            runOnUiThread(() -> {

                                Toast.makeText(
                                        CrearGrupoFamiliar.this,
                                        "¡Grupo familiar creado exitosamente!",
                                        Toast.LENGTH_SHORT
                                ).show();

                                // Informar que la operación terminó correctamente
                                setResult(RESULT_OK);
                                finish();
                            });
                        }

                        // Se ejecuta cuando ocurre un error
                        @Override
                        public void alOcurrirError(String error) {
                            runOnUiThread(() -> {

                                // Reactivar el botón para permitir otro intento
                                btnGuardarGrupo.setEnabled(true);

                                // Mostrar el mensaje de error
                                txError.setText(error);
                                txError.setVisibility(View.VISIBLE);
                            });
                        }
                    }
            );
        });
    }
}
