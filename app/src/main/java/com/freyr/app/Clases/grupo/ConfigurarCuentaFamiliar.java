
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

        // Obtener el identificador del grupo recibido.
        idGrupo = getIntent().getStringExtra("grupo_id");

        // Verificar que se haya recibido un identificador.
        if (idGrupo == null) {
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

        // ------------------------------------------
        // 4.1. Componentes principales
        // ------------------------------------------

        btnVolver = findViewById(R.id.btn_atras);
        textoTituloGrupo = findViewById(R.id.texto_titulo_grupo);

        btnAgregarDeudaGrupo = findViewById(
                R.id.btn_agregar_deuda_grupo
        );


        // ------------------------------------------
        // 4.2. Información financiera del grupo
        // ------------------------------------------

        txMontoTotalGrupo = findViewById(
                R.id.texto_total_deuda_grupo
        );

        txMontoAbonadoGrupo = findViewById(
                R.id.texto_total_pagado_grupo
        );

        txMontoPendienteGrupo = findViewById(
                R.id.texto_saldo_pendiente_grupo
        );


        // ------------------------------------------
        // 4.3. Integrantes e invitaciones
        // ------------------------------------------

        txListaIntegrantes = findViewById(
                R.id.texto_lista_miembros
        );

        etIdentificadorInvitacion = findViewById(
                R.id.identificador_invitacion
        );

        btnEnviarInvitacion = findViewById(
                R.id.enviar_invitacion
        );

        txRespuestaInvitacion = findViewById(
                R.id.texto_resultado_invitacion
        );


        // ------------------------------------------
        // 4.4. Lista de deudas
        // ------------------------------------------

        txSinDeudasGrupo = findViewById(
                R.id.texto_sin_deudas_grupo
        );

        rvDeudasGrupo = findViewById(
                R.id.lista_visual_deudas_grupo
        );
    }


    // ==========================================
    // 5. CONFIGURACIÓN DE EVENTOS
    // ==========================================

    private void setupListeners() {

        // ------------------------------------------
        // 5.1. Regresar a la pantalla anterior
        // ------------------------------------------

        btnVolver.setOnClickListener(v -> finish());


        // ------------------------------------------
        // 5.2. Agregar una deuda al grupo
        // ------------------------------------------

        btnAgregarDeudaGrupo.setOnClickListener(v -> {

            Intent intent = new Intent(
                    this,
                    AñadirDeuda.class
            );

            intent.putExtra("group_id", idGrupo);
            startActivity(intent);
        });


        // ------------------------------------------
        // 5.3. Enviar invitación a un usuario
        // ------------------------------------------

        btnEnviarInvitacion.setOnClickListener(v -> {

            String identifier = etIdentificadorInvitacion.getText()
                    .toString()
                    .trim();

            // Validar que se haya ingresado un usuario o correo.
            if (identifier.isEmpty()) {

                showResultadoInvitacion(
                        "Por favor ingresa un usuario o correo.",
                        false
                );

                return;
            }

            // Deshabilitar el botón mientras se procesa la invitación.
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

                                showResultadoInvitacion(
                                        "¡Invitación enviada exitosamente!",
                                        true
                                );
                            });
                        }

                        @Override
                        public void alOcurrirError(String error) {

                            runOnUiThread(() -> {

                                btnEnviarInvitacion.setEnabled(true);

                                showResultadoInvitacion(
                                        error,
                                        false
                                );
                            });
                        }
                    }
            );
        });
    }


    // ==========================================
    // 6. MOSTRAR RESULTADO DE LA INVITACIÓN
    // ==========================================

    private void showResultadoInvitacion(
            String mensaje,
            boolean isSuccess
    ) {

        txRespuestaInvitacion.setText(mensaje);

        // Cambiar el color según el resultado.
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


    // ==========================================
    // 7. CONFIGURACIÓN DE LA LISTA DE DEUDAS
    // ==========================================

    private void configurarListaDeudas() {

        // Crear el adaptador y configurar las acciones de cada deuda.
        adaptadorDeudas = new listaDeDeudas(
                this,

                new listaDeDeudas.MonitorDeudas() {

                    // ------------------------------------------
                    // 7.1. Abrir detalles de una deuda
                    // ------------------------------------------

                    @Override
                    public void tocDeuda(Deuda debt) {

                        Intent intencion = new Intent(
                                ConfigurarCuentaFamiliar.this,
                                DetallesDeuda.class
                        );

                        intencion.putExtra("debt_id", debt.getId());
                        startActivity(intencion);
                    }


                    // ------------------------------------------
                    // 7.2. Registrar un abono
                    // ------------------------------------------

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

        // Configurar el RecyclerView.
        rvDeudasGrupo.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rvDeudasGrupo.setAdapter(adaptadorDeudas);
    }


    // ==========================================
    // 8. OBSERVACIÓN DE DATOS
    // ==========================================

    private void observarDatos() {

        // ------------------------------------------
        // 8.1. Observar todos los usuarios
        // ------------------------------------------

        repositorio.observarTodosLosUsuarios().observe(this, users -> {

            if (users != null) {

                // Convertir la lista recibida en un arreglo.
                todosLosUsuarios = users.toArray(new Usuario[0]);

                actualizarListaMiembros();
            }
        });


        // ------------------------------------------
        // 8.2. Observar todos los grupos familiares
        // ------------------------------------------

        repositorio.observarTodosLosGrupos().observe(this, groups -> {

            if (groups != null) {

                for (Grupofamiliar grupo : groups) {

                    if (grupo.getId().equals(idGrupo)) {

                        grupoActual = grupo;

                        textoTituloGrupo.setText(
                                grupo.getNombre()
                        );

                        // Enviar el grupo al adaptador mediante un arreglo.
                        adaptadorDeudas.setGrupos(
                                new Grupofamiliar[]{grupo}
                        );

                        actualizarListaMiembros();

                        break;
                    }
                }
            }
        });


        // ------------------------------------------
        // 8.3. Observar todas las deudas
        // ------------------------------------------

        repositorio.observarTodasLasDeudas().observe(this, debts -> {

            if (debts != null) {

                // Arreglo temporal con capacidad para todas las deudas.
                Deuda[] deudasCoincidentes = new Deuda[debts.size()];

                int cantidad = 0;

                // Seleccionar únicamente las deudas del grupo actual.
                for (Deuda debt : debts) {

                    if (idGrupo.equals(debt.getGrupoFamiliarId())) {

                        deudasCoincidentes[cantidad] = debt;
                        cantidad++;
                    }
                }

                // Crear un arreglo con el tamaño exacto.
                deudasDelGrupo = new Deuda[cantidad];

                System.arraycopy(
                        deudasCoincidentes,
                        0,
                        deudasDelGrupo,
                        0,
                        cantidad
                );

                adaptadorDeudas.setDeudas(deudasDelGrupo);

                // Mostrar u ocultar el mensaje si no hay deudas.
                txSinDeudasGrupo.setVisibility(
                        deudasDelGrupo.length == 0
                                ? View.VISIBLE
                                : View.GONE
                );

                recalcularTotales();
            }
        });


        // ------------------------------------------
        // 8.4. Observar todos los pagos
        // ------------------------------------------

        repositorio.observarTodosLosPagos().observe(this, payments -> {

            if (payments != null) {

                // Convertir la lista de pagos en un arreglo.
                todosLosPagos = payments.toArray(new Pago[0]);

                adaptadorDeudas.setPagos(todosLosPagos);

                recalcularTotales();
            }
        });
    }


    // ==========================================
    // 9. ACTUALIZAR LISTA DE INTEGRANTES
    // ==========================================

    // Muestra los nombres de los integrantes del grupo.
    private void actualizarListaMiembros() {

        if (grupoActual == null || grupoActual.getMiembrosIds() == null) {
            return;
        }

        // Crear un arreglo para almacenar los nombres.
        String[] nombresParaMostrar =
                new String[grupoActual.getMiembrosIds().size()];

        int indice = 0;

        // Recorrer los identificadores de los integrantes.
        for (String idUsuario : grupoActual.getMiembrosIds()) {

            boolean Creador = idUsuario.equals(
                    grupoActual.getCreadorId()
            );

            String nombre = "Usuario (" + idUsuario + ")";

            // Buscar el usuario correspondiente.
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

        // Unir los nombres en un solo texto separado por comas.
        txListaIntegrantes.setText(
                String.join(", ", nombresParaMostrar)
        );
    }


    // ==========================================
    // 10. RECALCULAR TOTALES DEL GRUPO
    // ==========================================

    // Calcula el total de las deudas, los pagos y el saldo pendiente.
    private void recalcularTotales() {

        double total = 0.0;
        double totalPagado = 0.0;

        // Recorrer las deudas pertenecientes al grupo.
        for (Deuda debt : deudasDelGrupo) {

            total += debt.getMonto();

            // Sumar los pagos correspondientes a cada deuda.
            for (Pago payment : todosLosPagos) {

                if (payment.getDeudaId().equals(debt.getId())) {
                    totalPagado += payment.getMonto();
                }
            }
        }

        // Calcular el saldo que todavía está pendiente.
        double saldoPendiente = Math.max(
                0.0,
                total - totalPagado
        );


        // ------------------------------------------
        // 10.1. Actualizar los valores en pantalla
        // ------------------------------------------

        txMontoTotalGrupo.setText(
                FreyrRepositorio.formatearMoneda(total)
        );

        txMontoAbonadoGrupo.setText(
                FreyrRepositorio.formatearMoneda(totalPagado)
        );

        txMontoPendienteGrupo.setText(
                FreyrRepositorio.formatearMoneda(saldoPendiente)
        );
    }
}
