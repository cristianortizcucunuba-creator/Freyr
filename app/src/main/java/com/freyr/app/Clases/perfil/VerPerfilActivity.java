package com.freyr.app.Clases.perfil;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.freyr.app.FreyrApplication;
import com.freyr.app.R;
import com.freyr.app.data.model.Deuda;
import com.freyr.app.data.model.Grupofamiliar;
import com.freyr.app.data.model.Pago;
import com.freyr.app.data.model.Usuario;
import com.freyr.app.data.repository.FreyrRepository;
import java.util.LinkedList;
import java.util.List;

/**
 * Actividad dedicada a la visualización de perfil personal con mensaje de bienvenida,
 * resumen de estadísticas de actividad financiera y acceso a la edición de datos.
 * Cumple con el requisito estricto de NO USAR ArrayList.
 */
public class VerPerfilActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvInitials;
    private TextView tvFullName;
    private TextView tvUsername;
    private TextView tvEmail;
    private TextView tvStatDebts;
    private TextView tvStatPaid;
    private TextView tvStatGroups;
    private Button btnGoToEdit;

    private FreyrRepository repository;
    private Usuario currentUser;
    private List<Deuda> allDebts = new LinkedList<>();
    private List<Pago> allPayments = new LinkedList<>();
    private List<Grupofamiliar> allGroups = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_perfil);

        repository = ((FreyrApplication) getApplication()).getRepository();

        initViews();
        setupListeners();
        observeData();
    }

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

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnGoToEdit.setOnClickListener(v -> {
            Intent intent = new Intent(VerPerfilActivity.this, ActividadEditarPerfil.class);
            startActivity(intent);
        });
    }

    private void observeData() {
        repository.getAllUsersLive().observe(this, users -> {
            if (users != null) {
                String curId = repository.getCurrentUserId();
                for (Usuario u : users) {
                    if (u.getId().equals(curId)) {
                        currentUser = u;
                        tvFullName.setText(u.getNomCompl());
                        tvUsername.setText("@" + u.getNomUsuario());
                        tvEmail.setText(u.getEmail());

                        String initials = u.getNomCompl() != null && u.getNomCompl().length() >= 2
                                ? u.getNomCompl().substring(0, 2).toUpperCase()
                                : "U";
                        tvInitials.setText(initials);
                        break;
                    }
                }
                updateStats();
            }
        });

        repository.getAllDebtsLive().observe(this, debts -> {
            if (debts != null) {
                allDebts = debts;
                updateStats();
            }
        });

        repository.getAllPaymentsLive().observe(this, payments -> {
            if (payments != null) {
                allPayments = payments;
                updateStats();
            }
        });

        repository.getAllGroupsLive().observe(this, groups -> {
            if (groups != null) {
                allGroups = groups;
                updateStats();
            }
        });
    }

    private void updateStats() {
        if (currentUser == null) return;
        String uid = currentUser.getId();

        int debtsCount = 0;
        for (Deuda d : allDebts) {
            if (uid.equals(d.getAssignedUserId()) || uid.equals(d.getCreatedBy())) {
                debtsCount++;
            }
        }

        double paidTotal = 0.0;
        for (Pago p : allPayments) {
            if (uid.equals(p.getUsuarioId())) {
                paidTotal += p.getMonto();
            }
        }

        int groupsCount = 0;
        for (Grupofamiliar g : allGroups) {
            if (g.getMiembrosIds() != null && g.getMiembrosIds().contains(uid)) {
                groupsCount++;
            }
        }

        tvStatDebts.setText(String.valueOf(debtsCount));
        tvStatPaid.setText(FreyrRepository.formatCurrency(paidTotal));
        tvStatGroups.setText(String.valueOf(groupsCount));
    }
}
