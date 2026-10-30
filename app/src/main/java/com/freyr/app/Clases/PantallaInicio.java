
package com.freyr.app.Clases;

// 1. IMPORTACIONES
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


// 2. CLASE PRINCIPAL
public class PantallaInicio extends AppCompatActivity {


    // 3. CICLO DE VIDA DE LA ACTIVIDAD
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 3.1. CONFIGURAR PANTALLA COMPLETA
        // Oculta la barra superior durante la reproducción del video.
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );


        // 3.2. CARGAR DISEÑO XML
        setContentView(R.layout.activity_splash);

        // Obtiene el componente encargado de reproducir el video.
        VideoView videoView = findViewById(R.id.videoViewSplash);


        // 4. CONFIGURAR VIDEO
        // Construye la ruta del video almacenado en res/raw.
        String videoPath = "android.resource://" + getPackageName()
                + "/" + R.raw.nuevo_video_intro;

        // Establece el video que se reproducirá.
        videoView.setVideoURI(Uri.parse(videoPath));


        // 5. PREPARAR Y REPRODUCIR EL VIDEO
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {

                // Obtiene la proporción entre el ancho y el alto del video.
                float videoRatio = mp.getVideoWidth()
                        / (float) mp.getVideoHeight();

                // Obtiene la proporción de la pantalla donde se reproduce.
                float screenRatio = (float) videoView.getWidth()
                        / videoView.getHeight();

                // Calcula el ajuste horizontal necesario.
                float scaleX = videoRatio / screenRatio;

                // Ajusta la escala del video para adaptarlo a la pantalla.
                if (scaleX >= 1f) {
                    videoView.setScaleX(scaleX);
                } else {
                    videoView.setScaleY(1f / scaleX);
                }

                // Inicia la reproducción cuando el video está preparado.
                mp.start();
            }
        });


        // 6. TRANSICIÓN AL TERMINAR EL VIDEO
        videoView.setOnCompletionListener(mp -> {

            // Comprueba que la actividad todavía esté disponible.
            if (isFinishing() || isDestroyed()) return;

            // Obtiene el repositorio de la aplicación.
            FreyrRepositorio repositorio =
                    ((FreyrApp) getApplication()).getRepositorio();

            // Obtiene el identificador del usuario actual.
            String idUsuarioActual = repositorio.obtenerIdUsuarioActual();


            // 6.1. DETERMINAR LA SIGUIENTE PANTALLA
            Intent intent;

            if (idUsuarioActual != null
                    && !idUsuarioActual.trim().isEmpty()) {

                // Si existe una sesión, abre la pantalla principal.
                intent = new Intent(
                        PantallaInicio.this,
                        ActividadPrincipal.class
                );

            } else {

                // Si no existe una sesión, abre el inicio de sesión.
                intent = new Intent(
                        PantallaInicio.this,
                        LoguearUsuario.class
                );
            }


            // 6.2. CAMBIAR DE ACTIVIDAD
            startActivity(intent);

            // Aplica una animación de desvanecimiento.
            overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            );

            // Finaliza la pantalla de inicio.
            finish();
        });
    }
}
