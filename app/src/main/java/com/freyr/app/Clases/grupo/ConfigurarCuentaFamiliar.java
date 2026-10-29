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
    private Usuario[] todosUsuarios = new Usuario[0];
    private Deuda[] deudasGrupo = new Deuda[0];
    private Pago[] todosPagos = new Pago[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_group_detail);

        repositorio = ((FreyrApplication) getApplication()).getRepository();
        idGrupo = getIntent().getStringExtra("grupo_id");

        if (idGrupo == null) {
            finish();
            return;
        }

        inicializarVistas();
        configurarAcciones();
        configurarListaDeudas();
        observarDatos();
    }

    private void inicializarVistas() {
        btnVolver = findViewById(R.id.btn_volver);
        txTituloGrupo = findViewById(R.id.texto_titulo_grupo);
        btnAgregarDeudaGrupo = findViewById(R.id.btn_agregar_deuda_grupo);
        txMontoTotalGrupo = findViewById(R.id.texto_total_deuda_grupo);
        txMontoAbonadoGrupo = findViewById(R.id.texto_total_pagado_grupo);
        txMontoPendienteGrupo = findViewById(R.id.texto_saldo_pendiente_grupo);
        txListaIntegrantes = findViewById(R.id.texto_lista_miembros);
        etIdentificadorInvitacion = findViewById(R.id.identificador_invitacion);
        btnEnviarInvitacion = findViewById(R.id.enviar_invitacion);
        txRespuestaInvitacion = findViewById(R.id.texto_resultado_invitacion);
        txSinDeudasGrupo = findViewById(R.id.texto_sin_deudas_grupo);
        rvDeudasGrupo = findViewById(R.id.lista_visual_deudas_grupo);
    }

    private void configurarAcciones() {
        btnVolver.setOnClickListener(v -> finish());

        btnAgregarDeudaGrupo.setOnClickListener(v -> {
            Intent intencion = new Intent(this, AñadirDeuda.class);
            intencion.putExtra("grupo_id", idGrupo);
            startActivity(intencion);
        });

        btnEnviarInvitacion.setOnClickListener(v -> {
            String identificador = etIdentificadorInvitacion.getText().toString().trim();

            if (identificador.isEmpty()) {
                mostrarResultadoInvitacion("Por favor ingresa un usuario o correo.", false);
                return;
            }

            btnEnviarInvitacion.setEnabled(false);
            repositorio.inviteUser(
                    idGrupo,
                    identificador,
                    new FreyrRepository.Callback<Void>() {
                        @Override
                        public void onSuccess(Void resultado) {
                            runOnUiThread(() -> {
                                btnEnviarInvitacion.setEnabled(true);
                                etIdentificadorInvitacion.setText("");
                                mostrarResultadoInvitacion(
                                        "¡Invitación enviada exitosamente!",
                                        true
                                );
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                btnEnviarInvitacion.setEnabled(true);
                                mostrarResultadoInvitacion(error, false);
                            });
                        }
                    }
            );
        });
    }

    private void mostrarResultadoInvitacion(String mensaje, boolean isSuccess) {
        txRespuestaInvitacion.setText(mensaje);
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

    private void configurarListaDeudas() {
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

    private void observarDatos() {
        repositorio.getAllUsersLive().observe(this, users -> {
            if (users != null) {
                todosUsuarios = users.toArray(new Usuario[0]);
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
                deudasGrupo = new Deuda[count];
                System.arraycopy(matchingDebts, 0, deudasGrupo, 0, count);

                adaptadorDeudas.setDeudas(deudasGrupo);
                txSinDeudasGrupo.setVisibility(
                        deudasGrupo.length == 0 ? View.VISIBLE : View.GONE
                );
                recalculateTotals();
            }
        });

        repositorio.getAllPaymentsLive().observe(this, payments -> {
            if (payments != null) {
                todosPagos = payments.toArray(new Pago[0]);
                adaptadorDeudas.setPagos(todosPagos);
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

            for (Usuario user : todosUsuarios) {
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

        for (Deuda debt : deudasGrupo) {
            total += debt.getAmount();

            for (Pago payment : todosPagos) {
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