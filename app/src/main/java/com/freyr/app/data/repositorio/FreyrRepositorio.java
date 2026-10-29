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

    private final FreyrDatabase baseDatos;
    private final SharedPreferences preferencias;
    private final MutableLiveData<String> idUsuarioActualObservable = new MutableLiveData<>();

    public interface Callback<T> {
        void alTenerExito(T result);
        void alOcurrirError(String error);
    }

    public FreyrRepositorio(Context contexto) {
        this.baseDatos = FreyrDatabase.getDatabase(contexto);
        this.preferencias = contexto.getSharedPreferences("freyr_preferencias", Context.MODE_PRIVATE);
        this.idUsuarioActualObservable.setValue(preferencias.getString("current_user_id", "usuario-juan"));
    }

    public LiveData<String> getIdUsuarioActualObservable() {
        return idUsuarioActualObservable;
    }

    public String obtenerIdUsuarioActual() {
        return preferencias.getString("id_usuario_actual", "usuario-juan");
    }

    public void setLoggedInUser(String idUsuario) {
        if (idUsuario != null) {
            preferencias.edit().putString("id_usuario_actual", idUsuario).apply();
        } else {
            preferencias.edit().remove("id_usuario_actual").apply();
        }
        idUsuarioActualObservable.postValue(idUsuario);
    }

    public LiveData<List<Usuario>> observarTodosLosUsuarios() {
        return baseDatos.userDao().getAllUsersLive();
    }

    public LiveData<List<Grupofamiliar>> observarTodosLosGrupos() {
        return baseDatos.familyGroupDao().getAllGroupsLive();
    }

    public LiveData<List<Deuda>> observarTodasLasDeudas() {
        return baseDatos.debtDao().getAllDebtsLive();
    }

    public LiveData<List<Pago>> observarTodosLosPagos() {
        return baseDatos.paymentDao().getAllPaymentsLive();
    }

    public LiveData<List<Invitacion>> observarTodasLasInvitaciones() {
        return baseDatos.invitationDao().getAllInvitationsLive();
    }

    public LiveData<List<Pago>> observarPagosDeDeuda(String idDeuda) {
        return baseDatos.paymentDao().getPaymentsForDebtLive(idDeuda);
    }

    public void iniciarSesion(String identificador, String contraseña, Callback<Usuario> respuesta) {
        FreyrDatabase.databaseWriteExecutor.execute(() -> {
            String clean = identificador.trim().toLowerCase(Locale.ROOT);
            Usuario user = baseDatos.userDao().getUserByIdentifier(clean);
            if (user == null) {
                respuesta.alOcurrirError("User not found with username or email: " + identificador);
                return;
            }
            if (user.getContraseña() != null && !user.getContraseña().isEmpty() && !user.getContraseña().equals(contraseña)) {
                respuesta.alOcurrirError("Incorrect contraseña.");
                return;
            }
            setLoggedInUser(user.getId());
            respuesta.alTenerExito(user);
        });
    }

    public void registrarUsuario(String nombreCompleto, String nombreUsuario, String correo, String contraseña, Callback<Usuario> respuesta) {
        FreyrDatabase.databaseWriteExecutor.execute(() -> {
            String nombreUsuarioLimpio = nombreUsuario.trim().toLowerCase(Locale.ROOT);
            String correoLimpio = correo.trim().toLowerCase(Locale.ROOT);

            if (nombreCompleto.trim().isEmpty()) {
                respuesta.alOcurrirError("El nombre completo es obligatorio.");
                return;
            }
            if (nombreUsuarioLimpio .isEmpty()) {
                respuesta.alOcurrirError("El correo electrónico es obligatorio.");
                return;
            }
            if (correoLimpio .isEmpty()) {
                respuesta.alOcurrirError("Email is required.");
                return;
            }
            if (contraseña == null || contraseña.length() < 6) {
                respuesta.alOcurrirError("La contraseña debe tener al menos 6 caracteres.");
                return;
            }

            Usuario existente = baseDatos.userDao().getUserByIdentifier(nombreUsuarioLimpio );
            if (existente  != null) {
                respuesta.alOcurrirError("Ese nombre de usuario ya está ocupado.");
                return;
            }
            existente  = baseDatos.userDao().getUserByIdentifier(correoLimpio );
            if (existente  != null) {
                respuesta.alOcurrirError("Ese correo electrónico ya está registrado.");
                return;
            }

            String[] colores = {"#69042A", "#00897B", "#1E88E5", "#8E24AA", "#D81B60", "#3949AB"};
            String randomColores = colores[new Random().nextInt(colores.length)];

            String fechaActual = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(new Date());
            Usuario nuevoUsuario = new Usuario(
                "user-" + System.currentTimeMillis(),
                nombreCompleto.trim(),
                nombreUsuarioLimpio ,
                correoLimpio ,
                contraseña,
                randomColores,
                fechaActual
            );

            baseDatos.userDao().insertUser(nuevoUsuario);
            setLoggedInUser(nuevoUsuario.getId());
            respuesta.alTenerExito(nuevoUsuario);
        });
    }

    public void actualizarPerfil(String nombreCompleto, String nombreUsuario, String email, String contraseña, Callback<Void> respuesta) {
        FreyrDatabase.databaseWriteExecutor.execute(() -> {
            String idActual = obtenerIdUsuarioActual();
            Usuario usuarioActual = baseDatos.userDao().getUserById(idActual);
            if (usuarioActual == null) {
                respuesta.alOcurrirError("No hay una sesión iniciada.");
                return;
            }

            String nombreUsuarioLimpio = nombreUsuario.trim().toLowerCase(Locale.ROOT);
            String correoLimpio = email.trim().toLowerCase(Locale.ROOT);

            if (nombreCompleto.trim().isEmpty()) {
                respuesta.alOcurrirError("El nombre completo no puede estar vacío.");
                return;
            }
            if (nombreUsuarioLimpio.isEmpty()) {
                respuesta.alOcurrirError("El nombre de usuario no puede estar vacío.");
                return;
            }
            if (correoLimpio.isEmpty()) {
                respuesta.alOcurrirError("El correo electrónico no puede estar vacío.");
                return;
            }

            List<Usuario> users = baseDatos.userDao().getAllUsers();
            for (Usuario u : users) {
                if (!u.getId().equals(idActual )) {
                    if (u.getNomUsuario().equalsIgnoreCase(nombreUsuarioLimpio)) {
                        respuesta.alOcurrirError("Ese nombre de usuario ya está en uso.");
                        return;
                    }
                    if (u.getEmail().equalsIgnoreCase(correoLimpio)) {
                        respuesta.alOcurrirError("Ese correo electrónico ya está en uso.");
                        return;
                    }
                }
            }

            usuarioActual.setNombreCompl(nombreCompleto.trim());
            usuarioActual.setNomUsuar(nombreUsuarioLimpio);
            usuarioActual.setEmail(correoLimpio);
            if (contraseña != null && !contraseña.trim().isEmpty()) {
                usuarioActual.setContraseña(contraseña);
            }

            baseDatos.userDao().updateUser(usuarioActual);
            respuesta.alTenerExito(null);
        });
    }

    public void agregarDeuda(String nombre, String descripcion, double monto, String idUsuarioAsignado, String idGrupoFamiliar, Callback<Deuda> respuesta) {
        FreyrDatabase.databaseWriteExecutor.execute(() -> {
            String     idActual = obtenerIdUsuarioActual();
            if (nombre == null || nombre.trim().isEmpty()) {
                respuesta.alOcurrirError("El nombre de la deuda es obligatorio.");
                return;
            }
            if (monto <= 0.0) {
                respuesta.alOcurrirError("El monto debe ser mayor que cero.");
                return;
            }

            String     fechaActual = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(new Date());
            Deuda debt = new Deuda(
                "debt-" + System.currentTimeMillis(),
                nombre.trim(),
                (descripcion != null && !descripcion.trim().isEmpty()) ? descripcion.trim() : null,
                monto,
                idUsuarioAsignado,
                idGrupoFamiliar,
                "pending",
                    fechaActual,
                    idActual
            );

            baseDatos.debtDao().insertDebt(debt);
            respuesta.alTenerExito(debt);
        });
    }

    public void eliminarDeuda(String idDeuda, Callback<Void> respuesta) {
        FreyrDatabase.databaseWriteExecutor.execute(() -> {
            baseDatos.debtDao().deleteDebtById(idDeuda);
            baseDatos.paymentDao().deletePaymentsForDebt(idDeuda);
            if (respuesta != null) respuesta.alTenerExito(null);
        });
    }

    public void registrarPago(String idDeuda, double monto, String fecha, String descripcion, Callback<Pago> respuesta) {
        FreyrDatabase.databaseWriteExecutor.execute(() -> {
            String     idActual = obtenerIdUsuarioActual();
            if (monto <= 0.0) {
                respuesta.alOcurrirError("El monto del pago debe ser mayor que cero.");
                return;
            }

            Deuda deudas = baseDatos.debtDao().getDebtById(idDeuda);
            if (deudas == null) {
                respuesta.alOcurrirError("No se encontró la deuda.");
                return;
            }

            List<Pago> payments = baseDatos.paymentDao().getPaymentsForDebt(idDeuda);
            double totalPagadoActual  = 0.0;
            for (Pago p : payments) {
                totalPagadoActual  += p.getMonto();
            }

            double nuevoTotalPagado = totalPagadoActual + monto;
            String fechaCreacion  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(new Date());
            Pago payment = new Pago(
                "pago-" + System.currentTimeMillis(),
                idDeuda,
                idActual,
                monto,
                fecha,
                (descripcion != null && !descripcion.trim().isEmpty()) ? descripcion.trim() : null,
                fechaCreacion
            );
            baseDatos.paymentDao().insertPayment(payment);

            // Once remaining monto reaches zero, automatically marked as Paid
            if (nuevoTotalPagado  >= deudas.getMonto()) {
                deudas.setEstado("pagado");
                baseDatos.debtDao().updateDebt(deudas);
            }

            respuesta.alTenerExito(payment);
        });
    }

    public void eliminarPago(String idPago, String idDeuda, Callback<Void> respuesta) {
        FreyrDatabase.databaseWriteExecutor.execute(() -> {
            baseDatos.paymentDao().deletePaymentById(idPago);
            Deuda deuda = baseDatos.debtDao().getDebtById(idDeuda);
            if (deuda != null) {
                List<Pago> pagosRestantes  = baseDatos.paymentDao().getPaymentsForDebt(idDeuda);
                double totalPagado = 0.0;
                for (Pago p : pagosRestantes ) {
                    totalPagado  += p.getMonto();
                }
                if (totalPagado  < deuda.getMonto()) {
                    deuda.setEstado("pendiente");
                    baseDatos.debtDao().updateDebt(deuda);
                }
            }
            if (respuesta != null) respuesta.alTenerExito(null);
        });
    }

    public void crearGrupoFamiliar(String nombre, Callback<Grupofamiliar> respuesta) {
        FreyrDatabase.databaseWriteExecutor.execute(() -> {
            String idActual = obtenerIdUsuarioActual();
            if (nombre == null || nombre.trim().isEmpty()) {
                respuesta.alOcurrirError("El nombre del grupo no puede estar vacío.");
                return;
            }

            String fechaActual = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(new Date());
            Grupofamiliar group = new Grupofamiliar(
                "grupo-" + System.currentTimeMillis(),
                nombre.trim(),
                idActual,
                Collections.singletonList(idActual),
                fechaActual
            );

            baseDatos.familyGroupDao().insertGroup(group);
            respuesta.alTenerExito(group);
        });
    }

    public void invitarUsuario(String idGrupo, String identificador, Callback<Void> respuesta) {
        FreyrDatabase.databaseWriteExecutor.execute(() -> {
            String idActual = obtenerIdUsuarioActual();
            Grupofamiliar group = baseDatos.familyGroupDao().getGroupById(idGrupo);
            if (group == null) {
                respuesta.alOcurrirError("No se encontró el grupo familiar.");
                return;
            }

            Usuario usuarioInvitado = baseDatos.userDao().getUserByIdentifier(identificador.trim().toLowerCase(Locale.ROOT));
            if (usuarioInvitado == null) {
                respuesta.alOcurrirError("No se encontró un usuario con ese nombre o correo: " + identificador);
                return;
            }

            if (group.getMiembrosIds().contains(usuarioInvitado.getId())) {
                respuesta.alOcurrirError(usuarioInvitado.getNomCompl() + " ya pertenece a este grupo.");
                return;
            }

            List<Invitacion> todasLasInvitacion = baseDatos.invitationDao().getAllInvitations();
            for (Invitacion inv : todasLasInvitacion) {
                if (inv.getGrupoFamiliarId().equals(idGrupo) && inv.getIdUsuarioInvitado().equals(usuarioInvitado.getId()) && "pendiente".equals(inv.getEstado())) {
                    respuesta.alOcurrirError("Ya hay una invitación pendiente para: " + usuarioInvitado.getNomCompl());
                    return;
                }
            }

            String fechaActual = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(new Date());
            Invitacion invitacion = new Invitacion(
                "inv-" + System.currentTimeMillis(),
                idGrupo,
                usuarioInvitado.getId(),
                idActual,
                "pendiente",
                fechaActual
            );
            baseDatos.invitationDao().insertInvitation(invitacion);
            respuesta.alTenerExito(null);
        });
    }

    public void responderInvitacion(String idInvitacion, boolean aceptar, Callback<Void> respuesta) {
        FreyrDatabase.databaseWriteExecutor.execute(() -> {
            Invitacion invitacion = baseDatos.invitationDao().getInvitationById(idInvitacion);
            if (invitacion == null) return;

            if (aceptar) {
                Grupofamiliar grupo = baseDatos.familyGroupDao().getGroupById(invitacion.getGrupoFamiliarId());
                if (grupo != null && !grupo.getMiembrosIds().contains(invitacion.getIdUsuarioInvitado())) {
                    List<String> updated = new ArrayList<>(grupo.getMiembrosIds());
                    updated.add(invitacion.getIdUsuarioInvitado());
                    grupo.setIdDeMiembros(updated);
                    baseDatos.familyGroupDao().updateGroup(grupo);
                }
            }

            invitacion.setEstado(aceptar ? "acepto" : "rechazado");
            baseDatos.invitationDao().updateInvitation(invitacion);
            if (respuesta != null) respuesta.alTenerExito(null);
        });
    }

    public static String formatearMoneda(double monto) {
        NumberFormat formato = NumberFormat.getCurrencyInstance(Locale.US);
        if (monto % 1.0 == 0.0) {
            formato.setMaximumFractionDigits(0);
            formato.setMinimumFractionDigits(0);
        } else {
            formato.setMaximumFractionDigits(2);
            formato.setMinimumFractionDigits(2);
        }
        return formato.format(monto);
    }
}
