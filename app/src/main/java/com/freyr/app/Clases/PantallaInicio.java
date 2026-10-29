package com.freyr.app.Clases;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.freyr.app.FreyrApp;
import com.freyr.app.R;
import com.freyr.app.data.repositorio.FreyrRepositorio;
import com.freyr.app.Clases.autenticacion.LoguearUsuario;

//Pantalla de inicio que muestra el logo detallado Freyr (Image 2) en la paleta oscura antes de pasar a la actividad principal.
public class PantallaInicio extends AppCompatActivity {

    private static final int DURACION_INICIO_MS = 1400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;

            FreyrRepositorio repositorio = ((FreyrApp) getApplication()).getRepository();
            String idUsuarioActual = repositorio.getCurrentUserId();

            Intent intent;
            if (idUsuarioActual != null && !idUsuarioActual.trim().isEmpty()) {
                intent = new Intent(PantallaInicio.this, ActividadPrincipal.class);
            } else {
                intent = new Intent(PantallaInicio.this, LoguearUsuario.class);
            }

            startActivity(intent);
            finish();
        }, DURACION_INICIO_MS);
    }
}
