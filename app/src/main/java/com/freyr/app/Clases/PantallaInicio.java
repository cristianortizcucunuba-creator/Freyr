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

        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.nuevo_video_intro;
        videoView.setVideoURI(Uri.parse(videoPath));

        // Ajustar la escala del video y arrancar cuando esté preparado
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                float videoRatio = mp.getVideoWidth() / (float) mp.getVideoHeight();
                float screenRatio = (float) videoView.getWidth() / videoView.getHeight();
                float scaleX = videoRatio / screenRatio;

                if (scaleX >= 1f) {
                    videoView.setScaleX(scaleX);
                } else {
                    videoView.setScaleY(1f / scaleX);
                }

                // Arrancamos el video directamente
                mp.start();
            }
        });

        // Transición automática al terminar el video
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

            // Animación suave de desvanecimiento al cambiar de pantalla
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

            finish();
        });
    }
}