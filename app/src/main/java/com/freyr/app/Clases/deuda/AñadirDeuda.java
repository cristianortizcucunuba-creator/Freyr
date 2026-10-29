package com.freyr.app.Clases.deuda;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.freyr.app.FreyrApp;
import com.freyr.app.R;
import com.freyr.app.data.datos.Deuda;
import com.freyr.app.data.datos.Grupofamiliar;
import com.freyr.app.data.datos.Usuario;
import com.freyr.app.data.repositorio.FreyrRepositorio;

public class AñadirDeuda extends AppCompatActivity {

    private EditText etNombreDeuda;
    private EditText etMonto;
    private EditText etDescripcion;
    private RadioGroup rgTipoDeuda;
    private RadioButton rbPersonal;
    private RadioButton rbFamilia;
    private LinearLayout layoutFamilyOptions;
    private Spinner spinnerFamilyGroup;
    private Spinner spinnerAssignedMember;
    private Button btnGuardarDeuda;
    private ImageButton btnAtras;
    private TextView tvError;

    private FreyrRepositorio repositorio;

    // Arreglos para guardar los datos.
    private Grupofamiliar[] GruposUsuario = new Grupofamiliar[0];
    private Usuario[] Cuentas = new Usuario[0];
    private String[] nombreMiembros = new String[0];
    private String[] idMiembros = new String[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_debt);

        repositorio = ((FreyrApp) getApplication()).getRepository();

        iniciarVistas();
        setupListeners();
        loadData();


        String initialGroupId = getIntent().getStringExtra("group_id");
        if (initialGroupId != null && !initialGroupId.isEmpty()) {
            rbFamilia.setChecked(true);
            layoutFamilyOptions.setVisibility(View.VISIBLE);
        }
    }

    private void iniciarVistas() {
        btnAtras = findViewById(R.id.btn_atras);
        tvError = findViewById(R.id.tx_error);
        rgTipoDeuda = findViewById(R.id.rg_tipo_deuda);
        rbPersonal = findViewById(R.id.rb_deuda_personal);
        rbFamilia = findViewById(R.id.rb_deuda_familiar);

        etNombreDeuda = findViewById(R.id.et_nombre_deuda);
        etMonto = findViewById(R.id.et_monto);
        etDescripcion = findViewById(R.id.et_descripcion);

        layoutFamilyOptions = findViewById(R.id.layout_family_options);
        spinnerFamilyGroup = findViewById(R.id.spinner_family_group);
        spinnerAssignedMember = findViewById(R.id.spinner_assigned_member);
        btnGuardarDeuda = findViewById(R.id.btn_guardar_deuda);
    }

    private void setupListeners() {
        btnAtras.setOnClickListener(v -> finish());

        rgTipoDeuda.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_deuda_familiar) {
                if (GruposUsuario.length == 0) {
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
                        if (position >= 0 && position < GruposUsuario.length) {
                            updateMemberSpinner(GruposUsuario[position]);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                }
        );

        btnGuardarDeuda.setOnClickListener(v -> saveDebt());
    }

    private void loadData() {
        repositorio.getAllUsersLive().observe(this, users -> {
            if (users != null) {
                // El repositorio entrega una lista; la convertimos a arreglo.
                Cuentas = users.toArray(new Usuario[0]);

                int selectedPosition = spinnerFamilyGroup.getSelectedItemPosition();
                if (GruposUsuario.length > 0
                        && selectedPosition >= 0
                        && selectedPosition < GruposUsuario.length) {
                    updateMemberSpinner(GruposUsuario[selectedPosition]);
                }
            }
        });

        repositorio.getAllGroupsLive().observe(this, groups -> {
            if (groups != null) {
                String currentUid = repositorio.getCurrentUserId();

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
                GruposUsuario = new Grupofamiliar[count];
                System.arraycopy(matchingGroups, 0, GruposUsuario, 0, count);

                String[] groupNames = new String[count];
                for (int i = 0; i < count; i++) {
                    groupNames[i] = GruposUsuario[i].getNombre();
                }

                ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        groupNames
                );
                spinnerFamilyGroup.setAdapter(groupAdapter);

                if (GruposUsuario.length > 0) {
                    updateMemberSpinner(GruposUsuario[0]);
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
        nombreMiembros = new String[groupMemberCount + 1];
        idMiembros = new String[groupMemberCount + 1];

        nombreMiembros[0] = "Toda la familia (Compartida)";
        idMiembros[0] = null;

        if (group != null && group.getMiembrosIds() != null) {
            int index = 1;

            for (String userId : group.getMiembrosIds()) {
                String name = "Miembro (" + userId + ")";

                for (Usuario user : Cuentas) {
                    if (user.getId().equals(userId)) {
                        name = user.getNomCompl() + " (@" + user.getNomUsuario() + ")";
                        break;
                    }
                }

                nombreMiembros[index] = name;
                idMiembros[index] = userId;
                index++;
            }
        }

        ArrayAdapter<String> memberAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                nombreMiembros
        );
        spinnerAssignedMember.setAdapter(memberAdapter);
    }

    private void saveDebt() {
        clearError();

        // Valida el nombre de la deuda.
        String name = etNombreDeuda.getText().toString().trim();
        if (name.isEmpty()) {
            showError("El nombre de la deuda no puede estar vacío.");
            etNombreDeuda.requestFocus();
            return;
        }

        // Valida que el monto no esté vacío.
        String amountText = etMonto.getText().toString().trim();
        if (amountText.isEmpty()) {
            showError("El monto total no puede estar vacío.");
            etMonto.requestFocus();
            return;
        }

        // Convierte y valida el monto.
        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            showError("Por favor ingresa un monto numérico válido.");
            etMonto.requestFocus();
            return;
        }

        if (amount <= 0.0) {
            showError("El monto debe ser un número positivo mayor a 0.");
            etMonto.requestFocus();
            return;
        }

        String description = etDescripcion.getText().toString().trim();

        // Define el grupo y la persona responsable.
        boolean isFamily = rbFamilia.isChecked();
        String familyGroupId = null;
        String assignedUserId = repositorio.getCurrentUserId();

        if (isFamily) {
            int groupPosition = spinnerFamilyGroup.getSelectedItemPosition();

            if (GruposUsuario.length == 0
                    || groupPosition < 0
                    || groupPosition >= GruposUsuario.length) {
                showError("Por favor selecciona un grupo familiar.");
                return;
            }

            Grupofamiliar selectedGroup = GruposUsuario[groupPosition];
            familyGroupId = selectedGroup.getId();

            int memberPosition = spinnerAssignedMember.getSelectedItemPosition();
            if (memberPosition >= 0 && memberPosition < idMiembros.length) {
                assignedUserId = idMiembros[memberPosition];
            } else {
                assignedUserId = null;
            }
        }

        btnGuardarDeuda.setEnabled(false);

        repositorio.addDebt(
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
                            btnGuardarDeuda.setEnabled(true);
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