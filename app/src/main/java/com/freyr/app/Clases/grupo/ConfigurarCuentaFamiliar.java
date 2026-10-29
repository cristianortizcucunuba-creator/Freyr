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
    private TextView txMontoPendienteGrupo;
    private TextView txListaIntegrantes;
    private EditText etIdentificadorInvitacion;
    private Button btnEnviarInvitacion;
    private TextView txRespuestaInvitacion;
    private TextView txSinDeudasGrupo;
    private RecyclerView rvDeudasGrupo;


    private listaDeDeudas adaptadorDeudas;
    private FreyrRepository repositorio;
    private String idGrupo;
    private Grupofamiliar grupoActual;

    // Mantiene los datos de la pantalla en arreglos.
    private Usuario[] allUsers = new Usuario[0];
    private Deuda[] groupDebts = new Deuda[0];
    private Pago[] allPayments = new Pago[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_group_detail);

        repositorio = ((FreyrApplication) getApplication()).getRepository();
        idGrupo = getIntent().getStringExtra("group_id");

        if (idGrupo == null) {
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
        txMontoPendienteGrupo = findViewById(R.id.tv_group_remaining_amt);
        txListaIntegrantes = findViewById(R.id.tv_members_list);
        etIdentificadorInvitacion = findViewById(R.id.et_invite_identifier);
        btnEnviarInvitacion = findViewById(R.id.btn_send_invite);
        txRespuestaInvitacion = findViewById(R.id.tv_invite_feedback);
        txSinDeudasGrupo = findViewById(R.id.tv_no_group_debts);
        rvDeudasGrupo = findViewById(R.id.rv_group_debts);
    }

    private void setupListeners() {
        btnVolver.setOnClickListener(v -> finish());

        btnAgregarDeudaGrupo.setOnClickListener(v -> {
            Intent intent = new Intent(this, AñadirDeuda.class);
            intent.putExtra("group_id", idGrupo);
            startActivity(intent);
        });

        btnEnviarInvitacion.setOnClickListener(v -> {
            String identifier = etIdentificadorInvitacion.getText().toString().trim();

            if (identifier.isEmpty()) {
                showInviteFeedback("Por favor ingresa un usuario o correo.", false);
                return;
            }

            btnEnviarInvitacion.setEnabled(false);
            repositorio.inviteUser(
                    idGrupo,
                    identifier,
                    new FreyrRepository.Callback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            runOnUiThread(() -> {
                                btnEnviarInvitacion.setEnabled(true);
                                etIdentificadorInvitacion.setText("");
                                showInviteFeedback(
                                        "¡Invitación enviada exitosamente!",
                                        true
                                );
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                btnEnviarInvitacion.setEnabled(true);
                                showInviteFeedback(error, false);
                            });
                        }
                    }
            );
        });
    }

    private void showInviteFeedback(String message, boolean isSuccess) {
        txRespuestaInvitacion.setText(message);
        txRespuestaInvitacion.setTextColor(
                ContextCompat.getColor(
                        this,
                        isSuccess
                                ? R.color.freyr_status_paid
                                : R.color.freyr_primary
                )
        );
        txRespuestaInvitacion.setVisibility(View.VISIBLE);
    }

    private void setupRecyclerView() {
        adaptadorDeudas = new listaDeDeudas(
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

        rvDeudasGrupo.setLayoutManager(new LinearLayoutManager(this));
        rvDeudasGrupo.setAdapter(adaptadorDeudas);
    }

    private void observeData() {
        repositorio.getAllUsersLive().observe(this, users -> {
            if (users != null) {
                allUsers = users.toArray(new Usuario[0]);
                updateMembersDisplay();
            }
        });

        repositorio.getAllGroupsLive().observe(this, groups -> {
            if (groups != null) {
                for (Grupofamiliar group : groups) {
                    if (group.getId().equals(idGrupo)) {
                        grupoActual = group;
                        txTituloGrupo.setText(group.getNombre());

                        // El adapter recibe un arreglo de grupos.
                        adaptadorDeudas.setGrupos(new Grupofamiliar[]{group});
                        updateMembersDisplay();
                        break;
                    }
                }
            }
        });

        repositorio.getAllDebtsLive().observe(this, debts -> {
            if (debts != null) {
                Deuda[] matchingDebts = new Deuda[debts.size()];
                int count = 0;

                for (Deuda debt : debts) {
                    if (idGrupo.equals(debt.getGrupoFamiliarId())) {
                        matchingDebts[count] = debt;
                        count++;
                    }
                }

                // Recorta el arreglo temporal al número de deudas del grupo.
                groupDebts = new Deuda[count];
                System.arraycopy(matchingDebts, 0, groupDebts, 0, count);

                adaptadorDeudas.setDeudas(groupDebts);
                txSinDeudasGrupo.setVisibility(
                        groupDebts.length == 0 ? View.VISIBLE : View.GONE
                );
                recalculateTotals();
            }
        });

        repositorio.getAllPaymentsLive().observe(this, payments -> {
            if (payments != null) {
                allPayments = payments.toArray(new Pago[0]);
                adaptadorDeudas.setPagos(allPayments);
                recalculateTotals();
            }
        });
    }

    // Actualiza el texto con los miembros del grupo.
    private void updateMembersDisplay() {
        if (grupoActual == null || grupoActual.getMiembrosIds() == null) {
            return;
        }

        String[] displayNames =
                new String[grupoActual.getMiembrosIds().size()];

        int index = 0;
        for (String userId : grupoActual.getMiembrosIds()) {
            boolean isCreator = userId.equals(grupoActual.getCreadorId());
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

        txListaIntegrantes.setText(String.join(", ", displayNames));
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
        txMontoPendienteGrupo.setText(
                FreyrRepository.formatCurrency(remaining)
        );
    }
}