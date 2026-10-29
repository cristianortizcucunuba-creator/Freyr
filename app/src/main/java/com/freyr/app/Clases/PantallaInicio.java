package com.freyr.app.Clases;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

import com.freyr.app.Clases.autenticacion.LoguearUsuario;
import com.freyr.app.FreyrApp;
import com.freyr.app.R;
import com.freyr.app.data.repositorio.FreyrRepositorio;

public class PantallaInicio extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ocultar barra superior durante el video (Pantalla Completa)
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_splash);

        VideoView videoView = findViewById(R.id.videoViewSplash);
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.nuevo_video_intro;
        videoView.setVideoURI(Uri.parse(videoPath));

        // Cuando el video termine de reproducirse, decide a qué pantalla ir
        videoView.setOnCompletionListener(mp -> {
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
        });

        videoView.start();
    }
}