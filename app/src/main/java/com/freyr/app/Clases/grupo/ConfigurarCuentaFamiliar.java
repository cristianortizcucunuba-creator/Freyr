package com.freyr.app.Clases.grupo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.freyr.app.FreyrApplication;
import com.freyr.app.R;
import com.freyr.app.data.model.Deuda;
import com.freyr.app.data.model.Grupofamiliar;
import com.freyr.app.data.model.Pago;
import com.freyr.app.data.model.Usuario;
import com.freyr.app.data.repository.FreyrRepository;
import com.freyr.app.Clases.listas.listaDeDeudas;
import com.freyr.app.Clases.deuda.AñadirDeuda;
import com.freyr.app.Clases.deuda.RegistrarAbonosDeuda;
import com.freyr.app.Clases.deuda.DetallesDeuda;

public class ConfigurarCuentaFamiliar extends AppCompatActivity {

    private ImageButton btnVolver;
    private TextView txTituloGrupo;
    private Button btnAgregarDeudaGrupo;
    private TextView txMontoTotalGrupo;
    private TextView txMontoAbonadoGrupo;
    private TextView tvGroupRemainingAmt;
    private TextView tvMembersList;
    private EditText etInviteIdentifier;
    private Button btnSendInvite;
    private TextView tvInviteFeedback;
    private TextView tvNoGroupDebts;
    private RecyclerView rvGroupDebts;

    private listaDeDeudas debtAdapter;
    private FreyrRepository repository;
    private String groupId;
    private Grupofamiliar currentGroup;

    // Mantiene los datos de la pantalla en arreglos.
    private Usuario[] allUsers = new Usuario[0];
    private Deuda[] groupDebts = new Deuda[0];
    private Pago[] allPayments = new Pago[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_group_detail);

        repository = ((FreyrApplication) getApplication()).getRepository();
        groupId = getIntent().getStringExtra("group_id");

        if (groupId == null) {
            finish();
            return;
        }

        initViews();
        setupListeners();
        setupRecyclerView();
        observeData();
    }

    private void initViews() {
        btnVolver = findViewById(R.id.btn_back);
        txTituloGrupo = findViewById(R.id.tv_group_title);
        btnAgregarDeudaGrupo = findViewById(R.id.btn_add_group_debt);
        txMontoTotalGrupo = findViewById(R.id.tv_group_total_amt);
        txMontoAbonadoGrupo = findViewById(R.id.tv_group_paid_amt);
        tvGroupRemainingAmt = findViewById(R.id.tv_group_remaining_amt);
        tvMembersList = findViewById(R.id.tv_members_list);
        etInviteIdentifier = findViewById(R.id.et_invite_identifier);
        btnSendInvite = findViewById(R.id.btn_send_invite);
        tvInviteFeedback = findViewById(R.id.tv_invite_feedback);
        tvNoGroupDebts = findViewById(R.id.tv_no_group_debts);
        rvGroupDebts = findViewById(R.id.rv_group_debts);
    }

    private void setupListeners() {
        btnVolver.setOnClickListener(v -> finish());

        btnAgregarDeudaGrupo.setOnClickListener(v -> {
            Intent intent = new Intent(this, AñadirDeuda.class);
            intent.putExtra("group_id", groupId);
            startActivity(intent);
        });

        btnSendInvite.setOnClickListener(v -> {
            String identifier = etInviteIdentifier.getText().toString().trim();

            if (identifier.isEmpty()) {
                showInviteFeedback("Por favor ingresa un usuario o correo.", false);
                return;
            }

            btnSendInvite.setEnabled(false);
            repository.inviteUser(
                    groupId,
                    identifier,
                    new FreyrRepository.Callback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            runOnUiThread(() -> {
                                btnSendInvite.setEnabled(true);
                                etInviteIdentifier.setText("");
                                showInviteFeedback(
                                        "¡Invitación enviada exitosamente!",
                                        true
                                );
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                btnSendInvite.setEnabled(true);
                                showInviteFeedback(error, false);
                            });
                        }
                    }
            );
        });
    }

    private void showInviteFeedback(String message, boolean isSuccess) {
        tvInviteFeedback.setText(message);
        tvInviteFeedback.setTextColor(
                ContextCompat.getColor(
                        this,
                        isSuccess
                                ? R.color.freyr_status_paid
                                : R.color.freyr_primary
                )
        );
        tvInviteFeedback.setVisibility(View.VISIBLE);
    }

    private void setupRecyclerView() {
        debtAdapter = new listaDeDeudas(
                this,
                new listaDeDeudas.MonitorDeudas() {
                    @Override
                    public void tocDeuda(Deuda debt) {
                        Intent intent = new Intent(
                                ConfigurarCuentaFamiliar.this,
                                DetallesDeuda.class
                        );
                        intent.putExtra("debt_id", debt.getId());
                        startActivity(intent);
                    }

                    @Override
                    public void tocPagar(Deuda debt) {
                        Intent intent = new Intent(
                                ConfigurarCuentaFamiliar.this,
                                RegistrarAbonosDeuda.class
                        );
                        intent.putExtra("debt_id", debt.getId());
                        startActivity(intent);
                    }
                }
        );

        rvGroupDebts.setLayoutManager(new LinearLayoutManager(this));
        rvGroupDebts.setAdapter(debtAdapter);
    }

    private void observeData() {
        repository.getAllUsersLive().observe(this, users -> {
            if (users != null) {
                allUsers = users.toArray(new Usuario[0]);
                updateMembersDisplay();
            }
        });

        repository.getAllGroupsLive().observe(this, groups -> {
            if (groups != null) {
                for (Grupofamiliar group : groups) {
                    if (group.getId().equals(groupId)) {
                        currentGroup = group;
                        txTituloGrupo.setText(group.getNombre());

                        // El adapter recibe un arreglo de grupos.
                        debtAdapter.setGrupos(new Grupofamiliar[]{group});
                        updateMembersDisplay();
                        break;
                    }
                }
            }
        });

        repository.getAllDebtsLive().observe(this, debts -> {
            if (debts != null) {
                Deuda[] matchingDebts = new Deuda[debts.size()];
                int count = 0;

                for (Deuda debt : debts) {
                    if (groupId.equals(debt.getGrupoFamiliarId())) {
                        matchingDebts[count] = debt;
                        count++;
                    }
                }

                // Recorta el arreglo temporal al número de deudas del grupo.
                groupDebts = new Deuda[count];
                System.arraycopy(matchingDebts, 0, groupDebts, 0, count);

                debtAdapter.setDeudas(groupDebts);
                tvNoGroupDebts.setVisibility(
                        groupDebts.length == 0 ? View.VISIBLE : View.GONE
                );
                recalculateTotals();
            }
        });

        repository.getAllPaymentsLive().observe(this, payments -> {
            if (payments != null) {
                allPayments = payments.toArray(new Pago[0]);
                debtAdapter.setPagos(allPayments);
                recalculateTotals();
            }
        });
    }

    // Actualiza el texto con los miembros del grupo.
    private void updateMembersDisplay() {
        if (currentGroup == null || currentGroup.getMiembrosIds() == null) {
            return;
        }

        String[] displayNames =
                new String[currentGroup.getMiembrosIds().size()];

        int index = 0;
        for (String userId : currentGroup.getMiembrosIds()) {
            boolean isCreator = userId.equals(currentGroup.getCreadorId());
            String name = "Usuario (" + userId + ")";

            for (Usuario user : allUsers) {
                if (user.getId().equals(userId)) {
                    name = user.getNomCompl()
                            + (isCreator ? " (Creador)" : "");
                    break;
                }
            }

            displayNames[index] = name;
            index++;
        }

        tvMembersList.setText(String.join(", ", displayNames));
    }

    // Calcula los totales de las deudas y los pagos del grupo.
    private void recalculateTotals() {
        double total = 0.0;
        double paid = 0.0;

        for (Deuda debt : groupDebts) {
            total += debt.getAmount();

            for (Pago payment : allPayments) {
                if (payment.getDeudaId().equals(debt.getId())) {
                    paid += payment.getMonto();
                }
            }
        }

        double remaining = Math.max(0.0, total - paid);

        txMontoTotalGrupo.setText(FreyrRepository.formatCurrency(total));
        txMontoAbonadoGrupo.setText(FreyrRepository.formatCurrency(paid));
        tvGroupRemainingAmt.setText(
                FreyrRepository.formatCurrency(remaining)
        );
    }
}