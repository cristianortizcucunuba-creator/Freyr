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

import com.freyr.app.FreyrApp;
import com.freyr.app.R;
import com.freyr.app.data.datos.Deuda;
import com.freyr.app.data.datos.Grupofamiliar;
import com.freyr.app.data.datos.Pago;
import com.freyr.app.data.datos.Usuario;
import com.freyr.app.data.repositorio.FreyrRepositorio;
import com.freyr.app.Clases.listas.listaDeDeudas;
import com.freyr.app.Clases.deuda.AñadirDeuda;
import com.freyr.app.Clases.deuda.RegistrarAbonosDeuda;
import com.freyr.app.Clases.deuda.DetallesDeuda;

public class ConfigurarCuentaFamiliar extends AppCompatActivity {

    private ImageButton btnVolver;
    private TextView textoTituloGrupo;
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
    private FreyrRepositorio repositorio;
    private String idGrupo;
    private Grupofamiliar grupoActual;

    // Mantiene los datos de la pantalla en arreglos.
    private Usuario[] todosLosUsuarios = new Usuario[0];
    private Deuda[] deudasDelGrupo = new Deuda[0];
    private Pago[] todosLosPagos = new Pago[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_group_detail);

        repositorio = ((FreyrApp) getApplication()).getRepository();
        idGrupo = getIntent().getStringExtra("grupo_id");
        repositorio = ((FreyrApp) getApplication()).getRepository();
        idGrupo = getIntent().getStringExtra("group_id");

        if (idGrupo == null) {
            finish();
            return;
        }

        initViews();
        setupListeners();
        configurarListaDeudas();
        observarDatos();
    }

    private void initViews() {
        btnVolver = findViewById(R.id.btn_volver);
        textoTituloGrupo = findViewById(R.id.texto_titulo_grupo);
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
                showResultadoInvitacion("Por favor ingresa un usuario o correo.", false);
                return;
            }

            btnEnviarInvitacion.setEnabled(false);
            repositorio.inviteUser(
                    idGrupo,
                    identifier,

                    new FreyrRepositorio.Callback<Void>() {
                        @Override
                        public void onSuccess(Void resultado) {
                            runOnUiThread(() -> {
                                btnEnviarInvitacion.setEnabled(true);
                                etIdentificadorInvitacion.setText("");
                                showResultadoInvitacion(
                                        "¡Invitación enviada exitosamente!",
                                        true
                                );
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                btnEnviarInvitacion.setEnabled(true);
                                showResultadoInvitacion(error, false);
                            });
                        }
                    }
            );
        });
    }

    private void showResultadoInvitacion(String mensaje, boolean isSuccess) {
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
                        Intent intencion = new Intent(
                                ConfigurarCuentaFamiliar.this,
                                DetallesDeuda.class
                        );
                        intencion.putExtra("debt_id", debt.getId());
                        startActivity(intencion);
                    }

                    @Override
                    public void tocPagar(Deuda debt) {
                        Intent intencion = new Intent(
                                ConfigurarCuentaFamiliar.this,
                                RegistrarAbonosDeuda.class
                        );
                        intencion.putExtra("deuda_id", debt.getId());
                        startActivity(intencion);
                    }
                }
        );

        rvDeudasGrupo.setLayoutManager(new LinearLayoutManager(this));
        rvDeudasGrupo.setAdapter(adaptadorDeudas);
    }


    private void observarDatos() {
        repositorio.getAllUsersLive().observe(this, users -> {
            if (users != null) {
                todosLosUsuarios = users.toArray(new Usuario[0]);
                actualizarListaMiembros();
            }
        });

        repositorio.getAllGroupsLive().observe(this, groups -> {
            if (groups != null) {
                for (Grupofamiliar grupo : groups) {
                    if (grupo.getId().equals(idGrupo)) {
                        grupoActual = grupo;
                        textoTituloGrupo.setText(grupo.getNombre());

                        // El adapter recibe un arreglo de grupos.
                        adaptadorDeudas.setGrupos(new Grupofamiliar[]{grupo});
                        actualizarListaMiembros();
                        break;
                    }
                }
            }
        });

        repositorio.getAllDebtsLive().observe(this, debts -> {
            if (debts != null) {
                Deuda[] deudasCoincidentes = new Deuda[debts.size()];
                int cantidad = 0;

                for (Deuda debt : debts) {
                    if (idGrupo.equals(debt.getGrupoFamiliarId())) {
                        deudasCoincidentes[cantidad] = debt;
                        cantidad++;
                    }
                }

                // Recorta el arreglo temporal al número de deudas del grupo.
                deudasDelGrupo = new Deuda[cantidad];
                System.arraycopy(deudasCoincidentes, 0, deudasDelGrupo, 0, cantidad);

                adaptadorDeudas.setDeudas(deudasDelGrupo);
                txSinDeudasGrupo.setVisibility(
                        deudasDelGrupo.length == 0 ? View.VISIBLE : View.GONE
                );
                recalcularTotales();
            }
        });

        repositorio.getAllPaymentsLive().observe(this, payments -> {
            if (payments != null) {
                todosLosPagos = payments.toArray(new Pago[0]);
                adaptadorDeudas.setPagos(todosLosPagos);
                recalcularTotales();
            }
        });
    }

    // Actualiza el texto con los miembros del grupo.
    private void actualizarListaMiembros() {
        if (grupoActual == null || grupoActual.getMiembrosIds() == null) {
            return;
        }

        String[] nombresParaMostrar =
                new String[grupoActual.getMiembrosIds().size()];

        int indice = 0;
        for (String idUsuario : grupoActual.getMiembrosIds()) {
            boolean Creador = idUsuario.equals(grupoActual.getCreadorId());
            String nombre = "Usuario (" + idUsuario + ")";

            for (Usuario user : todosLosUsuarios) {
                if (user.getId().equals(idUsuario)) {
                    nombre = user.getNomCompl()
                            + (Creador ? " (Creador)" : "");
                    break;
                }
            }

            nombresParaMostrar[indice] = nombre;
            indice++;
        }

        txListaIntegrantes.setText(String.join(", ", nombresParaMostrar));
    }

    // Calcula los totales de las deudas y los pagos del grupo.
    private void recalcularTotales() {
        double total = 0.0;
        double totalPagado = 0.0;

        for (Deuda debt : deudasDelGrupo) {
            total += debt.getMonto();

            for (Pago payment : todosLosPagos) {
                if (payment.getDeudaId().equals(debt.getId())) {
                    totalPagado += payment.getMonto();
                }
            }
        }

        double saldoPendiente = Math.max(0.0, total - totalPagado);

        txMontoTotalGrupo.setText(FreyrRepositorio.formatCurrency(total));
        txMontoAbonadoGrupo.setText(FreyrRepositorio.formatCurrency(totalPagado));
        txMontoPendienteGrupo.setText(
                FreyrRepositorio.formatCurrency(saldoPendiente)
        );
    }
}