package com.freyr.app.ui.dashboard;

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
import com.freyr.app.ui.MainActivity;
import com.freyr.app.ui.adapter.DebtAdapter;
import com.freyr.app.ui.debt.AddDebtActivity;
import com.freyr.app.ui.debt.AddPaymentActivity;
import com.freyr.app.ui.debt.DebtDetailActivity;
import com.freyr.app.ui.group.CreateFamilyGroupActivity;
import com.freyr.app.ui.profile.EditProfileActivity;
import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private TextView tvWelcomeName;
    private TextView tvWelcomeSubtitle;

    private CardView cardEditProfile;
    private CardView cardAddDebt;
    private CardView cardCreateGroup;
    private CardView cardAcceptInvite;
    private TextView tvInviteActionTitle;
    private TextView tvInviteActionSub;

    // Historial financiero completo (Requisito 6)
    private TextView tvHistorialTotalDebido;
    private TextView tvHistorialTotalPagado;
    private TextView tvHistorialTotalPendiente;

    // Resumen adicional
    private TextView tvTotalPendingAmount;
    private TextView tvPendingDebtsCount;
    private TextView tvPaidDebtsCount;
    private TextView tvPersonalPaymentsTotal;
    private TextView tvFamilyDebtsTotal;

    private RecyclerView rvActiveDebts;
    private DebtAdapter debtAdapter;

    private FreyrRepository repository;
    private List<Debt> allDebts = new ArrayList<>();
    private List<Payment> allPayments = new ArrayList<>();
    private List<FamilyGroup> allGroups = new ArrayList<>();
    private List<Invitation> allInvitations = new ArrayList<>();
    private List<User> allUsers = new ArrayList<>();
    private User currentUser;

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
        tvWelcomeName = view.findViewById(R.id.tv_welcome_name);
        tvWelcomeSubtitle = view.findViewById(R.id.tv_welcome_subtitle);

        cardEditProfile = view.findViewById(R.id.card_action_edit_profile);
        cardAddDebt = view.findViewById(R.id.card_action_add_debt);
        cardCreateGroup = view.findViewById(R.id.card_action_create_group);
        cardAcceptInvite = view.findViewById(R.id.card_action_accept_invite);
        tvInviteActionTitle = view.findViewById(R.id.tv_invite_action_title);
        tvInviteActionSub = view.findViewById(R.id.tv_invite_action_sub);

        // Historial Financiero
        tvHistorialTotalDebido = view.findViewById(R.id.tv_historial_total_debido);
        tvHistorialTotalPagado = view.findViewById(R.id.tv_historial_total_pagado);
        tvHistorialTotalPendiente = view.findViewById(R.id.tv_historial_total_pendiente);

        tvTotalPendingAmount = view.findViewById(R.id.tv_total_pending_amount);
        tvPendingDebtsCount = view.findViewById(R.id.tv_pending_debts_count);
        tvPaidDebtsCount = view.findViewById(R.id.tv_paid_debts_count);
        tvPersonalPaymentsTotal = view.findViewById(R.id.tv_personal_payments_total);
        tvFamilyDebtsTotal = view.findViewById(R.id.tv_family_debts_total);

        rvActiveDebts = view.findViewById(R.id.rv_active_debts);
    }

    private void setupListeners() {
        // 1. Editar Perfil
        cardEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        // 2. Añadir Deuda
        cardAddDebt.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddDebtActivity.class);
            startActivity(intent);
        });

        // 3. Crear Grupo Familiar
        cardCreateGroup.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateFamilyGroupActivity.class);
            startActivity(intent);
        });

        // 4. Ver Invitaciones
        cardAcceptInvite.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selectTab(R.id.nav_invitations);
            }
        });
    }

    private void setupRecyclerView() {
        debtAdapter = new DebtAdapter(requireContext(), new DebtAdapter.OnDebtClickListener() {
            @Override
            public void onDebtClick(Debt debt) {
                Intent intent = new Intent(getActivity(), DebtDetailActivity.class);
                intent.putExtra("debt_id", debt.getId());
                startActivity(intent);
            }

            @Override
            public void onPayClick(Debt debt) {
                Intent intent = new Intent(getActivity(), AddPaymentActivity.class);
                intent.putExtra("debt_id", debt.getId());
                startActivity(intent);
            }
        });

        rvActiveDebts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvActiveDebts.setAdapter(debtAdapter);
    }

    private void observeData() {
        repository.getAllUsersLive().observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                allUsers = users;
                debtAdapter.setUsers(users);
                String curId = repository.getCurrentUserId();
                for (User u : users) {
                    if (u.getId().equals(curId)) {
                        currentUser = u;
                        tvWelcomeName.setText("Hola, " + u.getFullName());
                        break;
                    }
                }
                recalculateSummary();
            }
        });

        repository.getAllDebtsLive().observe(getViewLifecycleOwner(), debts -> {
            if (debts != null) {
                allDebts = debts;
                debtAdapter.setDebts(filterActiveDebts());
                recalculateSummary();
            }
        });

        repository.getAllPaymentsLive().observe(getViewLifecycleOwner(), payments -> {
            if (payments != null) {
                allPayments = payments;
                debtAdapter.setPayments(payments);
                recalculateSummary();
            }
        });

        repository.getAllGroupsLive().observe(getViewLifecycleOwner(), groups -> {
            if (groups != null) {
                allGroups = groups;
                debtAdapter.setGroups(groups);
                debtAdapter.setDebts(filterActiveDebts());
                recalculateSummary();
            }
        });

        repository.getAllInvitationsLive().observe(getViewLifecycleOwner(), invitations -> {
            if (invitations != null) {
                allInvitations = invitations;
                String uid = repository.getCurrentUserId();
                int pendingCount = 0;
                for (Invitation inv : invitations) {
                    if (inv.getInvitedUserId().equals(uid) && "pending".equalsIgnoreCase(inv.getStatus())) {
                        pendingCount++;
                    }
                }
                if (pendingCount > 0) {
                    tvInviteActionSub.setText(pendingCount + " invitación" + (pendingCount > 1 ? "es" : "") + " pendiente" + (pendingCount > 1 ? "s" : ""));
                } else {
                    tvInviteActionSub.setText("Ver invitaciones pendientes");
                }
            }
        });
    }

    private List<Debt> filterActiveDebts() {
        String uid = repository.getCurrentUserId();
        List<String> userGroupIds = new ArrayList<>();
        for (FamilyGroup g : allGroups) {
            if (g.getMemberIds() != null && g.getMemberIds().contains(uid)) {
                userGroupIds.add(g.getId());
            }
        }

        List<Debt> active = new ArrayList<>();
        for (Debt d : allDebts) {
            boolean isRelevant = (d.getFamilyGroupId() == null && (uid.equals(d.getAssignedUserId()) || uid.equals(d.getCreatedBy())))
                || (d.getFamilyGroupId() != null && userGroupIds.contains(d.getFamilyGroupId()));

            if (isRelevant) {
                double paid = 0.0;
                for (Payment p : allPayments) {
                    if (p.getDebtId().equals(d.getId())) {
                        paid += p.getAmount();
                    }
                }
                double remaining = Math.max(0.0, d.getAmount() - paid);
                if (remaining > 0 && !"paid".equalsIgnoreCase(d.getStatus())) {
                    active.add(d);
                    if (active.size() >= 3) break;
                }
            }
        }
        return active;
    }

    private void recalculateSummary() {
        String uid = repository.getCurrentUserId();
        if (uid == null) return;

        List<String> userGroupIds = new ArrayList<>();
        for (FamilyGroup g : allGroups) {
            if (g.getMemberIds() != null && g.getMemberIds().contains(uid)) {
                userGroupIds.add(g.getId());
            }
        }

        int pendingCount = 0;
        int paidCount = 0;
        double totalDebido = 0.0;
        double totalPagado = 0.0;
        double totalPending = 0.0;

        for (Debt d : allDebts) {
            boolean isRelevant = (d.getFamilyGroupId() == null && (uid.equals(d.getAssignedUserId()) || uid.equals(d.getCreatedBy())))
                || (d.getFamilyGroupId() != null && userGroupIds.contains(d.getFamilyGroupId()));

            if (isRelevant) {
                totalDebido += d.getAmount();

                double paid = 0.0;
                for (Payment p : allPayments) {
                    if (p.getDebtId().equals(d.getId())) {
                        paid += p.getAmount();
                    }
                }
                totalPagado += paid;

                double remaining = Math.max(0.0, d.getAmount() - paid);
                if (remaining == 0.0 || "paid".equalsIgnoreCase(d.getStatus())) {
                    paidCount++;
                } else {
                    pendingCount++;
                    totalPending += remaining;
                }
            }
        }

        double personalPayments = 0.0;
        for (Payment p : allPayments) {
            if (uid.equals(p.getUserId())) {
                personalPayments += p.getAmount();
            }
        }

        double familyPending = 0.0;
        for (Debt d : allDebts) {
            if (d.getFamilyGroupId() != null && userGroupIds.contains(d.getFamilyGroupId())) {
                double paid = 0.0;
                for (Payment p : allPayments) {
                    if (p.getDebtId().equals(d.getId())) {
                        paid += p.getAmount();
                    }
                }
                familyPending += Math.max(0.0, d.getAmount() - paid);
            }
        }

        double totalPendiente = Math.max(0.0, totalDebido - totalPagado);

        // Actualizar Historial Financiero Completo (Requisito 6)
        if (tvHistorialTotalDebido != null) {
            tvHistorialTotalDebido.setText(FreyrRepository.formatCurrency(totalDebido));
        }
        if (tvHistorialTotalPagado != null) {
            tvHistorialTotalPagado.setText(FreyrRepository.formatCurrency(totalPagado));
        }
        if (tvHistorialTotalPendiente != null) {
            tvHistorialTotalPendiente.setText(FreyrRepository.formatCurrency(totalPendiente));
        }

        // Resumen
        tvTotalPendingAmount.setText(FreyrRepository.formatCurrency(totalPending));
        tvPendingDebtsCount.setText(String.valueOf(pendingCount));
        tvPaidDebtsCount.setText(String.valueOf(paidCount));
        tvPersonalPaymentsTotal.setText(FreyrRepository.formatCurrency(personalPayments));
        tvFamilyDebtsTotal.setText(FreyrRepository.formatCurrency(familyPending));
    }
}
