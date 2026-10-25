package com.freyr.app.ui.debt;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.freyr.app.FreyrApplication;
import com.freyr.app.R;
import com.freyr.app.data.model.Debt;
import com.freyr.app.data.model.FamilyGroup;
import com.freyr.app.data.model.User;
import com.freyr.app.data.repository.FreyrRepository;
import java.util.ArrayList;
import java.util.List;

/**
 * Actividad para la creación y registro de nuevas deudas (personales o familiares).
 * Permite definir nombre, monto, descripción opcional, asociar a un grupo familiar
 * y asignar la responsabilidad a un miembro en particular o a toda la familia.
 */
public class AddDebtActivity extends AppCompatActivity {

    private EditText etDebtName;
    private EditText etDebtAmount;
    private EditText etDebtDescription;
    private RadioGroup rgDebtType;
    private RadioButton rbPersonal;
    private RadioButton rbFamily;
    private LinearLayout layoutFamilyOptions;
    private Spinner spinnerFamilyGroup;
    private Spinner spinnerAssignedMember;
    private Button btnSaveDebt;
    private ImageButton btnBack;
    private TextView tvError;

    private FreyrRepository repository;
    private List<FamilyGroup> userGroups = new ArrayList<>();
    private List<User> allUsers = new ArrayList<>();
    private List<String> groupNames = new ArrayList<>();
    private List<String> memberNames = new ArrayList<>();
    private List<String> memberIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_debt);

        repository = ((FreyrApplication) getApplication()).getRepository();

        initViews();
        setupListeners();
        loadData();

        // Verificar si se pasó un identificador de grupo preseleccionado mediante Intent
        String initialGroupId = getIntent().getStringExtra("group_id");
        if (initialGroupId != null && !initialGroupId.isEmpty()) {
            rbFamily.setChecked(true);
            layoutFamilyOptions.setVisibility(View.VISIBLE);
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvError = findViewById(R.id.tv_error);
        rgDebtType = findViewById(R.id.rg_debt_type);
        rbPersonal = findViewById(R.id.rb_personal_debt);
        rbFamily = findViewById(R.id.rb_family_debt);

        etDebtName = findViewById(R.id.et_debt_name);
        etDebtAmount = findViewById(R.id.et_debt_amount);
        etDebtDescription = findViewById(R.id.et_debt_description);

        layoutFamilyOptions = findViewById(R.id.layout_family_options);
        spinnerFamilyGroup = findViewById(R.id.spinner_family_group);
        spinnerAssignedMember = findViewById(R.id.spinner_assigned_member);
        btnSaveDebt = findViewById(R.id.btn_save_debt);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        rgDebtType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_family_debt) {
                if (userGroups.isEmpty()) {
                    showError("Primero debes crear o unirte a un grupo familiar.");
                    rbPersonal.setChecked(true);
                    layoutFamilyOptions.setVisibility(View.GONE);
                } else {
                    layoutFamilyOptions.setVisibility(View.VISIBLE);
                    clearError();
                }
            } else {
                layoutFamilyOptions.setVisibility(View.GONE);
                clearError();
            }
        });

        spinnerFamilyGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < userGroups.size()) {
                    updateMemberSpinner(userGroups.get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSaveDebt.setOnClickListener(v -> saveDebt());
    }

    private void loadData() {
        repository.getAllUsersLive().observe(this, users -> {
            if (users != null) {
                allUsers = users;
                if (!userGroups.isEmpty() && spinnerFamilyGroup.getSelectedItemPosition() >= 0) {
                    updateMemberSpinner(userGroups.get(spinnerFamilyGroup.getSelectedItemPosition()));
                }
            }
        });

        repository.getAllGroupsLive().observe(this, groups -> {
            if (groups != null) {
                String currentUid = repository.getCurrentUserId();
                userGroups.clear();
                groupNames.clear();

                for (FamilyGroup g : groups) {
                    if (g.getMemberIds() != null && g.getMemberIds().contains(currentUid)) {
                        userGroups.add(g);
                        groupNames.add(g.getName());
                    }
                }

                ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    groupNames
                );
                spinnerFamilyGroup.setAdapter(groupAdapter);

                if (!userGroups.isEmpty()) {
                    updateMemberSpinner(userGroups.get(0));
                }
            }
        });
    }

    private void updateMemberSpinner(FamilyGroup group) {
        memberNames.clear();
        memberIds.clear();

        memberNames.add("Toda la familia (Compartida)");
        memberIds.add(null);

        if (group != null && group.getMemberIds() != null) {
            for (String uid : group.getMemberIds()) {
                String name = "Miembro (" + uid + ")";
                for (User u : allUsers) {
                    if (u.getId().equals(uid)) {
                        name = u.getFullName() + " (@" + u.getUsername() + ")";
                        break;
                    }
                }
                memberNames.add(name);
                memberIds.add(uid);
            }
        }

        ArrayAdapter<String> memberAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            memberNames
        );
        spinnerAssignedMember.setAdapter(memberAdapter);
    }

    private void saveDebt() {
        clearError();

        // 1. Validar nombre de la deuda
        String name = etDebtName.getText().toString().trim();
        if (name.isEmpty()) {
            showError("El nombre de la deuda no puede estar vacío.");
            etDebtName.requestFocus();
            return;
        }

        // 2. Validar monto
        String amountStr = etDebtAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            showError("El monto total no puede estar vacío.");
            etDebtAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            showError("Por favor ingresa un monto numérico válido.");
            etDebtAmount.requestFocus();
            return;
        }

        if (amount <= 0.0) {
            showError("El monto debe ser un número positivo mayor a 0.");
            etDebtAmount.requestFocus();
            return;
        }

        String description = etDebtDescription.getText().toString().trim();

        // 3. Determinar grupo y responsable
        boolean isFamily = rbFamily.isChecked();
        String familyGroupId = null;
        String assignedUserId = repository.getCurrentUserId();

        if (isFamily) {
            if (userGroups.isEmpty() || spinnerFamilyGroup.getSelectedItemPosition() < 0) {
                showError("Por favor selecciona un grupo familiar.");
                return;
            }
            FamilyGroup selectedGroup = userGroups.get(spinnerFamilyGroup.getSelectedItemPosition());
            familyGroupId = selectedGroup.getId();

            int memberPos = spinnerAssignedMember.getSelectedItemPosition();
            if (memberPos >= 0 && memberPos < memberIds.size()) {
                assignedUserId = memberIds.get(memberPos);
            } else {
                assignedUserId = null;
            }
        }

        btnSaveDebt.setEnabled(false);

        repository.addDebt(name, description, amount, assignedUserId, familyGroupId, new FreyrRepository.Callback<Debt>() {
            @Override
            public void onSuccess(Debt result) {
                runOnUiThread(() -> {
                    Toast.makeText(AddDebtActivity.this, "¡Deuda guardada exitosamente!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnSaveDebt.setEnabled(true);
                    showError(error);
                });
            }
        });
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void clearError() {
        tvError.setText("");
        tvError.setVisibility(View.GONE);
    }
}

