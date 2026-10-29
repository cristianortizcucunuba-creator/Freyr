package com.freyr.app.Clases;

import android.content.Intent;
import android.media.MediaPlayer;
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

        // Cambia 'nuevo_video_intro' si le pusiste otro nombre a tu MP4 en res/raw
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.nuevo_video_intro;
        videoView.setVideoURI(Uri.parse(videoPath));

        // Ajustar la escala del video para que tome toda la pantalla sin bordes (Center Crop)
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                float videoRatio = mp.getVideoWidth() / (float) mp.getVideoHeight();
                float screenRatio = videoView.getWidth() / (float) videoView.getHeight();
                float scaleX = videoRatio / screenRatio;

                if (scaleX >= 1f) {
                    videoView.setScaleX(scaleX);
                } else {
                    videoView.setScaleY(1f / scaleX);
                }
            }
        });

        // Transición automática al terminar la animación
        videoView.setOnCompletionListener(mp -> {
            if (isFinishing() || isDestroyed()) return;

            FreyrRepositorio repositorio = ((FreyrApp) getApplication()).getRepositorio();
            String idUsuarioActual = repositorio.obtenerIdUsuarioActual();

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