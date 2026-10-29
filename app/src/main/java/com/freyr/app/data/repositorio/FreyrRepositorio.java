package com.freyr.app.data.repositorio;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.freyr.app.data.local.FreyrDatabase;
import com.freyr.app.data.datos.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class FreyrRepositorio {

    private final FreyrDatabase database;
    private final SharedPreferences prefs;
    private final MutableLiveData<String> currentUserIdLive = new MutableLiveData<>();

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public FreyrRepositorio(Context context) {
        this.database = FreyrDatabase.getDatabase(context);
        this.prefs = context.getSharedPreferences("freyr_prefs", Context.MODE_PRIVATE);
        this.currentUserIdLive.setValue(prefs.getString("current_user_id", null));
    }

    public LiveData<String> getCurrentUserIdLive() {
        return currentUserIdLive;
    }

    public String getCurrentUserId() {
        return prefs.getString("current_user_id", null);
    }

    public void setLoggedInUser(String userId) {
        if (userId != null) {
            prefs.edit().putString("current_user_id", userId).apply();
        } else {
            prefs.edit().remove("current_user_id").apply();
        }
        currentUserIdLive.postValue(userId);
    }

    public LiveData<List<Usuario>> getAllUsersLive() {
        return database.userDao().getAllUsersLive();
    }

    public LiveData<List<Grupofamiliar>> getAllGroupsLive() {
        return database.familyGroupDao().getAllGroupsLive();
    }

    public LiveData<List<Deuda>> getAllDebtsLive() {
        return database.debtDao().getAllDebtsLive();
    }

    public LiveData<List<Pago>> getAllPaymentsLive() {
        return database.paymentDao().getAllPaymentsLive();
    }

    public LiveData<List<Invitacion>> getAllInvitationsLive() {
        return database.invitationDao().getAllInvitationsLive();
    }

    public LiveData<List<Pago>> getPaymentsForDebtLive(String debtId) {
        return database.paymentDao().getPaymentsForDebtLive(debtId);
    }

    public void login(String identifier, String password, Callback<Usuario> callback) {
        FreyrDatabase.databaseWriteExecutor.execute(() -> {
            String clean = identifier.trim().toLowerCase(Locale.ROOT);
            Usuario user = database.userDao().getUserByIdentifier(clean);
            if (user == null) {
                callback.onError("Usuario o correo no encontrado: " + identifier);
                return;
            }
            if (user.getContraseña() != null && !user.getContraseña().isEmpty() && !user.getContraseña().equals(password)) {
                callback.onError("Contraseña incorrecta.");
                return;
            }
            setLoggedInUser(user.getId());
            callback.onSuccess(user);
        });
    }

    public void register(String fullName, String username, String email, String password, Callback<Usuario> callback) {
        FreyrDatabase.databaseWriteExecutor.execute(() -> {
            String trimmedUsername = username.trim().toLowerCase(Locale.ROOT);
            String trimmedEmail = email.trim().toLowerCase(Locale.ROOT);

            if (fullName.trim().isEmpty()) {
                callback.onError("El nombre completo es requerido.");
                return;
            }
            if (trimmedUsername.isEmpty()) {
                callback.onError("El nombre de usuario es requerido.");
                return;
            }
            if (trimmedEmail.isEmpty()) {
                callback.onError("El correo electrónico es requerido.");
                return;
            }
            if (password == null || password.length() < 6) {
                callback.onError("La contraseña debe tener al menos 6 caracteres.");
                return;
            }

            Usuario existing = database.userDao().getUserByIdentifier(trimmedUsername);
            if (existing != null) {
                callback.onError("El nombre de usuario ya está en uso.");
                return;
            }
            existing = database.userDao().getUserByIdentifier(trimmedEmail);
            if (existing != null) {
                callback.onError("El correo electrónico ya está registrado.");
                return;
            }

            String[] colors = {"#69042A", "#00897B", "#1E88E5", "#8E24AA", "#D81B60", "#3949AB"};
            String randomColor = colors[new Random().nextInt(colors.length)];

            String now = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(new Date());
            Usuario newUser = new Usuario(
                    "user-" + System.currentTimeMillis(),
                    fullName.trim(),
                    trimmedUsername,
                    trimmedEmail,
                    password,
                    randomColor,
                    now
            );

            database.userDao().insertUser(newUser);
            setLoggedInUser(newUser.getId());
            callback.onSuccess(newUser);
        });
    }

    public void updateProfile(String fullName, String username, String email, String password, Callback<Void> callback) {
        FreyrDatabase.databaseWriteExecutor.execute(() -> {
            String currentId = getCurrentUserId();
            Usuario current = database.userDao().getUserById(currentId);
            if (current == null) {
                callback.onError("No hay sesión iniciada.");
                return;
            }

            String trimmedUsername = username.trim().toLowerCase(Locale.ROOT);
            String trimmedEmail = email.trim().toLowerCase(Locale.ROOT);

            if (fullName.trim().isEmpty()) {
                callback.onError("El nombre completo no puede estar vacío.");
                return;
            }
            if (trimmedUsername.isEmpty()) {
                callback.onError("El nombre de usuario no puede estar vacío.");
                return;
            }
            if (trimmedEmail.isEmpty()) {
                callback.onError("El correo electrónico no puede estar vacío.");
                return;
            }

            List<Usuario> users = database.userDao().getAllUsers();
            for (Usuario u : users) {
                if (!u.getId().equals(currentId)) {
                    if (u.getNomUsuario().equalsIgnoreCase(trimmedUsername)) {
                        callback.onError("El nombre de usuario ya está en uso.");
                        return;
                    }
                    if (u.getEmail().equalsIgnoreCase(trimmedEmail)) {
                        callback.onError("El correo electrónico ya está en uso.");
                        return;
                    }
                }
            }

            current.setNombreCompl(fullName.trim());
            current.setNomUsuar(trimmedUsername);
            current.setEmail(trimmedEmail);
            if (password != null && !password.trim().isEmpty()) {
                current.setContraseña(password);
            }

            database.userDao().updateUser(current);
            callback.onSuccess(null);
        });
    }

    public void addDebt(String name, String description, double amount, String assignedUserId, String familyGroupId, Callback<Deuda> callback) {
        FreyrDatabase.databaseWriteExecutor.execute(() -> {
            String currentId = getCurrentUserId();
            if (name == null || name.trim().isEmpty()) {
                callback.onError("El nombre de la deuda no puede estar vacío.");
                return;
            }
            if (amount <= 0.0) {
                callback.onError("El monto debe ser un número positivo.");
                return;
            }

            String now = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(new Date());
            Deuda debt = new Deuda(
                    "debt-" + System.currentTimeMillis(),
                    name.trim(),
                    (description != null && !description.trim().isEmpty()) ? description.trim() : null,
                    amount,
                    assignedUserId,
                    familyGroupId,
                    "pending",
                    now,
                    currentId
            );

            database.debtDao().insertDebt(debt);
            callback.onSuccess(debt);
        });
    }

    public void deleteDebt(String debtId, Callback<Void> callback) {
        FreyrDatabase.databaseWriteExecutor.execute(() -> {
            database.debtDao().deleteDebtById(debtId);
            database.paymentDao().deletePaymentsForDebt(debtId);
            if (callback != null) callback.onSuccess(null);
        });
    }

    public void addPayment(String debtId, double amount, String date, String description, Callback<Pago> callback) {
        FreyrDatabase.databaseWriteExecutor.execute(() -> {
            String currentId = getCurrentUserId();
            if (amount <= 0.0) {
                callback.onError("El monto del abono debe ser mayor a 0.");
                return;
            }

            Deuda debt = database.debtDao().getDebtById(debtId);
            if (debt == null) {
                callback.onError("Deuda no encontrada.");
                return;
            }

            List<Pago> payments = database.paymentDao().getPaymentsForDebt(debtId);
            double currentPaid = 0.0;
            for (Pago p : payments) {
                currentPaid += p.getMonto();
            }

            double newTotalPaid = currentPaid + amount;
            String now = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(new Date());
            Pago payment = new Pago(
                    "pay-" + System.currentTimeMillis(),
                    debtId,
                    currentId,
                    amount,
                    date,
                    (description != null && !description.trim().isEmpty()) ? description.trim() : null,
                    now
            );
            database.paymentDao().insertPayment(payment);

            if (newTotalPaid >= debt.getMonto()) {
                debt.setEstado("paid");
                database.debtDao().updateDebt(debt);
            }

            callback.onSuccess(payment);
        });
    }

    public void deletePayment(String paymentId, String debtId, Callback<Void> callback) {
        FreyrDatabase.databaseWriteExecutor.execute(() -> {
            database.paymentDao().deletePaymentById(paymentId);
            Deuda debt = database.debtDao().getDebtById(debtId);
            if (debt != null) {
                List<Pago> remainingPayments = database.paymentDao().getPaymentsForDebt(debtId);
                double totalPaid = 0.0;
                for (Pago p : remainingPayments) {
                    totalPaid += p.getMonto();
                }
                if (totalPaid < debt.getMonto()) {
                    debt.setEstado("pending");
                    database.debtDao().updateDebt(debt);
                }
            }
            if (callback != null) callback.onSuccess(null);
        });
    }

    public void createFamilyGroup(String name, Callback<Grupofamiliar> callback) {
        FreyrDatabase.databaseWriteExecutor.execute(() -> {
            String currentId = getCurrentUserId();
            if (name == null || name.trim().isEmpty()) {
                callback.onError("El nombre del grupo no puede estar vacío.");
                return;
            }

            String now = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(new Date());
            Grupofamiliar group = new Grupofamiliar(
                    "group-" + System.currentTimeMillis(),
                    name.trim(),
                    currentId,
                    Collections.singletonList(currentId),
                    now
            );

            database.familyGroupDao().insertGroup(group);
            callback.onSuccess(group);
        });
    }

    public void inviteUser(String groupId, String identifier, Callback<Void> callback) {
        FreyrDatabase.databaseWriteExecutor.execute(() -> {
            String currentId = getCurrentUserId();
            Grupofamiliar group = database.familyGroupDao().getGroupById(groupId);
            if (group == null) {
                callback.onError("Grupo familiar no encontrado.");
                return;
            }

            Usuario target = database.userDao().getUserByIdentifier(identifier.trim().toLowerCase(Locale.ROOT));
            if (target == null) {
                callback.onError("No se encontró usuario con el nombre o correo: " + identifier);
                return;
            }

            if (group.getMiembrosIds().contains(target.getId())) {
                callback.onError(target.getNomCompl() + " ya es miembro de este grupo.");
                return;
            }

            List<Invitacion> allInvites = database.invitationDao().getAllInvitations();
            for (Invitacion inv : allInvites) {
                if (inv.getGrupoFamiliarId().equals(groupId) && inv.getIdUsuarioInvitado().equals(target.getId()) && "pending".equals(inv.getEstado())) {
                    callback.onError("Ya hay una invitación pendiente para " + target.getNomCompl());
                    return;
                }
            }

            String now = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(new Date());
            Invitacion invitacion = new Invitacion(
                    "inv-" + System.currentTimeMillis(),
                    groupId,
                    target.getId(),
                    currentId,
                    "pending",
                    now
            );
            database.invitationDao().insertInvitation(invitacion);
            callback.onSuccess(null);
        });
    }

    public void respondToInvitation(String invitationId, boolean accept, Callback<Void> callback) {
        FreyrDatabase.databaseWriteExecutor.execute(() -> {
            Invitacion invite = database.invitationDao().getInvitationById(invitationId);
            if (invite == null) return;

            if (accept) {
                Grupofamiliar group = database.familyGroupDao().getGroupById(invite.getGrupoFamiliarId());
                if (group != null && !group.getMiembrosIds().contains(invite.getIdUsuarioInvitado())) {
                    List<String> updated = new ArrayList<>(group.getMiembrosIds());
                    updated.add(invite.getIdUsuarioInvitado());
                    group.setIdDeMiembros(updated);
                    database.familyGroupDao().updateGroup(group);
                }
            }

            invite.setEstado(accept ? "accepted" : "declined");
            database.invitationDao().updateInvitation(invite);
            if (callback != null) callback.onSuccess(null);
        });
    }

    public static String formatCurrency(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        if (amount % 1.0 == 0.0) {
            format.setMaximumFractionDigits(0);
            format.setMinimumFractionDigits(0);
        } else {
            format.setMaximumFractionDigits(2);
            format.setMinimumFractionDigits(2);
        }
        return format.format(amount);
    }
}