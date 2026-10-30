package com.freyr.app.Clases;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.freyr.app.data.datos.Usuario;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.freyr.app.FreyrApp;
import com.freyr.app.R;
import com.freyr.app.data.repositorio.FreyrRepositorio;
import com.freyr.app.Clases.autenticacion.LoguearUsuario;
import com.freyr.app.Clases.perfil.VerPerfilActivity;
import com.freyr.app.Clases.resumen.RecapMesFinancieroFragment;
import com.freyr.app.Clases.deuda.FinanzasPersonales;
import com.freyr.app.Clases.grupo.GruposFamiliares;
import com.freyr.app.Clases.grupo.InvitarGrupoFamiliar;

public class ActividadPrincipal extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private FrameLayout btnProfileAvatar;

    private FreyrRepositorio repository;

    private Usuario[] allUsers = new Usuario[0];
    private Usuario currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Barra ciruela (#550724) con iconos blancos
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(Color.parseColor("#550724"));
        }

        WindowInsetsControllerCompat insetsController =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(false);

        repository = ((FreyrApp) getApplication()).getRepositorio();

        if (repository.obtenerIdUsuarioActual() == null) {
            Intent intent = new Intent(this, LoguearUsuario.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main_root), (view, windowInsets) -> {
                    Insets systemBars = windowInsets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    view.setPadding(
                            view.getPaddingLeft(),
                            systemBars.top,
                            view.getPaddingRight(),
                            0
                    );

                    return windowInsets;
                }
        );

        initViews();
        setupNavigation();
        observeUser();

        if (savedInstanceState == null) {
            loadFragment(new RecapMesFinancieroFragment());
        }
    }

    private void initViews() {
        bottomNav = findViewById(R.id.bottom_navigation);
        btnProfileAvatar = findViewById(R.id.btn_profile_avatar);

        if (btnProfileAvatar != null) {
            btnProfileAvatar.setOnClickListener(v -> showAccountDialog());
        }
    }

    private void setupNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {
                loadFragment(new RecapMesFinancieroFragment());
                return true;
            } else if (itemId == R.id.nav_personal) {
                loadFragment(new FinanzasPersonales());
                return true;
            } else if (itemId == R.id.nav_groups) {
                loadFragment(new GruposFamiliares());
                return true;
            } else if (itemId == R.id.nav_invitations) {
                loadFragment(new InvitarGrupoFamiliar());
                return true;
            }

            return false;
        });
    }

    public void selectTab(int navItemId) {
        bottomNav.setSelectedItemId(navItemId);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void observeUser() {
        repository.observarTodosLosUsuarios().observe(this, users -> {
            if (users == null) {
                return;
            }

            allUsers = users.toArray(new Usuario[0]);
            String currentUid = repository.obtenerIdUsuarioActual();
            currentUser = null;

            for (Usuario user : allUsers) {
                if (user.getId() != null && user.getId().equals(currentUid)) {
                    currentUser = user;
                    break;
                }
            }
        });
    }

    private void showAccountDialog() {
        if (currentUser == null) return;

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_cambiar_cuenta, null);
        LinearLayout usersContainer = dialogView.findViewById(R.id.container_users);
        TextView btnVerPerfil = dialogView.findViewById(R.id.btn_dialog_ver_perfil);
        TextView btnLogout = dialogView.findViewById(R.id.btn_dialog_logout);
        Button btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setBackground(new ColorDrawable(Color.TRANSPARENT))
                .create();

        for (Usuario user : allUsers) {
            LinearLayout userRow = new LinearLayout(this);
            userRow.setOrientation(LinearLayout.HORIZONTAL);
            userRow.setPadding(20, 22, 20, 22);
            userRow.setGravity(Gravity.CENTER_VERTICAL);
            userRow.setBackgroundResource(R.drawable.bg_dialog_item_ripple);

            boolean isCurrent = user.getId() != null && user.getId().equals(currentUser.getId());

            TextView tvInfo = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            tvInfo.setLayoutParams(params);
            tvInfo.setText(user.getNomCompl() + " (@" + user.getNomUsuario() + ")");
            tvInfo.setTextColor(isCurrent ? Color.parseColor("#550724") : Color.parseColor("#2E0B17"));
            tvInfo.setTextSize(14f);
            if (isCurrent) tvInfo.setTypeface(null, Typeface.BOLD);
            userRow.addView(tvInfo);

            if (isCurrent) {
                TextView badge = new TextView(this);
                badge.setText("Activo");
                badge.setTextSize(11f);
                badge.setTextColor(Color.parseColor("#550724"));
                badge.setBackgroundColor(Color.parseColor("#F5EAEF"));
                badge.setPadding(20, 8, 20, 8);
                userRow.addView(badge);
            }

            userRow.setOnClickListener(v -> {
                dialog.dismiss();
                repository.setLoggedInUser(user.getId());
                loadFragment(new RecapMesFinancieroFragment());
                bottomNav.setSelectedItemId(R.id.nav_dashboard);
            });

            usersContainer.addView(userRow);
        }

        btnVerPerfil.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(ActividadPrincipal.this, VerPerfilActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            dialog.dismiss();
            repository.setLoggedInUser(null);
            Intent intent = new Intent(ActividadPrincipal.this, LoguearUsuario.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        if (dialog.getWindow() != null) {
            GradientDrawable bgShape = new GradientDrawable();
            bgShape.setColor(Color.WHITE);
            bgShape.setCornerRadius(48f);
            dialog.getWindow().setBackgroundDrawable(bgShape);
        }
    }
}