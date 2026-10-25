package com.freyr.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.freyr.app.FreyrApplication;
import com.freyr.app.R;
import com.freyr.app.data.model.User;
import com.freyr.app.data.repository.FreyrRepository;
import com.freyr.app.ui.auth.LoginActivity;
import com.freyr.app.ui.dashboard.DashboardFragment;
import com.freyr.app.ui.debt.PersonalFinancesFragment;
import com.freyr.app.ui.group.FamilyGroupsFragment;
import com.freyr.app.ui.group.InvitationsFragment;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private FrameLayout btnProfileAvatar;
    private TextView tvUserInitials;

    private FreyrRepository repository;
    private List<User> allUsers = new ArrayList<>();
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        repository = ((FreyrApplication) getApplication()).getRepository();

        // Check login state
        if (repository.getCurrentUserId() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        initViews();
        setupNavigation();
        observeUser();

        // Default to Dashboard
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
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
                loadFragment(new DashboardFragment());
                return true;
            } else if (itemId == R.id.nav_personal) {
                loadFragment(new PersonalFinancesFragment());
                return true;
            } else if (itemId == R.id.nav_groups) {
                loadFragment(new FamilyGroupsFragment());
                return true;
            } else if (itemId == R.id.nav_invitations) {
                loadFragment(new InvitationsFragment());
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
                allUsers = users;
                String currentUid = repository.getCurrentUserId();
                for (User u : users) {
                    if (u.getId().equals(currentUid)) {
                        currentUser = u;
                        String initials = u.getFullName() != null && u.getFullName().length() >= 2
                            ? u.getFullName().substring(0, 2).toUpperCase()
                            : "U";
                        tvUserInitials.setText(initials);
                        break;
                    }
                }
            }
        });
    }

    /**
     * Muestra el diálogo de selección de usuario o cierre de sesión rápido.
     * Permite alternar fácilmente entre perfiles demostrativos o cerrar la sesión actual.
     */
    private void showAccountDialog() {
        if (currentUser == null) return;

        List<String> userOptions = new ArrayList<>();
        for (User u : allUsers) {
            String label = u.getFullName() + " (@" + u.getUsername() + ")";
            if (u.getId().equals(currentUser.getId())) {
                label += " (Sesión activa)";
            }
            userOptions.add(label);
        }

        userOptions.add("Cerrar sesión");

        CharSequence[] items = userOptions.toArray(new CharSequence[0]);

        new AlertDialog.Builder(this)
            .setTitle("Cambiar cuenta / Opciones")
            .setItems(items, (dialog, which) -> {
                if (which == userOptions.size() - 1) {
                    // Cerrar sesión
                    repository.setLoggedInUser(null);
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    // Cambiar de usuario
                    User selected = allUsers.get(which);
                    repository.setLoggedInUser(selected.getId());
                    loadFragment(new DashboardFragment());
                    bottomNav.setSelectedItemId(R.id.nav_dashboard);
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
}
