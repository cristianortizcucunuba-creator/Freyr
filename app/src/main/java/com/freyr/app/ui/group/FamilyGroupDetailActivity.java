package com.freyr.app.ui.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.freyr.app.FreyrApplication;
import com.freyr.app.R;
import com.freyr.app.data.model.Debt;
import com.freyr.app.data.model.FamilyGroup;
import com.freyr.app.data.model.Payment;
import com.freyr.app.data.model.User;
import com.freyr.app.data.repository.FreyrRepository;
import com.freyr.app.ui.adapter.DebtAdapter;
import com.freyr.app.ui.debt.AddDebtActivity;
import com.freyr.app.ui.debt.AddPaymentActivity;
import com.freyr.app.ui.debt.DebtDetailActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Actividad que muestra la vista detallada de un grupo familiar.
 * Presenta el saldo total colectivo (total debido, total pagado, total pendiente),
 * el listado de miembros actuales, formulario para invitar nuevos usuarios por correo o nombre
 * y el listado de todas las deudas compartidas del grupo.
 */
public class FamilyGroupDetailActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvGroupTitle;
    private Button btnAddGroupDebt;
    private TextView tvGroupTotalAmt;
    private TextView tvGroupPaidAmt;
    private TextView tvGroupRemainingAmt;
    private TextView tvMembersList;
    private EditText etInviteIdentifier;
    private Button btnSendInvite;
    private TextView tvInviteFeedback;
    private TextView tvNoGroupDebts;
    private RecyclerView rvGroupDebts;

    private DebtAdapter debtAdapter;
    private FreyrRepository repository;
    private String groupId;
    private FamilyGroup currentGroup;
    private List<User> allUsers = new ArrayList<>();
    private List<Debt> groupDebts = new ArrayList<>();
    private List<Payment> allPayments = new ArrayList<>();

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
        btnBack = findViewById(R.id.btn_back);
        tvGroupTitle = findViewById(R.id.tv_group_title);
        btnAddGroupDebt = findViewById(R.id.btn_add_group_debt);
        tvGroupTotalAmt = findViewById(R.id.tv_group_total_amt);
        tvGroupPaidAmt = findViewById(R.id.tv_group_paid_amt);
        tvGroupRemainingAmt = findViewById(R.id.tv_group_remaining_amt);
        tvMembersList = findViewById(R.id.tv_members_list);
        etInviteIdentifier = findViewById(R.id.et_invite_identifier);
        btnSendInvite = findViewById(R.id.btn_send_invite);
        tvInviteFeedback = findViewById(R.id.tv_invite_feedback);
        tvNoGroupDebts = findViewById(R.id.tv_no_group_debts);
        rvGroupDebts = findViewById(R.id.rv_group_debts);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnAddGroupDebt.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddDebtActivity.class);
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
            repository.inviteUser(groupId, identifier, new FreyrRepository.Callback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    runOnUiThread(() -> {
                        btnSendInvite.setEnabled(true);
                        etInviteIdentifier.setText("");
                        showInviteFeedback("¡Invitación enviada exitosamente!", true);
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        btnSendInvite.setEnabled(true);
                        showInviteFeedback(error, false);
                    });
                }
            });
        });
    }

    private void showInviteFeedback(String msg, boolean isSuccess) {
        tvInviteFeedback.setText(msg);
        tvInviteFeedback.setTextColor(ContextCompat.getColor(this, isSuccess ? R.color.freyr_status_paid : R.color.freyr_primary));
        tvInviteFeedback.setVisibility(View.VISIBLE);
    }

    private void setupRecyclerView() {
        debtAdapter = new DebtAdapter(this, new DebtAdapter.OnDebtClickListener() {
            @Override
            public void onDebtClick(Debt debt) {
                Intent intent = new Intent(FamilyGroupDetailActivity.this, DebtDetailActivity.class);
                intent.putExtra("debt_id", debt.getId());
                startActivity(intent);
            }

            @Override
            public void onPayClick(Debt debt) {
                Intent intent = new Intent(FamilyGroupDetailActivity.this, AddPaymentActivity.class);
                intent.putExtra("debt_id", debt.getId());
                startActivity(intent);
            }
        });

        rvGroupDebts.setLayoutManager(new LinearLayoutManager(this));
        rvGroupDebts.setAdapter(debtAdapter);
    }

    private void observeData() {
        repository.getAllUsersLive().observe(this, users -> {
            if (users != null) {
                allUsers = users;
                updateMembersDisplay();
            }
        });

        repository.getAllGroupsLive().observe(this, groups -> {
            if (groups != null) {
                for (FamilyGroup g : groups) {
                    if (g.getId().equals(groupId)) {
                        currentGroup = g;
                        tvGroupTitle.setText(g.getName());
                        debtAdapter.setGroups(Collections.singletonList(g));
                        updateMembersDisplay();
                        break;
                    }
                }
            }
        });

        repository.getAllDebtsLive().observe(this, debts -> {
            if (debts != null) {
                groupDebts.clear();
                for (Debt d : debts) {
                    if (groupId.equals(d.getFamilyGroupId())) {
                        groupDebts.add(d);
                    }
                }
                debtAdapter.setDebts(groupDebts);
                tvNoGroupDebts.setVisibility(groupDebts.isEmpty() ? View.VISIBLE : View.GONE);
                recalculateTotals();
            }
        });

        repository.getAllPaymentsLive().observe(this, payments -> {
            if (payments != null) {
                allPayments = payments;
                debtAdapter.setPayments(payments);
                recalculateTotals();
            }
        });
    }

    private void updateMembersDisplay() {
        if (currentGroup == null || currentGroup.getMemberIds() == null) return;

        List<String> displayNames = new ArrayList<>();
        for (String uid : currentGroup.getMemberIds()) {
            boolean isCreator = uid.equals(currentGroup.getCreatorId());
            String name = "Usuario (" + uid + ")";
            for (User u : allUsers) {
                if (u.getId().equals(uid)) {
                    name = u.getFullName() + (isCreator ? " (Creador)" : "");
                    break;
                }
            }
            displayNames.add(name);
        }

        tvMembersList.setText(String.join(", ", displayNames));
    }

    private void recalculateTotals() {
        double total = 0.0;
        double paid = 0.0;

        for (Debt d : groupDebts) {
            total += d.getAmount();
            for (Payment p : allPayments) {
                if (p.getDebtId().equals(d.getId())) {
                    paid += p.getAmount();
                }
            }
        }

        double remaining = Math.max(0.0, total - paid);

        tvGroupTotalAmt.setText(FreyrRepository.formatCurrency(total));
        tvGroupPaidAmt.setText(FreyrRepository.formatCurrency(paid));
        tvGroupRemainingAmt.setText(FreyrRepository.formatCurrency(remaining));
    }
}

