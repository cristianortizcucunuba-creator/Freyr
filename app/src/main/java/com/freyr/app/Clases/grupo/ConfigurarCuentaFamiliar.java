package com.freyr.app.Clases.grupo;

// ==========================================
// 1. IMPORTACIONES
// ==========================================

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
import com.freyr.app.Clases.deuda.AñadirDeuda;
import com.freyr.app.Clases.deuda.DetallesDeuda;
import com.freyr.app.Clases.deuda.RegistrarAbonosDeuda;
import com.freyr.app.Clases.listas.listaDeDeudas;
import com.freyr.app.data.datos.Deuda;
import com.freyr.app.data.datos.Grupofamiliar;
import com.freyr.app.data.datos.Pago;
import com.freyr.app.data.datos.Usuario;
import com.freyr.app.data.repositorio.FreyrRepositorio;


// ==========================================
// 2. CLASE PRINCIPAL
// ==========================================

public class ConfigurarCuentaFamiliar extends AppCompatActivity {

    // ------------------------------------------
    // 2.1. Componentes de la interfaz
    // ------------------------------------------

    private ImageButton btnVolver;

    private TextView textoTituloGrupo;
    private TextView txMontoTotalGrupo;
    private TextView txMontoAbonadoGrupo;
    private TextView txMontoPendienteGrupo;
    private TextView txListaIntegrantes;
    private TextView txRespuestaInvitacion;
    private TextView txSinDeudasGrupo;

    private Button btnAgregarDeudaGrupo;
    private Button btnEnviarInvitacion;

    private EditText etIdentificadorInvitacion;

    private RecyclerView rvDeudasGrupo;


    // ------------------------------------------
    // 2.2. Adaptador y repositorio
    // ------------------------------------------

    private listaDeDeudas adaptadorDeudas;
    private FreyrRepositorio repositorio;


    // ------------------------------------------
    // 2.3. Datos del grupo familiar
    // ------------------------------------------

    private String idGrupo;
    private Grupofamiliar grupoActual;

    // Arreglos que almacenan los datos recibidos.
    private Usuario[] todosLosUsuarios = new Usuario[0];
    private Deuda[] deudasDelGrupo = new Deuda[0];
    private Pago[] todosLosPagos = new Pago[0];


    // ==========================================
    // 3. CICLO DE VIDA DE LA ACTIVIDAD
    // ==========================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_group_detail);

        // Obtener el repositorio de la aplicación.
        repositorio = ((FreyrApp) getApplication()).getRepositorio();

        // Compatible con ambas claves: grupo_id o group_id
        idGrupo = getIntent().getStringExtra("grupo_id");
        if (idGrupo == null || idGrupo.trim().isEmpty()) {
            idGrupo = getIntent().getStringExtra("group_id");
        }

        // Si no se recibió un identificador válido, finalizar
        if (idGrupo == null || idGrupo.trim().isEmpty()) {
            finish();
            return;
        }

        // Inicializar y configurar los componentes.
        initViews();
        setupListeners();
        configurarListaDeudas();
        observarDatos();
    }


    // ==========================================
    // 4. INICIALIZACIÓN DE COMPONENTES
    // ==========================================

    private void initViews() {

        btnVolver = findViewById(R.id.btn_atras);
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


    // ==========================================
    // 5. CONFIGURACIÓN DE EVENTOS
    // ==========================================

    private void setupListeners() {

        btnVolver.setOnClickListener(v -> finish());

        btnAgregarDeudaGrupo.setOnClickListener(v -> {
            Intent intent = new Intent(this, AñadirDeuda.class);
            intent.putExtra("group_id", idGrupo);
            intent.putExtra("grupo_id", idGrupo);
            startActivity(intent);
        });

        btnEnviarInvitacion.setOnClickListener(v -> {

            String identifier = etIdentificadorInvitacion.getText()
                    .toString()
                    .trim();

            if (identifier.isEmpty()) {
                showResultadoInvitacion("Por favor ingresa un usuario o correo.", false);
                return;
            }

            btnEnviarInvitacion.setEnabled(false);

            repositorio.invitarUsuario(
                    idGrupo,
                    identifier,
                    new FreyrRepositorio.Callback<Void>() {
                        @Override
                        public void alTenerExito(Void resultado) {
                            runOnUiThread(() -> {
                                btnEnviarInvitacion.setEnabled(true);
                                etIdentificadorInvitacion.setText("");
                                showResultadoInvitacion("¡Invitación enviada exitosamente!", true);
                            });
                        }

                        @Override
                        public void alOcurrirError(String error) {
                            runOnUiThread(() -> {
                                btnEnviarInvitacion.setEnabled(true);
                                showResultadoInvitacion(error, false);
                            });
                        }
                    }
            );
        });
    }


    // ==========================================
    // 6. MOSTRAR RESULTADO DE LA INVITACIÓN
    // ==========================================

    private void showResultadoInvitacion(String mensaje, boolean isSuccess) {
        txRespuestaInvitacion.setText(mensaje);
        txRespuestaInvitacion.setTextColor(
                ContextCompat.getColor(
                        this,
                        isSuccess ? R.color.freyr_status_paid : R.color.freyr_primary
                )
        );
        txRespuestaInvitacion.setVisibility(View.VISIBLE);
    }


    // ==========================================
    // 7. CONFIGURACIÓN DE LA LISTA DE DEUDAS
    // ==========================================

    private void configurarListaDeudas() {

        adaptadorDeudas = new listaDeDeudas(
                this,
                new listaDeDeudas.MonitorDeudas() {
                    @Override
                    public void tocDeuda(Deuda debt) {
                        Intent intencion = new Intent(ConfigurarCuentaFamiliar.this, DetallesDeuda.class);
                        intencion.putExtra("debt_id", debt.getId());
                        intencion.putExtra("deuda_id", debt.getId());
                        startActivity(intencion);
                    }

                    @Override
                    public void tocPagar(Deuda debt) {
                        Intent intencion = new Intent(ConfigurarCuentaFamiliar.this, RegistrarAbonosDeuda.class);
                        intencion.putExtra("debt_id", debt.getId());
                        intencion.putExtra("deuda_id", debt.getId());
                        startActivity(intencion);
                    }
                }
        );

        rvDeudasGrupo.setLayoutManager(new LinearLayoutManager(this));
        rvDeudasGrupo.setAdapter(adaptadorDeudas);
    }


    // ==========================================
    // 8. OBSERVACIÓN DE DATOS
    // ==========================================

    private void observarDatos() {

        repositorio.observarTodosLosUsuarios().observe(this, users -> {
            if (users != null) {
                todosLosUsuarios = users.toArray(new Usuario[0]);
                actualizarListaMiembros();
            }
        });

        repositorio.observarTodosLosGrupos().observe(this, groups -> {
            if (groups != null) {
                for (Grupofamiliar grupo : groups) {
                    if (grupo.getId().equals(idGrupo)) {
                        grupoActual = grupo;
                        textoTituloGrupo.setText(grupo.getNombre());
                        adaptadorDeudas.setGrupos(new Grupofamiliar[]{grupo});
                        actualizarListaMiembros();
                        break;
                    }
                }
            }
        });

        repositorio.observarTodasLasDeudas().observe(this, debts -> {
            if (debts != null) {
                Deuda[] deudasCoincidentes = new Deuda[debts.size()];
                int cantidad = 0;

                for (Deuda debt : debts) {
                    if (idGrupo.equals(debt.getGrupoFamiliarId())) {
                        deudasCoincidentes[cantidad] = debt;
                        cantidad++;
                    }
                }

                deudasDelGrupo = new Deuda[cantidad];
                System.arraycopy(deudasCoincidentes, 0, deudasDelGrupo, 0, cantidad);
                adaptadorDeudas.setDeudas(deudasDelGrupo);

                txSinDeudasGrupo.setVisibility(deudasDelGrupo.length == 0 ? View.VISIBLE : View.GONE);
                recalcularTotales();
            }
        });

        repositorio.observarTodosLosPagos().observe(this, payments -> {
            if (payments != null) {
                todosLosPagos = payments.toArray(new Pago[0]);
                adaptadorDeudas.setPagos(todosLosPagos);
                recalcularTotales();
            }
        });
    }


    // ==========================================
    // 9. ACTUALIZAR LISTA DE INTEGRANTES
    // ==========================================

    private void actualizarListaMiembros() {
        if (grupoActual == null || grupoActual.getMiembrosIds() == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        int size = grupoActual.getMiembrosIds().size();

        for (int i = 0; i < size; i++) {
            String idUsuario = grupoActual.getMiembrosIds().get(i);
            boolean creador = idUsuario.equals(grupoActual.getCreadorId());
            String nombre = "Usuario (" + idUsuario + ")";

            for (Usuario user : todosLosUsuarios) {
                if (user.getId().equals(idUsuario)) {
                    nombre = user.getNomCompl() + (creador ? " (Creador)" : "");
                    break;
                }
            }

            sb.append(nombre);
            if (i < size - 1) {
                sb.append(", ");
            }
        }

        txListaIntegrantes.setText(sb.toString());
    }


    // ==========================================
    // 10. RECALCULAR TOTALES DEL GRUPO
    // ==========================================

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

        txMontoTotalGrupo.setText(FreyrRepositorio.formatearMoneda(total));
        txMontoAbonadoGrupo.setText(FreyrRepositorio.formatearMoneda(totalPagado));
        txMontoPendienteGrupo.setText(FreyrRepositorio.formatearMoneda(saldoPendiente));
    }
}