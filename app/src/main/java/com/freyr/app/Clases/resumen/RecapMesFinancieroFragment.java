package com.freyr.app.Clases.resumen;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.freyr.app.FreyrApplication;
import com.freyr.app.R;
import com.freyr.app.data.model.*;
import com.freyr.app.data.repository.FreyrRepository;
import com.freyr.app.Clases.ActividadPrincipal;
import com.freyr.app.Clases.listas.listaDeDeudas;
import com.freyr.app.Clases.deuda.AñadirDeuda;
import com.freyr.app.Clases.deuda.RegistrarAbonosDeuda;
import com.freyr.app.Clases.deuda.DetallesDeuda;
import com.freyr.app.Clases.grupo.CrearGrupoFamiliar;
import com.freyr.app.Clases.profile.ActividadEditarPerfil;

//Pantalla de inicio con el resumen financiero del usuario
public class RecapMesFinancieroFragment extends Fragment {

    //Vistas del layout
    private TextView txNombreBienvenida;
    private TextView txSubtituloBienvenida;
    private CardView cardEditarPerfil, cardAgregarDeuda, cardCrearGrupo, cardAceptarInvitacion;
    private TextView txTituloAccionInvitacion, txSubAccionInvitacion;
    private TextView txHistorialTotalDebido, txHistorialTotalPagado, txHistorialTotalPendiente;
    private TextView txMontoTotalPendiente, txCantidadDeudasPendientes, txCantidadDeudasPagadas;
    private TextView txTotalPagosPersonales, txTotalDeudasFamiliares;
    private RecyclerView rvDeudasActivas;
    private listaDeDeudas adaptadorDeudas;
    private FreyrRepository repositorio;

    //Guardan los datos usando arreglos.
    private Deuda[] todasLasDeudasallDebts = new Deuda[0];
    private Pago[] todosLosPagos = new Pago[0];
    private Grupofamiliar[] todosLosGrupos = new Grupofamiliar[0];
    private Invitacion[] todasLasInvitaciones = new Invitacion[0];
    private Usuario[] todosLosUsuarios = new Usuario[0];
    private Usuario usuarioActual;

    //Crea la vista y dejatodo configurado
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_dashboard, container, false);
        repositorio = ((FreyrApplication) requireActivity().getApplication()).getRepository();
        inicializarVistas(vista);
        configurarListeners();
        configurarRecyclerView();
        observarDatos();
        return vista;
    }

    //Enlaza cada vista con su id del layout
    private void inicializarVistas(View view) {
        txNombreBienvenida = view.findViewById(R.id.tx_saldo_pendiente_grupo);
        txSubtituloBienvenida = view.findViewById(R.id.tx_subtitulo_bienvenida);
        cardEditarPerfil = view.findViewById(R.id.card_accion_editar_perfil);
        cardAgregarDeuda = view.findViewById(R.id.card_accion_agregar_deuda);
        cardCrearGrupo = view.findViewById(R.id.card_accion_crear_grupo);
        cardAceptarInvitacion = view.findViewById(R.id.card_accion_aceptar_invitacion);
        txTituloAccionInvitacion = view.findViewById(R.id.tx_titulo_accion_invitacion);
        txSubAccionInvitacion = view.findViewById(R.id.tx_sub_accion_invitacion);
        txHistorialTotalDebido = view.findViewById(R.id.tx_historial_total_debido);
        txHistorialTotalPagado = view.findViewById(R.id.tx_historial_total_pagado);
        txHistorialTotalPendiente = view.findViewById(R.id.tx_historial_total_pendiente);
        txMontoTotalPendiente = view.findViewById(R.id.tx_monto_total_pendiente);
        txCantidadDeudasPendientes = view.findViewById(R.id.tx_cantidad_integrantes);
        txCantidadDeudasPagadas = view.findViewById(R.id.tx_cantidad_deudas_pagadas);
        txTotalPagosPersonales = view.findViewById(R.id.tx_total_pagado_grupo);
        txTotalDeudasFamiliares = view.findViewById(R.id.tx_total_deudas_familiares);
        rvDeudasActivas = view.findViewById(R.id.rv_deudas_activas);
    }

    //Abre la pantalla que corresponde a cada tarjeta
    private void configurarListeners() {
        cardEditarPerfil.setOnClickListener(v -> startActivity(new Intent(getActivity(), ActividadEditarPerfil.class)));
        cardAgregarDeuda.setOnClickListener(v -> startActivity(new Intent(getActivity(), AñadirDeuda.class)));
        cardCrearGrupo.setOnClickListener(v -> startActivity(new Intent(getActivity(), CrearGrupoFamiliar.class)));
        cardAceptarInvitacion.setOnClickListener(v -> {
            if (getActivity() instanceof ActividadPrincipal) ((ActividadPrincipal) getActivity()).selectTab(R.id.nav_invitations);
        });
    }

    //al tocar una se ve el detalle o se registra un pago
    private void configurarRecyclerView() {
        adaptadorDeudas = new listaDeDeudas(requireContext(), new listaDeDeudas.MonitorDeudas() {
            @Override public void tocDeuda(Deuda debt) {
                Intent intencion = new Intent(getActivity(), DetallesDeuda.class);
                intencion.putExtra("debt_id", debt.getId());
                startActivity(intencion);
            }
            @Override public void tocPagar(Deuda debt) {
                Intent intent = new Intent(getActivity(), RegistrarAbonosDeuda.class);
                intent.putExtra("debt_id", debt.getId());
                startActivity(intent);
            }
        });
        rvDeudasActivas.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvDeudasActivas.setAdapter(adaptadorDeudas);
    }

    //Actualiza la pantalla cada vez que cambian los datos
    private void observarDatos() {
        repositorio.getAllUsersLive().observe(getViewLifecycleOwner(), usuarios -> {
            if (usuarios != null) {
                todosLosUsuarios = usuarios.toArray(new Usuario[0]);
                adaptadorDeudas.setUsuarios(todosLosUsuarios);
                String idActual = repositorio.getCurrentUserId();
                //Busca al usuario actual y muestra su nombre
                for (Usuario user : todosLosUsuarios) if (user.getId().equals(idActual)) {
                    usuarioActual = user;
                    txNombreBienvenida.setText("Hola, " + user.getNomCompl());
                    break;
                }
                recalcularResumen();
            }
        });
        repositorio.getAllDebtsLive().observe(getViewLifecycleOwner(), debts -> {
            if (debts != null) {
                todasLasDeudasallDebts = debts.toArray(new Deuda[0]);
                adaptadorDeudas.setDeudas(filterActiveDebts());
                recalcularResumen();
            }
        });
        repositorio.getAllPaymentsLive().observe(getViewLifecycleOwner(), pagos -> {
            if (pagos != null) {
                todosLosPagos = pagos.toArray(new Pago[0]);
                adaptadorDeudas.setPagos(todosLosPagos);
                recalcularResumen();
            }
        });
        repositorio.getAllGroupsLive().observe(getViewLifecycleOwner(), grupos -> {
            if (grupos != null) {
                todosLosGrupos = grupos.toArray(new Grupofamiliar[0]);
                adaptadorDeudas.setGrupos(todosLosGrupos);
                adaptadorDeudas.setDeudas(filterActiveDebts());
                recalcularResumen();
            }
        });
        repositorio.getAllInvitationsLive().observe(getViewLifecycleOwner(), invitaciones -> {
            if (invitaciones != null) {
                todasLasInvitaciones = invitaciones.toArray(new Invitacion[0]);
                String usuarioid = repositorio.getCurrentUserId();
                int cantidadPendientes = 0;
                //Cuenta las invitaciones pendientes del usuario
                for (Invitacion inv : todasLasInvitaciones)
                    if (inv.getInvitedUserId().equals(usuarioid) && "pendiente".equalsIgnoreCase(inv.getStatus())) cantidadPendientes++;
                txSubAccionInvitacion.setText(cantidadPendientes > 0
                        ? cantidadPendientes + " invitación" + (cantidadPendientes > 1 ? "es" : "") + " pendiente" + (cantidadPendientes > 1 ? "s" : "")
                        : "Ver invitaciones pendientes");
            }
        });
    }

    //Devuelve hasta tres deudas activas que correspondan al usuario
    private Deuda[] filterActiveDebts() {
        String usuarioid = repositorio.getCurrentUserId();
        String[] userGroupIds = obtenerIdsGruposUsuario(usuarioid);
        Deuda[] activas = new Deuda[Math.min(3, todasLasDeudasallDebts.length)];
        int contador = 0;
        for (Deuda d : todasLasDeudasallDebts) {
            //Es relevante si es personal del usuario o de uno de sus grupos
            boolean relevante = (d.getGrupoFamiliarId() == null && (usuarioid.equals(d.getAssignedUserId()) || usuarioid.equals(d.getCreatedBy())))
                    || (d.getGrupoFamiliarId() != null && contiene(userGroupIds, d.getGrupoFamiliarId()));
            if (relevante) {
                double pagado = obtenerMontoPagado(d);
                double relevant = Math.max(0.0, d.getAmount() - pagado);
                if (relevant > 0 && !"pagada".equalsIgnoreCase(d.getStatus())) {
                    activas[contador++] = d;
                    if (contador == activas.length) break;
                }
            }
        }
        Deuda[] resultado = new Deuda[contador];
        System.arraycopy(activas, 0, resultado, 0, contador);
        return resultado;
    }

    //Calcula los totales de deudas y pagos y los muestra en pantalla
    private void recalcularResumen() {
        String usuarioid = repositorio.getCurrentUserId();
        if (usuarioid == null) return;
        String[] idsGruposUsuario = obtenerIdsGruposUsuario(usuarioid);
        int cantidadPendientes = 0, cantidadPagadas = 0;
        double totalDebido = 0.0, totalPagado = 0.0, totalPorPagar = 0.0;

        //Suma lo debido, lo pagado y lo pendiente de cada deuda relevante
        for (Deuda d : todasLasDeudasallDebts) {
            boolean relevant = (d.getGrupoFamiliarId() == null && (usuarioid.equals(d.getAssignedUserId()) || usuarioid.equals(d.getCreatedBy())))
                    || (d.getGrupoFamiliarId() != null && contiene(idsGruposUsuario, d.getGrupoFamiliarId()));
            if (relevant) {
                totalDebido += d.getAmount();
                double pagado = obtenerMontoPagado(d);
                totalPagado += pagado;
                double remaining = Math.max(0.0, d.getAmount() - pagado);
                if (remaining == 0.0 || "pagado".equalsIgnoreCase(d.getStatus())) cantidadPagadas++;
                else { cantidadPendientes++; totalPorPagar += remaining; }
            }
        }

        //Total de pagos hechos por el usuario
        double pagosPersonales = 0.0;
        for (Pago p : todosLosPagos) if (usuarioid.equals(p.getUserId())) pagosPersonales += p.getMonto();

        //Lo que falta por pagar en las deudas de sus grupos
        double pendienteFamiliar = 0.0;
        for (Deuda d : todasLasDeudasallDebts) if (d.getGrupoFamiliarId() != null && contiene(idsGruposUsuario, d.getGrupoFamiliarId()))
            pendienteFamiliar += Math.max(0.0, d.getAmount() - obtenerMontoPagado(d));
        double totalPendiente = Math.max(0.0, totalDebido - totalPagado);
        if (txHistorialTotalDebido != null) txHistorialTotalDebido.setText(FreyrRepository.formatCurrency(totalDebido));
        if (txHistorialTotalPagado != null) txHistorialTotalPagado.setText(FreyrRepository.formatCurrency(totalPagado));
        if (txHistorialTotalPendiente != null) txHistorialTotalPendiente.setText(FreyrRepository.formatCurrency(totalPendiente));
        txMontoTotalPendiente.setText(FreyrRepository.formatCurrency(totalPorPagar));
        txCantidadDeudasPendientes.setText(String.valueOf(cantidadPendientes));
        txCantidadDeudasPagadas.setText(String.valueOf(cantidadPagadas));
        txTotalPagosPersonales.setText(FreyrRepository.formatCurrency(pagosPersonales));
        txTotalDeudasFamiliares.setText(FreyrRepository.formatCurrency(pendienteFamiliar));
    }

    //Obtiene los identificadores de grupos a los que pertenece el usuario.
    private String[] obtenerIdsGruposUsuario(String userId) {
        String[] ids = new String[todosLosGrupos.length];
        int count = 0;
        for (Grupofamiliar group : todosLosGrupos)
            if (group.getMiembrosIds() != null && group.getMiembrosIds().contains(userId)) ids[count++] = group.getId();
        String[] result = new String[count];
        System.arraycopy(ids, 0, result, 0, count);
        return result;
    }
    //Revisa si el arreglo contiene el valor buscado
    private boolean contiene(String[] valores, String objetivo) {
        for (String valor : valores) if (objetivo.equals(valor)) return true;
        return false;
    }

    //Suma los pagos hechos a una deuda
    private double obtenerMontoPagado(Deuda debt) {
        double paid = 0.0;
        for (Pago payment : todosLosPagos) if (payment.getDeudaId().equals(debt.getId())) paid += payment.getMonto();
        return paid;
    }
}

