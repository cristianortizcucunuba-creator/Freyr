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
import com.freyr.app.Clases.MainActivity;
import com.freyr.app.Clases.listas.listaDeDeudas;
import com.freyr.app.Clases.deuda.AñadirDeuda;
import com.freyr.app.Clases.deuda.RegistrarAbonosDeuda;
import com.freyr.app.Clases.deuda.DetallesDeuda;
import com.freyr.app.Clases.grupo.CrearGrupoFamiliar;
import com.freyr.app.Clases.profile.EditProfileActivity;

public class RecapMesFinancieroFragment extends Fragment {

    private TextView tvNombreBienvenida;
    private TextView tvSubtituloBienvenida;
    private CardView cardEditarPerfil, cardAgregarDeuda, cardCrearGrupo, cardAceptarInvitacion;
    private TextView tvTituloAccionInvitacion, tvSubAccionInvitacion;
    private TextView tvHistorialTotalDebido, tvHistorialTotalPagado, tvHistorialTotalPendiente;
    private TextView tvMontoTotalPendiente, tvCantidadDeudasPendientes, tvCantidadDeudasPagadas;
    private TextView tvTotalPagosPersonales, tvTotalDeudasFamiliares;
    private RecyclerView rvDeudasActivas;
    private listaDeDeudas adaptadorDeudas;
    private FreyrRepository repository;

    //Guardan los datos usando arreglos.
    private Deuda[] todasLasDeudasallDebts = new Deuda[0];
    private Pago[] todosLosPagos = new Pago[0];
    private Grupofamiliar[] todosLosGrupos = new Grupofamiliar[0];
    private Invitation[] todasLasInvitaciones = new Invitation[0];
    private Usuario[] todosLosUsuarios = new Usuario[0];
    private Usuario usuarioActual;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        repository = ((FreyrApplication) requireActivity().getApplication()).getRepository();
        initViews(view);
        setupListeners();
        setupRecyclerView();
        observeData();
        return view;
    }

    private void initViews(View view) {
        tvNombreBienvenida = view.findViewById(R.id.tv_welcome_name);
        tvSubtituloBienvenida = view.findViewById(R.id.tv_welcome_subtitle);
        cardEditarPerfil = view.findViewById(R.id.card_action_edit_profile);
        cardAgregarDeuda = view.findViewById(R.id.card_action_add_debt);
        cardCrearGrupo = view.findViewById(R.id.card_action_create_group);
        cardAceptarInvitacion = view.findViewById(R.id.card_action_accept_invite);
        tvTituloAccionInvitacion = view.findViewById(R.id.tv_invite_action_title);
        tvSubAccionInvitacion = view.findViewById(R.id.tv_invite_action_sub);
        tvHistorialTotalDebido = view.findViewById(R.id.tv_historial_total_debido);
        tvHistorialTotalPagado = view.findViewById(R.id.tv_historial_total_pagado);
        tvHistorialTotalPendiente = view.findViewById(R.id.tv_historial_total_pendiente);
        tvMontoTotalPendiente = view.findViewById(R.id.tv_total_pending_amount);
        tvCantidadDeudasPendientes = view.findViewById(R.id.tv_pending_debts_count);
        tvCantidadDeudasPagadas = view.findViewById(R.id.tv_paid_debts_count);
        tvTotalPagosPersonales = view.findViewById(R.id.tv_personal_payments_total);
        tvTotalDeudasFamiliares = view.findViewById(R.id.tv_family_debts_total);
        rvDeudasActivas = view.findViewById(R.id.rv_active_debts);
    }

    private void setupListeners() {
        cardEditarPerfil.setOnClickListener(v -> startActivity(new Intent(getActivity(), EditProfileActivity.class)));
        cardAgregarDeuda.setOnClickListener(v -> startActivity(new Intent(getActivity(), AñadirDeuda.class)));
        cardCrearGrupo.setOnClickListener(v -> startActivity(new Intent(getActivity(), CrearGrupoFamiliar.class)));
        cardAceptarInvitacion.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).selectTab(R.id.nav_invitations);
        });
    }

    private void setupRecyclerView() {
        adaptadorDeudas = new listaDeDeudas(requireContext(), new listaDeDeudas.MonitorDeudas() {
            @Override public void tocDeuda(Deuda debt) {
                Intent intent = new Intent(getActivity(), DetallesDeuda.class);
                intent.putExtra("debt_id", debt.getId());
                startActivity(intent);
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

    private void observeData() {
        repository.getAllUsersLive().observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                todosLosUsuarios = users.toArray(new Usuario[0]);
                adaptadorDeudas.setUsuarios(todosLosUsuarios);
                String curId = repository.getCurrentUserId();
                for (Usuario u : todosLosUsuarios) if (u.getId().equals(curId)) {
                    usuarioActual = u;
                    tvNombreBienvenida.setText("Hola, " + u.getNomCompl());
                    break;
                }
                recalculateSummary();
            }
        });
        repository.getAllDebtsLive().observe(getViewLifecycleOwner(), debts -> {
            if (debts != null) {
                todasLasDeudasallDebts = debts.toArray(new Deuda[0]);
                adaptadorDeudas.setDeudas(filterActiveDebts());
                recalculateSummary();
            }
        });
        repository.getAllPaymentsLive().observe(getViewLifecycleOwner(), payments -> {
            if (payments != null) {
                todosLosPagos = payments.toArray(new Pago[0]);
                adaptadorDeudas.setPagos(todosLosPagos);
                recalculateSummary();
            }
        });
        repository.getAllGroupsLive().observe(getViewLifecycleOwner(), groups -> {
            if (groups != null) {
                todosLosGrupos = groups.toArray(new Grupofamiliar[0]);
                adaptadorDeudas.setGrupos(todosLosGrupos);
                adaptadorDeudas.setDeudas(filterActiveDebts());
                recalculateSummary();
            }
        });
        repository.getAllInvitationsLive().observe(getViewLifecycleOwner(), invitations -> {
            if (invitations != null) {
                todasLasInvitaciones = invitations.toArray(new Invitation[0]);
                String uid = repository.getCurrentUserId();
                int pendingCount = 0;
                for (Invitation inv : todasLasInvitaciones)
                    if (inv.getInvitedUserId().equals(uid) && "pending".equalsIgnoreCase(inv.getStatus())) pendingCount++;
                tvSubAccionInvitacion.setText(pendingCount > 0
                        ? pendingCount + " invitación" + (pendingCount > 1 ? "es" : "") + " pendiente" + (pendingCount > 1 ? "s" : "")
                        : "Ver invitaciones pendientes");
            }
        });
    }

    // Devuelve hasta tres deudas activas que correspondan al usuario.
    private Deuda[] filterActiveDebts() {
        String uid = repository.getCurrentUserId();
        String[] userGroupIds = getUserGroupIds(uid);
        Deuda[] active = new Deuda[Math.min(3, todasLasDeudasallDebts.length)];
        int count = 0;
        for (Deuda d : todasLasDeudasallDebts) {
            boolean relevant = (d.getFamilyGroupId() == null && (uid.equals(d.getAssignedUserId()) || uid.equals(d.getCreatedBy())))
                    || (d.getFamilyGroupId() != null && contains(userGroupIds, d.getFamilyGroupId()));
            if (relevant) {
                double paid = getPaidAmount(d);
                double remaining = Math.max(0.0, d.getAmount() - paid);
                if (remaining > 0 && !"paid".equalsIgnoreCase(d.getStatus())) {
                    active[count++] = d;
                    if (count == active.length) break;
                }
            }
        }
        Deuda[] result = new Deuda[count];
        System.arraycopy(active, 0, result, 0, count);
        return result;
    }

    private void recalculateSummary() {
        String uid = repository.getCurrentUserId();
        if (uid == null) return;
        String[] userGroupIds = getUserGroupIds(uid);
        int pendingCount = 0, paidCount = 0;
        double totalDebido = 0.0, totalPagado = 0.0, totalPending = 0.0;
        for (Deuda d : todasLasDeudasallDebts) {
            boolean relevant = (d.getFamilyGroupId() == null && (uid.equals(d.getAssignedUserId()) || uid.equals(d.getCreatedBy())))
                    || (d.getFamilyGroupId() != null && contains(userGroupIds, d.getFamilyGroupId()));
            if (relevant) {
                totalDebido += d.getAmount();
                double paid = getPaidAmount(d);
                totalPagado += paid;
                double remaining = Math.max(0.0, d.getAmount() - paid);
                if (remaining == 0.0 || "paid".equalsIgnoreCase(d.getStatus())) paidCount++;
                else { pendingCount++; totalPending += remaining; }
            }
        }
        double personalPayments = 0.0;
        for (Pago p : todosLosPagos) if (uid.equals(p.getUserId())) personalPayments += p.getAmount();
        double familyPending = 0.0;
        for (Deuda d : todasLasDeudasallDebts) if (d.getFamilyGroupId() != null && contains(userGroupIds, d.getFamilyGroupId()))
            familyPending += Math.max(0.0, d.getAmount() - getPaidAmount(d));
        double totalPendiente = Math.max(0.0, totalDebido - totalPagado);
        if (tvHistorialTotalDebido != null) tvHistorialTotalDebido.setText(FreyrRepository.formatCurrency(totalDebido));
        if (tvHistorialTotalPagado != null) tvHistorialTotalPagado.setText(FreyrRepository.formatCurrency(totalPagado));
        if (tvHistorialTotalPendiente != null) tvHistorialTotalPendiente.setText(FreyrRepository.formatCurrency(totalPendiente));
        tvMontoTotalPendiente.setText(FreyrRepository.formatCurrency(totalPending));
        tvCantidadDeudasPendientes.setText(String.valueOf(pendingCount));
        tvCantidadDeudasPagadas.setText(String.valueOf(paidCount));
        tvTotalPagosPersonales.setText(FreyrRepository.formatCurrency(personalPayments));
        tvTotalDeudasFamiliares.setText(FreyrRepository.formatCurrency(familyPending));
    }

    // Obtiene los identificadores de grupos a los que pertenece el usuario.
    private String[] getUserGroupIds(String userId) {
        String[] ids = new String[todosLosGrupos.length];
        int count = 0;
        for (Grupofamiliar group : todosLosGrupos)
            if (group.getMemberIds() != null && group.getMemberIds().contains(userId)) ids[count++] = group.getId();
        String[] result = new String[count];
        System.arraycopy(ids, 0, result, 0, count);
        return result;
    }

    private boolean contains(String[] values, String target) {
        for (String value : values) if (target.equals(value)) return true;
        return false;
    }

    private double getPaidAmount(Deuda debt) {
        double paid = 0.0;
        for (Pago payment : todosLosPagos) if (payment.getDebtId().equals(debt.getId())) paid += payment.getAmount();
        return paid;
    }
}
