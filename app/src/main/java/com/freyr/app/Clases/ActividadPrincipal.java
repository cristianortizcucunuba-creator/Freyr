
package com.freyr.app.Clases;

// =====================================================
// 1. IMPORTACIONES
// =====================================================

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.freyr.app.data.datos.Usuario;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

        // Obtiene el repositorio desde la clase FreyrApp.
        repository = ((FreyrApp) getApplication()).getRepositorio();


        // -------------------------------------------------
        // 4.1. COMPROBACIÓN DE LA SESIÓN
        // -------------------------------------------------

        // Comprueba que exista una sesión iniciada.
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
                            view.getPaddingBottom()
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

        // Muestra el panel principal al iniciar la actividad.
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

            // Si no encuentra al usuario, muestra una inicial predeterminada.
            if (currentUser == null) {
                tvUserInitials.setText("U");
            }
        });
    }


    // =====================================================
    // 10. DIÁLOGO DE OPCIONES DE CUENTA
    // =====================================================

    /**
     * Muestra las cuentas disponibles y permite cambiar de usuario
     * o cerrar la sesión actual limpiando el historial de navegación.
     */
    private void showAccountDialog() {

        if (currentUser == null) {
            return;
        }

        // Reserva una opción para cada usuario,
        // otra para ver el perfil y otra para cerrar sesión.
        CharSequence[] userOptions = new CharSequence[allUsers.length + 2];


        // -------------------------------------------------
        // 10.1. CONSTRUCCIÓN DE LAS OPCIONES DE USUARIO
        // -------------------------------------------------

        for (int i = 0; i < allUsers.length; i++) {

            Usuario user = allUsers[i];

            String label = user.getNomCompl()
                    + " (@"
                    + user.getNomUsuario()
                    + ")";

            // Identifica la cuenta que tiene la sesión activa.
            if (user.getId().equals(currentUser.getId())) {
                label += " (Sesión activa)";
            }

            userOptions[i] = label;
        }

        // Opciones adicionales del diálogo.
        userOptions[allUsers.length] = "Ver perfil";
        userOptions[allUsers.length + 1] = "Cerrar sesión";


        // -------------------------------------------------
        // 10.2. CONFIGURACIÓN DEL DIÁLOGO
        // -------------------------------------------------

        new AlertDialog.Builder(this)
                .setTitle("Cambiar cuenta / Opciones")

                .setItems(userOptions, (dialog, which) -> {

                    // -------------------------------------
                    // 10.2.1. CERRAR SESIÓN
                    // -------------------------------------

                    if (which == allUsers.length + 1) {

                        // Limpia la sesión guardada en SharedPreferences.
                        repository.setLoggedInUser(null);

                        // Redirige al inicio de sesión y borra
                        // el historial de actividades anteriores.
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

                        // -------------------------------------
                        // 10.2.2. VER PERFIL
                        // -------------------------------------

                    } else if (which == allUsers.length) {

                        startActivity(
                                new Intent(
                                        ActividadPrincipal.this,
                                        VerPerfilActivity.class
                                )
                        );

                        // -------------------------------------
                        // 10.2.3. CAMBIAR DE CUENTA
                        // -------------------------------------

                    } else if (which >= 0 && which < allUsers.length) {

                        // Obtiene el usuario seleccionado.
                        Usuario selectedUser = allUsers[which];

                        // Actualiza la cuenta activa.
                        repository.setLoggedInUser(selectedUser.getId());

                        // Regresa al panel principal.
                        loadFragment(new RecapMesFinancieroFragment());

                        // Selecciona la pestaña del panel principal.
                        bottomNav.setSelectedItemId(R.id.nav_dashboard);
                    }
                })

                // Botón para cerrar el diálogo sin realizar cambios.
                .setNegativeButton("Cancelar", null)

                .show();
    }
}
