
package com.freyr.app.Clases;

// =====================================================
// 1. IMPORTACIONES
// =====================================================

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
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


// =====================================================
// 2. CLASE PRINCIPAL
// =====================================================

// Actividad principal de la aplicación.
// Controla la navegación y las opciones de cuenta.
public class ActividadPrincipal extends AppCompatActivity {

    // =====================================================
    // 3. ATRIBUTOS
    // =====================================================

    // Elementos visuales de la actividad.
    private BottomNavigationView bottomNav;
    private FrameLayout btnProfileAvatar;
    private TextView tvUserInitials;

    // Repositorio para acceder a los datos de la aplicación.
    private FreyrRepositorio repository;

    // Arreglo que almacena los usuarios registrados.
    private Usuario[] allUsers = new Usuario[0];

    // Usuario que tiene la sesión iniciada.
    private Usuario currentUser;


    // =====================================================
    // 4. MÉTODO onCreate
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuración de la barra de estado.
        getWindow().setStatusBarColor(
                getResources().getColor(R.color.freyr_background)
        );

        getWindow().setStatusBarColor(
                getResources().getColor(R.color.freyr_background)
        );

        getWindow().getDecorView().setSystemUiVisibility(0);

        // Barra de estado Ciruela (#550724) con iconos blancos para ver hora y batería
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(Color.parseColor("#550724"));
        }

        WindowInsetsControllerCompat insetsController =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(false);

        // Obtiene el repositorio desde la clase FreyrApp.
        repository = ((FreyrApp) getApplication()).getRepositorio();


        // -------------------------------------------------
        // 4.1. COMPROBACIÓN DE LA SESIÓN
        // -------------------------------------------------

        // Comprueba sesión iniciada
        if (repository.obtenerIdUsuarioActual() == null) {

            Intent intent = new Intent(this, LoguearUsuario.class);

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();
            return;
        }


        // -------------------------------------------------
        // 4.2. CONFIGURACIÓN DE LA INTERFAZ
        // -------------------------------------------------

        setContentView(R.layout.activity_main);

        // Ajusta el contenido para respetar las barras del sistema.

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main_root),
                (view, windowInsets) -> {
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


        // -------------------------------------------------
        // 4.3. INICIALIZACIÓN DE COMPONENTES
        // -------------------------------------------------

        initViews();
        setupNavigation();
        observeUser();

        if (savedInstanceState == null) {
            loadFragment(new RecapMesFinancieroFragment());
        }
    }


    // =====================================================
    // 5. INICIALIZACIÓN DE VISTAS
    // =====================================================

    private void initViews() {

        // Vincula los elementos del diseño XML con Java.
        bottomNav = findViewById(R.id.bottom_navigation);
        btnProfileAvatar = findViewById(R.id.btn_profile_avatar);
        tvUserInitials = findViewById(R.id.tv_user_initials);

        // Ocultar las letras (DA / JU) y poner el icono de persona sin foto
        if (tvUserInitials != null) {
            tvUserInitials.setVisibility(View.GONE);
        }
        if (btnProfileAvatar != null) {
            btnProfileAvatar.setBackgroundResource(R.drawable.ic_avatar_ciruela);
            btnProfileAvatar.setOnClickListener(v -> showAccountDialog());
        }
        // Al pulsar el avatar, muestra las opciones de cuenta.
        btnProfileAvatar.setOnClickListener(v -> showAccountDialog());
    }


    // =====================================================
    // 6. CONFIGURACIÓN DE LA NAVEGACIÓN
    // =====================================================

    private void setupNavigation() {

        // Detecta qué opción de la barra inferior selecciona el usuario.
        bottomNav.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();

            // Panel principal.
            if (itemId == R.id.nav_dashboard) {
                loadFragment(new RecapMesFinancieroFragment());
                return true;

                // Finanzas personales.
            } else if (itemId == R.id.nav_personal) {
                loadFragment(new FinanzasPersonales());
                return true;

                // Grupos familiares.
            } else if (itemId == R.id.nav_groups) {
                loadFragment(new GruposFamiliares());
                return true;

                // Invitaciones.
            } else if (itemId == R.id.nav_invitations) {
                loadFragment(new InvitarGrupoFamiliar());
                return true;
            }

            return false;
        });
    }


    // =====================================================
    // 7. SELECCIÓN DE PESTAÑAS
    // =====================================================

    // Permite seleccionar una opción de la navegación desde otra clase.
    public void selectTab(int navItemId) {
        bottomNav.setSelectedItemId(navItemId);
    }


    // =====================================================
    // 8. CARGA DE FRAGMENTS
    // =====================================================

    // Reemplaza el contenido del contenedor por el fragment recibido.
    private void loadFragment(Fragment fragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }


    // =====================================================
    // 9. OBSERVACIÓN DE USUARIOS
    // =====================================================

    private void observeUser() {

        // Observa los cambios en la lista de usuarios de la base de datos.
        repository.observarTodosLosUsuarios().observe(this, users -> {
            if (users == null) {
                return;
            }

            // Convierte la lista recibida en un arreglo de usuarios.
            allUsers = users.toArray(new Usuario[0]);

            // Obtiene el identificador de la cuenta activa.
            String currentUid = repository.obtenerIdUsuarioActual();
            currentUser = null;

            // Busca al usuario que tiene la sesión iniciada.
            for (Usuario user : allUsers) {
                if (user.getId() != null && user.getId().equals(currentUid)) {
                    currentUser = user;

                    // Obtiene las dos primeras letras del nombre completo.
                    String fullName = user.getNomCompl();

                    String initials = fullName != null && fullName.length() >= 2
                            ? fullName.substring(0, 2).toUpperCase()
                            : "U";

                    tvUserInitials.setText(initials);

                    break;
                }
            }

            // Asegura que se mantenga el muñeco sin foto en el avatar
            if (tvUserInitials != null) {
                tvUserInitials.setVisibility(View.GONE);
            }
            if (btnProfileAvatar != null) {
                btnProfileAvatar.setBackgroundResource(R.drawable.ic_avatar_ciruela);
            }

            // Si no encuentra al usuario, muestra una inicial predeterminada.
            if (currentUser == null && tvUserInitials != null) {
                tvUserInitials.setText("U");
            }
        });
    }


    // =====================================================
    // 10. DIÁLOGO DE OPCIONES DE CUENTA
    // =====================================================

    /**
     * Muestra el menú de cambiar cuenta en tarjeta blanca, redondeada y moderna.
     */
    private void showAccountDialog() {

        if (currentUser == null) return;

        // Contenedor principal con fondo blanco y espaciado
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(48, 44, 48, 40);

        // Título limpio sin '/ Opciones'
        TextView tvTitle = new TextView(this);
        tvTitle.setText("Cambiar cuenta");
        tvTitle.setTextSize(18f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextColor(Color.parseColor("#550724"));
        tvTitle.setPadding(8, 0, 8, 20);
        root.addView(tvTitle);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(root)
                .create();

        // Lista de usuarios con diseño limpio
        for (Usuario user : allUsers) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(16, 20, 16, 20);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setClickable(true);
            row.setFocusable(true);

            boolean isCurrent = user.getId() != null && user.getId().equals(currentUser.getId());

            TextView tvUser = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            tvUser.setLayoutParams(lp);
            tvUser.setText(user.getNomCompl() + " (@" + user.getNomUsuario() + ")");
            tvUser.setTextSize(14f);
            tvUser.setTextColor(isCurrent ? Color.parseColor("#550724") : Color.parseColor("#2E0B17"));
            if (isCurrent) tvUser.setTypeface(null, Typeface.BOLD);
            row.addView(tvUser);

            // Identifica la cuenta que tiene la sesión activa.
            if (isCurrent) {
                TextView badge = new TextView(this);
                badge.setText("Activo");
                badge.setTextSize(10f);
                badge.setTextColor(Color.parseColor("#550724"));
                badge.setBackgroundColor(Color.parseColor("#F5EAEF"));
                badge.setPadding(18, 6, 18, 6);
                row.addView(badge);
            }

            row.setOnClickListener(v -> {
                dialog.dismiss();
                repository.setLoggedInUser(user.getId());
                loadFragment(new RecapMesFinancieroFragment());
                bottomNav.setSelectedItemId(R.id.nav_dashboard);
            });

            root.addView(row);
        }

        // Línea divisoria suave
        View divider = new View(this);
        LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2);
        divLp.setMargins(8, 14, 8, 14);
        divider.setLayoutParams(divLp);
        divider.setBackgroundColor(Color.parseColor("#F0E8EB"));
        root.addView(divider);

        // Opción: Ver perfil
        TextView btnProfile = new TextView(this);
        btnProfile.setText("Ver perfil");
        btnProfile.setTextSize(14f);
        btnProfile.setTextColor(Color.parseColor("#2E0B17"));
        btnProfile.setPadding(16, 18, 16, 18);
        btnProfile.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(ActividadPrincipal.this, VerPerfilActivity.class));
        });
        root.addView(btnProfile);

        // Opción: Cerrar sesión
        TextView btnLogout = new TextView(this);
        btnLogout.setText("Cerrar sesión");
        btnLogout.setTextSize(14f);
        btnLogout.setTypeface(null, Typeface.BOLD);
        btnLogout.setTextColor(Color.parseColor("#DC2626"));
        btnLogout.setPadding(16, 18, 16, 18);
        btnLogout.setOnClickListener(v -> {
            dialog.dismiss();
            repository.setLoggedInUser(null);
            Intent intent = new Intent(ActividadPrincipal.this, LoguearUsuario.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        root.addView(btnLogout);

        // Botón Cancelar en tono suave ciruela
        Button btnCancel = new Button(this);
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnLp.setMargins(0, 16, 0, 0);
        btnCancel.setLayoutParams(btnLp);
        btnCancel.setText("Cancelar");
        btnCancel.setTextColor(Color.parseColor("#550724"));
        btnCancel.setBackgroundColor(Color.parseColor("#F8EEF1"));
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        root.addView(btnCancel);

        dialog.show();

        // Forzar fondo blanco y esquinas redondeadas
        if (dialog.getWindow() != null) {
            GradientDrawable bgShape = new GradientDrawable();
            bgShape.setColor(Color.WHITE);
            bgShape.setCornerRadius(48f); // 24dp de radio
            dialog.getWindow().setBackgroundDrawable(bgShape);
        }
    }
}
