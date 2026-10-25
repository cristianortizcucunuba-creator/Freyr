package com.freyr.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.freyr.app.FreyrApplication;
import com.freyr.app.R;
import com.freyr.app.data.repository.FreyrRepository;
import com.freyr.app.ui.auth.LoginActivity;

/**
 * Pantalla de inicio (Splash / Startup Screen) que muestra el logo detallado
 * de Freyr (Image 2) en la paleta oscura antes de pasar a la actividad principal.
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION_MS = 1400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;

            FreyrRepository repository = ((FreyrApplication) getApplication()).getRepository();
            String currentUserId = repository.getCurrentUserId();

            Intent intent;
            if (currentUserId != null && !currentUserId.trim().isEmpty()) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish();
        }, SPLASH_DURATION_MS);
    }
}
