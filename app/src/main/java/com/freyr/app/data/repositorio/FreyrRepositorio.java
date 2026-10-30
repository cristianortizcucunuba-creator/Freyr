
package com.freyr.app.data.repositorio;

// =====================================================
// 1. IMPORTACIONES
// =====================================================

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.freyr.app.data.local.FreyrDatabase;
import com.freyr.app.data.datos.*;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;


// =====================================================
// 2. CLASE PRINCIPAL
// =====================================================

// Gestiona la comunicación entre la aplicación y la base de datos.
// También administra las sesiones y las operaciones de los usuarios.
public class FreyrRepositorio {

    // =====================================================
    // 3. ATRIBUTOS
    // =====================================================

    // Instancia de la base de datos Room.
    private final FreyrDatabase database;

    // Almacena información de la sesión mediante SharedPreferences.
    private final SharedPreferences prefs;

    // Permite observar los cambios del identificador del usuario actual.
    private final MutableLiveData<String> idUsuarioActualObservable =
            new MutableLiveData<>();


    // =====================================================
    // 4. INTERFAZ CALLBACK
    // =====================================================

    // Permite comunicar el resultado de una operación asíncrona.
    public interface Callback<T> {

        // Se ejecuta cuando la operación finaliza correctamente.
        void alTenerExito(T result);

        // Se ejecuta cuando ocurre un error.
        void alOcurrirError(String error);
    }


    // =====================================================
    // 5. CONSTRUCTOR
    // =====================================================

    public FreyrRepositorio(Context context) {

        // Obtiene la instancia de la base de datos.
        this.database = FreyrDatabase.getDatabase(context);

        // Obtiene las preferencias privadas de la aplicación.
        this.prefs = context.getSharedPreferences(
                "freyr_prefs",
                Context.MODE_PRIVATE
        );

        // Recupera la sesión guardada anteriormente.
        this.idUsuarioActualObservable.setValue(
                prefs.getString("id_usuario_actual", null)
        );
    }


    // =====================================================
    // 6. GESTIÓN DE LA SESIÓN DEL USUARIO
    // =====================================================

    // Permite observar los cambios del identificador del usuario actual.
    public LiveData<String> getIdUsuarioActualObservable() {
        return idUsuarioActualObservable;
    }

    // Obtiene el identificador del usuario que tiene la sesión activa.
    public String obtenerIdUsuarioActual() {
        return prefs.getString("id_usuario_actual", null);
    }

    // Guarda o elimina la sesión del usuario.
    public void setLoggedInUser(String idUsuario) {

        if (idUsuario != null) {

            // Guarda el identificador del usuario.
            prefs.edit()
                    .putString("id_usuario_actual", idUsuario)
                    .apply();

        } else {

            // Elimina el identificador para cerrar la sesión.
            prefs.edit()
                    .remove("id_usuario_actual")
                    .apply();
        }

        // Notifica a los observadores sobre el cambio de sesión.
        idUsuarioActualObservable.postValue(idUsuario);
    }


    // =====================================================
    // 7. OBSERVACIÓN DE DATOS DE LA BASE DE DATOS
    // =====================================================

    // Obtiene y observa todos los usuarios registrados.
    public LiveData<List<Usuario>> observarTodosLosUsuarios() {
        return database.userDao().getAllUsersLive();
    }

    // Obtiene y observa todos los grupos familiares.
    public LiveData<List<Grupofamiliar>> observarTodosLosGrupos() {
        return database.familyGroupDao().getAllGroupsLive();
    }

    // Obtiene y observa todas las deudas.
    public LiveData<List<Deuda>> observarTodasLasDeudas() {
        return database.debtDao().getAllDebtsLive();
    }

    // Obtiene y observa todos los pagos.
    public LiveData<List<Pago>> observarTodosLosPagos() {
        return database.paymentDao().getAllPaymentsLive();
    }

    // Obtiene y observa todas las invitaciones.
    public LiveData<List<Invitacion>> observarTodasLasInvitaciones() {
        return database.invitationDao().getAllInvitationsLive();
    }

    // Obtiene y observa los pagos asociados a una deuda específica.
    public LiveData<List<Pago>> observarPagosDeDeuda(String idDeuda) {
        return database.paymentDao().getPaymentsForDebtLive(idDeuda);
    }


    // =====================================================
    // 8. INICIO DE SESIÓN
    // =====================================================

    public void iniciarSesion(
            String identificador,
            String contraseña,
            Callback<Usuario> respuesta) {

        // Ejecuta la operación en un hilo de trabajo.
        FreyrDatabase.databaseWriteExecutor.execute(() -> {

            // Limpia el identificador y lo convierte a minúsculas.
            String clean = identificador.trim().toLowerCase(Locale.ROOT);

            // Busca al usuario por nombre de usuario o correo.
            Usuario user = database.userDao().getUserByIdentifier(clean);

            // Comprueba si el usuario existe.
            if (user == null) {
                respuesta.alOcurrirError(
                        "Usuario o correo no encontrado: " + identificador
                );
                return;
            }

            // Comprueba la contraseña.
            if (user.getContraseña() != null
                    && !user.getContraseña().isEmpty()
                    && !user.getContraseña().equals(contraseña)) {

                respuesta.alOcurrirError("Incorrect contraseña.");
                return;
            }

            // Guarda la sesión del usuario.
            setLoggedInUser(user.getId());

            // Devuelve el usuario encontrado.
            respuesta.alTenerExito(user);
        });
    }


    // =====================================================
    // 9. REGISTRO DE USUARIOS
    // =====================================================

    public void registrarUsuario(
            String nombreCompleto,
            String nombreUsuario,
            String correo,
            String contraseña,
            Callback<Usuario> respuesta) {

        FreyrDatabase.databaseWriteExecutor.execute(() -> {

            // Limpia los datos y convierte los identificadores a minúsculas.
            String nombreUsuarioLimpio =
                    nombreUsuario.trim().toLowerCase(Locale.ROOT);

            String correoLimpio =
                    correo.trim().toLowerCase(Locale.ROOT);


            // -------------------------------------------------
            // 9.1. VALIDACIÓN DE LOS DATOS
            // -------------------------------------------------

            if (nombreCompleto.trim().isEmpty()) {
                respuesta.alOcurrirError(
                        "El nombre completo es obligatorio."
                );
                return;
            }

            if (nombreUsuarioLimpio.isEmpty()) {
                respuesta.alOcurrirError(
                        "El correo electrónico es obligatorio."
                );
                return;
            }

            if (correoLimpio.isEmpty()) {
                respuesta.alOcurrirError(
                        "El correo electrónico es requerido."
                );
                return;
            }

            if (contraseña == null || contraseña.length() < 6) {
                respuesta.alOcurrirError(
                        "La contraseña debe tener al menos 6 caracteres."
                );
                return;
            }


            // -------------------------------------------------
            // 9.2. COMPROBACIÓN DE DATOS REPETIDOS
            // -------------------------------------------------

            Usuario existente =
                    database.userDao().getUserByIdentifier(nombreUsuarioLimpio);

            if (existente != null) {
                respuesta.alOcurrirError(
                        "Ese nombre de usuario ya está ocupado."
                );
                return;
            }

            existente =
                    database.userDao().getUserByIdentifier(correoLimpio);

            if (existente != null) {
                respuesta.alOcurrirError(
                        "Ese correo electrónico ya está registrado."
                );
                return;
            }


            // -------------------------------------------------
            // 9.3. CREACIÓN DEL NUEVO USUARIO
            // -------------------------------------------------

            // Colores disponibles para asignar al avatar.
            String[] colores = {
                    "#69042A",
                    "#00897B",
                    "#1E88E5",
                    "#8E24AA",
                    "#D81B60",
                    "#3949AB"
            };

            // Selecciona un color aleatorio.
            String randomColores =
                    colores[new Random().nextInt(colores.length)];

            // Obtiene la fecha y hora actuales.
            String fechaActual = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    Locale.US
            ).format(new Date());

            // Crea el objeto Usuario con los datos recibidos.
            Usuario nuevoUsuario = new Usuario(
                    "user-" + System.currentTimeMillis(),
                    nombreCompleto.trim(),
                    nombreUsuarioLimpio,
                    correoLimpio,
                    contraseña,
                    randomColores,
                    fechaActual
            );


            // -------------------------------------------------
            // 9.4. ALMACENAMIENTO Y APERTURA DE SESIÓN
            // -------------------------------------------------

            database.userDao().insertUser(nuevoUsuario);

            // Inicia la sesión del usuario recién registrado.
            setLoggedInUser(nuevoUsuario.getId());

            respuesta.alTenerExito(nuevoUsuario);
        });
    }


    // =====================================================
    // 10. ACTUALIZACIÓN DEL PERFIL
    // =====================================================

    public void actualizarPerfil(
            String nombreCompleto,
            String nombreUsuario,
            String email,
            String contraseña,
            Callback<Void> respuesta) {

        FreyrDatabase.databaseWriteExecutor.execute(() -> {

            // Obtiene el identificador de la sesión actual.
            String idActual = obtenerIdUsuarioActual();

            // Busca los datos del usuario en la base de datos.
            Usuario usuarioActual = database.userDao().getUserById(idActual);

            if (usuarioActual == null) {
                respuesta.alOcurrirError("No hay una sesión iniciada.");
                return;
            }

            // Limpia los datos recibidos.
            String nombreUsuarioLimpio =
                    nombreUsuario.trim().toLowerCase(Locale.ROOT);

            String correoLimpio =
                    email.trim().toLowerCase(Locale.ROOT);


            // -------------------------------------------------
            // 10.1. VALIDACIÓN DE LOS CAMPOS
            // -------------------------------------------------

            if (nombreCompleto.trim().isEmpty()) {
                respuesta.alOcurrirError(
                        "El nombre completo no puede estar vacío."
                );
                return;
            }

            if (nombreUsuarioLimpio.isEmpty()) {
                respuesta.alOcurrirError(
                        "El nombre de usuario no puede estar vacío."
                );
                return;
            }

            if (correoLimpio.isEmpty()) {
                respuesta.alOcurrirError(
                        "El correo electrónico no puede estar vacío."
                );
                return;
            }


            // -------------------------------------------------
            // 10.2. COMPROBACIÓN DE DATOS REPETIDOS
            // -------------------------------------------------

            List<Usuario> users = database.userDao().getAllUsers();

            for (Usuario u : users) {

                // Evita comparar al usuario consigo mismo.
                if (!u.getId().equals(idActual)) {

                    if (u.getNomUsuario().equalsIgnoreCase(nombreUsuarioLimpio)) {
                        respuesta.alOcurrirError(
                                "Ese nombre de usuario ya está en uso."
                        );
                        return;
                    }

                    if (u.getEmail().equalsIgnoreCase(correoLimpio)) {
                        respuesta.alOcurrirError(
                                "Ese correo electrónico ya está en uso."
                        );
                        return;
                    }
                }
            }


            // -------------------------------------------------
            // 10.3. ACTUALIZACIÓN DE LOS DATOS
            // -------------------------------------------------

            usuarioActual.setNombreCompl(nombreCompleto.trim());
            usuarioActual.setNomUsuar(nombreUsuarioLimpio);
            usuarioActual.setEmail(correoLimpio);

            // Solo actualiza la contraseña si se proporcionó una nueva.
            if (contraseña != null && !contraseña.trim().isEmpty()) {
                usuarioActual.setContraseña(contraseña);
            }

            // Guarda los cambios en la base de datos.
            database.userDao().updateUser(usuarioActual);

            respuesta.alTenerExito(null);
        });
    }


    // =====================================================
    // 11. AGREGAR UNA DEUDA
    // =====================================================

    public void agregarDeuda(
            String nombre,
            String descripcion,
            double monto,
            String idUsuarioAsignado,
            String idGrupoFamiliar,
            Callback<Deuda> respuesta) {

        FreyrDatabase.databaseWriteExecutor.execute(() -> {

            // Obtiene el usuario que registra la deuda.
            String idActual = obtenerIdUsuarioActual();


            // -------------------------------------------------
            // 11.1. VALIDACIÓN DE LOS DATOS
            // -------------------------------------------------

            if (nombre == null || nombre.trim().isEmpty()) {
                respuesta.alOcurrirError(
                        "El nombre de la deuda es obligatorio."
                );
                return;
            }

            if (monto <= 0.0) {
                respuesta.alOcurrirError(
                        "El monto debe ser mayor que cero."
                );
                return;
            }


            // -------------------------------------------------
            // 11.2. CREACIÓN DE LA DEUDA
            // -------------------------------------------------

            String fechaActual = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    Locale.US
            ).format(new Date());

            Deuda debt = new Deuda(
                    "debt-" + System.currentTimeMillis(),
                    nombre.trim(),
                    (descripcion != null && !descripcion.trim().isEmpty())
                            ? descripcion.trim()
                            : null,
                    monto,
                    idUsuarioAsignado,
                    idGrupoFamiliar,
                    "pending",
                    fechaActual,
                    idActual
            );


            // -------------------------------------------------
            // 11.3. ALMACENAMIENTO
            // -------------------------------------------------

            database.debtDao().insertDebt(debt);

            respuesta.alTenerExito(debt);
        });
    }


    // =====================================================
    // 12. ELIMINAR UNA DEUDA
    // =====================================================

    public void eliminarDeuda(
            String idDeuda,
            Callback<Void> respuesta) {

        FreyrDatabase.databaseWriteExecutor.execute(() -> {

            // Elimina la deuda por su identificador.
            database.debtDao().deleteDebtById(idDeuda);

            // Elimina los pagos asociados a esa deuda.
            database.paymentDao().deletePaymentsForDebt(idDeuda);

            if (respuesta != null) {
                respuesta.alTenerExito(null);
            }
        });
    }


    // =====================================================
    // 13. REGISTRAR UN PAGO
    // =====================================================

    public void registrarPago(
            String idDeuda,
            double monto,
            String fecha,
            String descripcion,
            Callback<Pago> respuesta) {

        FreyrDatabase.databaseWriteExecutor.execute(() -> {

            // Obtiene el identificador del usuario actual.
            String idActual = obtenerIdUsuarioActual();


            // -------------------------------------------------
            // 13.1. VALIDACIÓN DEL MONTO
            // -------------------------------------------------

            if (monto <= 0.0) {
                respuesta.alOcurrirError(
                        "El monto del pago debe ser mayor que cero."
                );
                return;
            }


            // -------------------------------------------------
            // 13.2. BÚSQUEDA DE LA DEUDA
            // -------------------------------------------------

            Deuda deudas = database.debtDao().getDebtById(idDeuda);

            if (deudas == null) {
                respuesta.alOcurrirError("No se encontró la deuda.");
                return;
            }


            // -------------------------------------------------
            // 13.3. CÁLCULO DE LOS PAGOS ANTERIORES
            // -------------------------------------------------

            List<Pago> payments =
                    database.paymentDao().getPaymentsForDebt(idDeuda);

            double totalPagadoActual = 0.0;

            for (Pago p : payments) {
                totalPagadoActual += p.getMonto();
            }

            // Calcula el total pagado después del nuevo abono.
            double nuevoTotalPagado = totalPagadoActual + monto;


            // -------------------------------------------------
            // 13.4. CREACIÓN DEL OBJETO PAGO
            // -------------------------------------------------

            String fechaCreacion = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    Locale.US
            ).format(new Date());

            Pago payment = new Pago(
                    "pago-" + System.currentTimeMillis(),
                    idDeuda,
                    idActual,
                    monto,
                    fecha,
                    (descripcion != null && !descripcion.trim().isEmpty())
                            ? descripcion.trim()
                            : null,
                    fechaCreacion
            );


            // -------------------------------------------------
            // 13.5. ALMACENAMIENTO DEL PAGO
            // -------------------------------------------------

            database.paymentDao().insertPayment(payment);


            // -------------------------------------------------
            // 13.6. ACTUALIZACIÓN DEL ESTADO DE LA DEUDA
            // -------------------------------------------------

            // Si el total pagado alcanza o supera el monto de la deuda,
            // cambia automáticamente su estado a pagado.
            if (nuevoTotalPagado >= deudas.getMonto()) {

                deudas.setEstado("pagado");

                database.debtDao().updateDebt(deudas);
            }

            respuesta.alTenerExito(payment);
        });
    }


    // =====================================================
    // 14. ELIMINAR UN PAGO
    // =====================================================

    public void eliminarPago(
            String idPago,
            String idDeuda,
            Callback<Void> respuesta) {

        FreyrDatabase.databaseWriteExecutor.execute(() -> {

            // Elimina el pago seleccionado.
            database.paymentDao().deletePaymentById(idPago);

            // Busca la deuda relacionada con el pago.
            Deuda deuda = database.debtDao().getDebtById(idDeuda);

            if (deuda != null) {

                // Obtiene los pagos que todavía están registrados.
                List<Pago> pagosRestantes =
                        database.paymentDao().getPaymentsForDebt(idDeuda);

                double totalPagado = 0.0;

                // Suma los montos de los pagos restantes.
                for (Pago p : pagosRestantes) {
                    totalPagado += p.getMonto();
                }

                // Si todavía falta dinero por pagar,
                // cambia el estado de la deuda a pendiente.
                if (totalPagado < deuda.getMonto()) {

                    deuda.setEstado("pendiente");

                    database.debtDao().updateDebt(deuda);
                }
            }

            if (respuesta != null) {
                respuesta.alTenerExito(null);
            }
        });
    }


    // =====================================================
    // 15. CREAR UN GRUPO FAMILIAR
    // =====================================================

    public void crearGrupoFamiliar(
            String nombre,
            Callback<Grupofamiliar> respuesta) {

        FreyrDatabase.databaseWriteExecutor.execute(() -> {

            // Obtiene el identificador del usuario actual.
            String idActual = obtenerIdUsuarioActual();


            // -------------------------------------------------
            // 15.1. VALIDACIÓN DEL NOMBRE
            // -------------------------------------------------

            if (nombre == null || nombre.trim().isEmpty()) {
                respuesta.alOcurrirError(
                        "El nombre del grupo no puede estar vacío."
                );
                return;
            }


            // -------------------------------------------------
            // 15.2. CREACIÓN DEL GRUPO
            // -------------------------------------------------

            String fechaActual = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    Locale.US
            ).format(new Date());

            Grupofamiliar group = new Grupofamiliar(
                    "grupo-" + System.currentTimeMillis(),
                    nombre.trim(),
                    idActual,
                    Collections.singletonList(idActual),
                    fechaActual
            );


            // -------------------------------------------------
            // 15.3. ALMACENAMIENTO DEL GRUPO
            // -------------------------------------------------

            database.familyGroupDao().insertGroup(group);

            respuesta.alTenerExito(group);
        });
    }


    // =====================================================
    // 16. INVITAR A UN USUARIO A UN GRUPO
    // =====================================================

    public void invitarUsuario(
            String idGrupo,
            String identificador,
            Callback<Void> respuesta) {

        FreyrDatabase.databaseWriteExecutor.execute(() -> {

            // Obtiene el identificador del usuario que envía la invitación.
            String idActual = obtenerIdUsuarioActual();

            // Busca el grupo familiar.
            Grupofamiliar group =
                    database.familyGroupDao().getGroupById(idGrupo);

            if (group == null) {
                respuesta.alOcurrirError(
                        "No se encontró el grupo familiar."
                );
                return;
            }


            // -------------------------------------------------
            // 16.1. BÚSQUEDA DEL USUARIO INVITADO
            // -------------------------------------------------

            Usuario usuarioInvitado =
                    database.userDao().getUserByIdentifier(
                            identificador.trim().toLowerCase(Locale.ROOT)
                    );

            if (usuarioInvitado == null) {
                respuesta.alOcurrirError(
                        "No se encontró un usuario con ese nombre o correo: "
                                + identificador
                );
                return;
            }


            // -------------------------------------------------
            // 16.2. COMPROBACIÓN DE MEMBRESÍA
            // -------------------------------------------------

            // Comprueba si el usuario ya pertenece al grupo.
            if (group.getMiembrosIds().contains(usuarioInvitado.getId())) {

                respuesta.alOcurrirError(
                        usuarioInvitado.getNomCompl()
                                + " ya pertenece a este grupo."
                );
                return;
            }


            // -------------------------------------------------
            // 16.3. COMPROBACIÓN DE INVITACIONES PENDIENTES
            // -------------------------------------------------

            List<Invitacion> todasLasInvitacion =
                    database.invitationDao().getAllInvitations();

            for (Invitacion inv : todasLasInvitacion) {

                if (inv.getGrupoFamiliarId().equals(idGrupo)
                        && inv.getIdUsuarioInvitado().equals(usuarioInvitado.getId())
                        && "pendiente".equals(inv.getEstado())) {

                    respuesta.alOcurrirError(
                            "Ya hay una invitación pendiente para: "
                                    + usuarioInvitado.getNomCompl()
                    );
                    return;
                }
            }


            // -------------------------------------------------
            // 16.4. CREACIÓN DE LA INVITACIÓN
            // -------------------------------------------------

            String fechaActual = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    Locale.US
            ).format(new Date());

            Invitacion invitacion = new Invitacion(
                    "inv-" + System.currentTimeMillis(),
                    idGrupo,
                    usuarioInvitado.getId(),
                    idActual,
                    "pendiente",
                    fechaActual
            );


            // -------------------------------------------------
            // 16.5. ALMACENAMIENTO DE LA INVITACIÓN
            // -------------------------------------------------

            database.invitationDao().insertInvitation(invitacion);

            respuesta.alTenerExito(null);
        });
    }


    // =====================================================
    // 17. RESPONDER UNA INVITACIÓN
    // =====================================================

    public void responderInvitacion(
            String idInvitacion,
            boolean aceptar,
            Callback<Void> respuesta) {

        FreyrDatabase.databaseWriteExecutor.execute(() -> {

            // Busca la invitación por su identificador.
            Invitacion invitacion =
                    database.invitationDao().getInvitationById(idInvitacion);

            if (invitacion == null) {
                return;
            }


            // -------------------------------------------------
            // 17.1. AGREGAR AL USUARIO AL GRUPO
            // -------------------------------------------------

            if (aceptar) {

                // Busca el grupo relacionado con la invitación.
                Grupofamiliar grupo =
                        database.familyGroupDao().getGroupById(
                                invitacion.getGrupoFamiliarId()
                        );

                if (grupo != null
                        && !grupo.getMiembrosIds().contains(
                        invitacion.getIdUsuarioInvitado()
                )) {

                    // Crea una nueva lista con los miembros actuales.
                    List<String> updated =
                            new ArrayList<>(grupo.getMiembrosIds());

                    // Agrega al usuario que aceptó la invitación.
                    updated.add(invitacion.getIdUsuarioInvitado());

                    // Actualiza la lista de miembros del grupo.
                    grupo.setIdDeMiembros(updated);

                    // Guarda los cambios en la base de datos.
                    database.familyGroupDao().updateGroup(grupo);
                }
            }


            // -------------------------------------------------
            // 17.2. ACTUALIZACIÓN DEL ESTADO DE LA INVITACIÓN
            // -------------------------------------------------

            // Cambia el estado según la respuesta del usuario.
            invitacion.setEstado(aceptar ? "acepto" : "rechazado");

            database.invitationDao().updateInvitation(invitacion);

            if (respuesta != null) {
                respuesta.alTenerExito(null);
            }
        });
    }


    // =====================================================
    // 18. FORMATEAR MONEDA
    // =====================================================

    // Convierte un valor numérico a un formato de moneda.
    public static String formatearMoneda(double monto) {

        NumberFormat formato =
                NumberFormat.getCurrencyInstance(Locale.US);

        // Si el monto es entero, no muestra decimales.
        if (monto % 1.0 == 0.0) {

            formato.setMaximumFractionDigits(0);
            formato.setMinimumFractionDigits(0);

        } else {

            // Si tiene decimales, muestra hasta dos posiciones.
            formato.setMaximumFractionDigits(2);
            formato.setMinimumFractionDigits(2);
        }

        return formato.format(monto);
    }
}
