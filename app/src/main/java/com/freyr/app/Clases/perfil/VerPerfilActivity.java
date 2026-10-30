
package com.freyr.app.Clases.perfil;

// 1. IMPORTACIONES
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.freyr.app.FreyrApp;
import com.freyr.app.R;
import com.freyr.app.data.datos.Deuda;
import com.freyr.app.data.datos.Grupofamiliar;
import com.freyr.app.data.datos.Pago;
import com.freyr.app.data.datos.Usuario;
import com.freyr.app.data.repositorio.FreyrRepositorio;

import java.util.LinkedList;
import java.util.List;


/**
 * Actividad encargada de mostrar el perfil personal del usuario.
 *
 * Presenta un mensaje de bienvenida, información personal,
 * estadísticas financieras y acceso a la edición del perfil.
 *
 * No utiliza ArrayList.
 */


// 2. CLASE PRINCIPAL
public class VerPerfilActivity extends AppCompatActivity {


    // 3. ATRIBUTOS

    // Botón para regresar a la pantalla anterior.
    private ImageButton btnBack;

    // Componentes para mostrar la información personal.
    private TextView tvInitials;
    private TextView tvFullName;
    private TextView tvUsername;
    private TextView tvEmail;

    // Componentes para mostrar las estadísticas.
    private TextView tvStatDebts;
    private TextView tvStatPaid;
    private TextView tvStatGroups;

    // Botón para acceder a la edición del perfil.
    private Button btnGoToEdit;


    // Objetos para acceder a los datos de la aplicación.
    private FreyrRepositorio repository;
    private Usuario currentUser;

    // Listas que almacenan las deudas, pagos y grupos.
    private List<Deuda> allDebts = new LinkedList<>();
    private List<Pago> allPayments = new LinkedList<>();
    private List<Grupofamiliar> allGroups = new LinkedList<>();


    // 4. CICLO DE VIDA DE LA ACTIVIDAD
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_perfil);

        // Obtiene el repositorio desde la aplicación.
        repository = ((FreyrApp) getApplication()).getRepositorio();

        // Inicializa los componentes de la interfaz.
        initViews();

        // Configura las acciones de los botones.
        setupListeners();

        // Comienza a observar los datos del usuario.
        observeData();
    }


    // 5. INICIALIZACIÓN DE VISTAS
    private void initViews() {

        btnBack = findViewById(R.id.btn_back_profile);

        tvInitials = findViewById(R.id.tv_profile_initials);
        tvFullName = findViewById(R.id.tv_profile_full_name);
        tvUsername = findViewById(R.id.tv_profile_username);
        tvEmail = findViewById(R.id.tv_profile_email);

        tvStatDebts = findViewById(R.id.tv_stat_debts_count);
        tvStatPaid = findViewById(R.id.tv_stat_paid_total);
        tvStatGroups = findViewById(R.id.tv_stat_groups_count);

        btnGoToEdit = findViewById(R.id.btn_go_to_edit_profile);
    }


    // 6. CONFIGURACIÓN DE EVENTOS
    private void setupListeners() {

        // Regresa a la pantalla anterior.
        btnBack.setOnClickListener(v -> finish());

        // Abre la actividad para editar el perfil.
        btnGoToEdit.setOnClickListener(v -> {

            Intent intent = new Intent(
                    VerPerfilActivity.this,
                    ActividadEditarPerfil.class
            );

            startActivity(intent);
        });
    }


    // 7. OBSERVACIÓN DE DATOS
    private void observeData() {

        // 7.1. OBSERVAR USUARIOS
        repository.observarTodosLosUsuarios().observe(this, users -> {

            if (users != null) {

                // Obtiene el identificador del usuario actual.
                String curId = repository.obtenerIdUsuarioActual();

                // Busca al usuario que tiene la sesión iniciada.
                for (Usuario u : users) {

                    if (u.getId().equals(curId)) {

                        currentUser = u;

                        // Muestra la información personal.
                        tvFullName.setText(u.getNomCompl());
                        tvUsername.setText("@" + u.getNomUsuario());
                        tvEmail.setText(u.getEmail());

                        // Obtiene las iniciales del nombre completo.
                        String initials =
                                u.getNomCompl() != null
                                        && u.getNomCompl().length() >= 2
                                        ? u.getNomCompl()
                                        .substring(0, 2)
                                        .toUpperCase()
                                        : "U";

                        tvInitials.setText(initials);

                        break;
                    }
                }

                // Actualiza las estadísticas del perfil.
                updateStats();
            }
        });


        // 7.2. OBSERVAR DEUDAS
        repository.observarTodasLasDeudas().observe(this, debts -> {

            if (debts != null) {
                allDebts = debts;
                updateStats();
            }
        });


        // 7.3. OBSERVAR PAGOS
        repository.observarTodosLosPagos().observe(this, payments -> {

            if (payments != null) {
                allPayments = payments;
                updateStats();
            }
        });


        // 7.4. OBSERVAR GRUPOS FAMILIARES
        repository.observarTodosLosGrupos().observe(this, groups -> {

            if (groups != null) {
                allGroups = groups;
                updateStats();
            }
        });
    }


    // 8. ACTUALIZACIÓN DE ESTADÍSTICAS
    private void updateStats() {

        // Comprueba que exista un usuario actual.
        if (currentUser == null) return;

        String uid = currentUser.getId();


        // 8.1. CONTAR DEUDAS DEL USUARIO
        int debtsCount = 0;

        for (Deuda d : allDebts) {

            if (uid.equals(d.getIdUsuarioAsignado())
                    || uid.equals(d.getCreadoPor())) {

                debtsCount++;
            }
        }


        // 8.2. CALCULAR EL TOTAL DE PAGOS REALIZADOS
        double paidTotal = 0.0;

        for (Pago p : allPayments) {

            if (uid.equals(p.getUsuarioId())) {
                paidTotal += p.getMonto();
            }
        }


        // 8.3. CONTAR GRUPOS FAMILIARES
        int groupsCount = 0;

        for (Grupofamiliar g : allGroups) {

            if (g.getMiembrosIds() != null
                    && g.getMiembrosIds().contains(uid)) {

                groupsCount++;
            }
        }


        // 8.4. MOSTRAR LOS RESULTADOS EN PANTALLA
        tvStatDebts.setText(String.valueOf(debtsCount));

        tvStatPaid.setText(
                FreyrRepositorio.formatearMoneda(paidTotal)
        );

        tvStatGroups.setText(String.valueOf(groupsCount));
    }
}
