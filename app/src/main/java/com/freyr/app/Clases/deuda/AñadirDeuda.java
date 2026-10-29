package com.freyr.app.Clases.deuda;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.freyr.app.FreyrApplication;
import com.freyr.app.R;
import com.freyr.app.data.model.Deuda;
import com.freyr.app.data.model.Grupofamiliar;
import com.freyr.app.data.model.Usuario;
import com.freyr.app.data.repository.FreyrRepositorio;

public class AñadirDeuda extends AppCompatActivity {

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

    private FreyrRepositorio repository;

    // Datos de la pantalla guardados en arreglos.
    private Grupofamiliar[] userGroups = new Grupofamiliar[0];
    private Usuario[] allUsers = new Usuario[0];
    private String[] memberNames = new String[0];
    private String[] memberIds = new String[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_debt);

        repository = ((FreyrApplication) getApplication()).getRepository();

        initViews();
        setupListeners();
        loadData();

        // Muestra las opciones familiares si se recibió un grupo en el Intent.
        String initialGroupId = getIntent().getStringExtra("group_id");
        if (initialGroupId != null && !initialGroupId.isEmpty()) {
            rbFamily.setChecked(true);
            layoutFamilyOptions.setVisibility(View.VISIBLE);
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvError = findViewById(R.id.tx_error);
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
                if (userGroups.length == 0) {
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

        spinnerFamilyGroup.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {
                        if (position >= 0 && position < userGroups.length) {
                            updateMemberSpinner(userGroups[position]);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                }
        );

        btnSaveDebt.setOnClickListener(v -> saveDebt());
    }

    private void loadData() {
        repository.getAllUsersLive().observe(this, users -> {
            if (users != null) {
                // El repositorio entrega una lista; la convertimos a arreglo.
                allUsers = users.toArray(new Usuario[0]);

                int selectedPosition = spinnerFamilyGroup.getSelectedItemPosition();
                if (userGroups.length > 0
                        && selectedPosition >= 0
                        && selectedPosition < userGroups.length) {
                    updateMemberSpinner(userGroups[selectedPosition]);
                }
            }
        });

        repository.getAllGroupsLive().observe(this, groups -> {
            if (groups != null) {
                String currentUid = repository.getCurrentUserId();

                // Primero guardamos los grupos coincidentes en un arreglo temporal.
                Grupofamiliar[] matchingGroups = new Grupofamiliar[groups.size()];
                int count = 0;

                for (Grupofamiliar group : groups) {
                    if (group.getMiembrosIds() != null
                            && group.getMiembrosIds().contains(currentUid)) {
                        matchingGroups[count] = group;
                        count++;
                    }
                }

                // Copiamos solo los grupos encontrados al arreglo definitivo.
                userGroups = new Grupofamiliar[count];
                System.arraycopy(matchingGroups, 0, userGroups, 0, count);

                String[] groupNames = new String[count];
                for (int i = 0; i < count; i++) {
                    groupNames[i] = userGroups[i].getNombre();
                }

                ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        groupNames
                );
                spinnerFamilyGroup.setAdapter(groupAdapter);

                if (userGroups.length > 0) {
                    updateMemberSpinner(userGroups[0]);
                }
            }
        });
    }

    // Actualiza las opciones de responsable del grupo seleccionado.
    private void updateMemberSpinner(Grupofamiliar group) {
        int groupMemberCount = 0;
        if (group != null && group.getMiembrosIds() != null) {
            groupMemberCount = group.getMiembrosIds().size();
        }

        // La primera opción representa a toda la familia.
        memberNames = new String[groupMemberCount + 1];
        memberIds = new String[groupMemberCount + 1];

        memberNames[0] = "Toda la familia (Compartida)";
        memberIds[0] = null;

        if (group != null && group.getMiembrosIds() != null) {
            int index = 1;

            for (String userId : group.getMiembrosIds()) {
                String name = "Miembro (" + userId + ")";

                for (Usuario user : allUsers) {
                    if (user.getId().equals(userId)) {
                        name = user.getNomCompl() + " (@" + user.getNomUsuario() + ")";
                        break;
                    }
                }

                memberNames[index] = name;
                memberIds[index] = userId;
                index++;
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

        // Valida el nombre de la deuda.
        String name = etDebtName.getText().toString().trim();
        if (name.isEmpty()) {
            showError("El nombre de la deuda no puede estar vacío.");
            etDebtName.requestFocus();
            return;
        }

        // Valida que el monto no esté vacío.
        String amountText = etDebtAmount.getText().toString().trim();
        if (amountText.isEmpty()) {
            showError("El monto total no puede estar vacío.");
            etDebtAmount.requestFocus();
            return;
        }

        // Convierte y valida el monto.
        double amount;
        try {
            amount = Double.parseDouble(amountText);
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

        // Define el grupo y la persona responsable.
        boolean isFamily = rbFamily.isChecked();
        String familyGroupId = null;
        String assignedUserId = repository.getCurrentUserId();

        if (isFamily) {
            int groupPosition = spinnerFamilyGroup.getSelectedItemPosition();

            if (userGroups.length == 0
                    || groupPosition < 0
                    || groupPosition >= userGroups.length) {
                showError("Por favor selecciona un grupo familiar.");
                return;
            }

            Grupofamiliar selectedGroup = userGroups[groupPosition];
            familyGroupId = selectedGroup.getId();

            int memberPosition = spinnerAssignedMember.getSelectedItemPosition();
            if (memberPosition >= 0 && memberPosition < memberIds.length) {
                assignedUserId = memberIds[memberPosition];
            } else {
                assignedUserId = null;
            }
        }

        btnSaveDebt.setEnabled(false);

        repository.addDebt(
                name,
                description,
                amount,
                assignedUserId,
                familyGroupId,
                new FreyrRepositorio.Callback<Deuda>() {
                    @Override
                    public void onSuccess(Deuda result) {
                        runOnUiThread(() -> {
                            Toast.makeText(
                                    AñadirDeuda.this,
                                    "¡Deuda guardada exitosamente!",
                                    Toast.LENGTH_SHORT
                            ).show();
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
                }
        );
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