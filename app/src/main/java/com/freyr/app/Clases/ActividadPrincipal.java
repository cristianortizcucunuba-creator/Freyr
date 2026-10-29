package com.freyr.app.Clases;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.freyr.app.data.model.Usuario;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.freyr.app.FreyrApp;
import com.freyr.app.R;
import com.freyr.app.data.repository.FreyrRepositorio;
import com.freyr.app.Clases.autenticacion.LoguearUsuario;
import com.freyr.app.Clases.perfil.VerPerfilActivity;
import com.freyr.app.Clases.resumen.RecapMesFinancieroFragment;
import com.freyr.app.Clases.deuda.FinanzasPersonales;
import com.freyr.app.Clases.grupo.GruposFamiliares;
import com.freyr.app.Clases.grupo.InvitarGrupoFamiliar;

public class ActividadPrincipal extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private FrameLayout btnProfileAvatar;
    private TextView tvUserInitials;

    private FreyrRepositorio repository;

    //Guarda los usuarios en un arreglo.
    private Usuario[] allUsers = new Usuario[0];
    private Usuario currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        repository = ((FreyrApp) getApplication()).getRepositorio();

        // Comprueba que exista una sesión iniciada.
        if (repository.getCurrentUserId() == null) {
            startActivity(new Intent(this, LoguearUsuario.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        initViews();
        setupNavigation();
        observeUser();

        // Muestra el panel principal al iniciar la actividad.
        if (savedInstanceState == null) {
            loadFragment(new RecapMesFinancieroFragment());
        }
    }

    private void initViews() {
        bottomNav = findViewById(R.id.bottom_navigation);
        btnProfileAvatar = findViewById(R.id.btn_profile_avatar);
        tvUserInitials = findViewById(R.id.tv_user_initials);

        btnProfileAvatar.setOnClickListener(v -> showAccountDialog());
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
        repository.getAllUsersLive().observe(this, users -> {
            if (users != null) {
                // El repositorio entrega una lista; la convertimos a arreglo.
                allUsers = users.toArray(new Usuario[0]);

                String currentUid = repository.getCurrentUserId();

                for (Usuario user : allUsers) {
                    if (user.getId().equals(currentUid)) {
                        currentUser = user;

                        String fullName = user.getNomCompl();
                        String initials = fullName != null && fullName.length() >= 2
                                ? fullName.substring(0, 2).toUpperCase()
                                : "U";

                        tvUserInitials.setText(initials);
                        break;
                    }
                }
            }
        });
    }

    /**
     * Muestra las cuentas disponibles y permite cambiar de usuario
     * o cerrar la sesión actual.
     */
    private void showAccountDialog() {
        if (currentUser == null) {
            return;
        }

        // Reserva una opción para cada usuario, una para Ver perfil y otra para cerrar sesión.
        CharSequence[] userOptions = new CharSequence[allUsers.length + 2];

        for (int i = 0; i < allUsers.length; i++) {
            Usuario user = allUsers[i];
            String label = user.getNomCompl()
                    + " (@"
                    + user.getNomUsuario()
                    + ")";

            if (user.getId().equals(currentUser.getId())) {
                label += " (Sesión activa)";
            }

            userOptions[i] = label;
        }

        userOptions[allUsers.length] = "Ver perfil";
        userOptions[allUsers.length + 1] = "Cerrar sesión";

        new AlertDialog.Builder(this)
                .setTitle("Cambiar cuenta / Opciones")
                .setItems(userOptions, (dialog, which) -> {
                    if (which == allUsers.length + 1) {
                        // Cierra la sesión actual.
                        repository.setLoggedInUser(null);

                        Intent intent = new Intent(
                                ActividadPrincipal.this,
                                LoguearUsuario.class
                        );
                        intent.setFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        );
                        startActivity(intent);
                        finish();
                    } else if (which == allUsers.length) {
                        // Ver perfil
                        startActivity(new Intent(ActividadPrincipal.this, VerPerfilActivity.class));
                    } else if (which >= 0 && which < allUsers.length) {
                        // Cambia a la cuenta seleccionada.
                        Usuario selectedUser = allUsers[which];
                        repository.setLoggedInUser(selectedUser.getId());
                        loadFragment(new RecapMesFinancieroFragment());
                        bottomNav.setSelectedItemId(R.id.nav_dashboard);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}