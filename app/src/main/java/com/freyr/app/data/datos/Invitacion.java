
package com.freyr.app.data.datos;

// =====================================================
// 1. IMPORTACIONES
// =====================================================

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


// =====================================================
// 2. DEFINICIÓN DE LA ENTIDAD
// =====================================================

// Representa la tabla "invitations" en la base de datos Room.
@Entity(tableName = "invitations")
public class Invitacion {

    // =====================================================
    // 3. ATRIBUTOS
    // =====================================================

    // Identificador único de la invitación.
    @PrimaryKey
    @NonNull
    private String id;

    // Identificador del grupo familiar al que pertenece.
    private String idGrupoFamiliar;

    // Identificador del usuario que recibe la invitación.
    private String idUsuarioInvitado;

    // Identificador del usuario que envía la invitación.
    private String idUsuarioInvitador;

    // Estado actual: "pendiente", "acepto" o "rechazado".
    private String estado;

    // Fecha de creación de la invitación.
    private String fechaCreacion;


    // =====================================================
    // 4. CONSTRUCTOR
    // =====================================================

    public Invitacion(
            @NonNull String id,
            String idGrupoFamiliar,
            String idUsuarioInvitado,
            String idUsuarioInvitador,
            String estado,
            String fechaCreacion) {

        this.id = id;
        this.idGrupoFamiliar = idGrupoFamiliar;
        this.idUsuarioInvitado = idUsuarioInvitado;
        this.idUsuarioInvitador = idUsuarioInvitador;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }


    // =====================================================
    // 5. MÉTODOS GET Y SET DEL IDENTIFICADOR
    // =====================================================

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }


    // =====================================================
    // 6. MÉTODOS GET Y SET DEL GRUPO FAMILIAR
    // =====================================================

    public String getIdGrupoFamiliar() {
        return idGrupoFamiliar;
    }

    // Método alternativo para obtener el identificador del grupo.
    public String getGrupoFamiliarId() {
        return idGrupoFamiliar;
    }

    public void setIdGrupoFamiliar(String idGrupoFamiliar) {
        this.idGrupoFamiliar = idGrupoFamiliar;
    }


    // =====================================================
    // 7. MÉTODOS GET Y SET DEL USUARIO INVITADO
    // =====================================================

    public String getIdUsuarioInvitado() {
        return idUsuarioInvitado;
    }

    public void setIdUsuarioInvitado(String idUsuarioInvitado) {
        this.idUsuarioInvitado = idUsuarioInvitado;
    }


    // =====================================================
    // 8. MÉTODOS GET Y SET DEL USUARIO INVITADOR
    // =====================================================

    public String getIdUsuarioInvitador() {
        return idUsuarioInvitador;
    }

    // Método alternativo para obtener el identificador del invitador.
    public String getUsuarioInvitadorId() {
        return idUsuarioInvitador;
    }

    public void setIdUsuarioInvitador(String idUsuarioInvitador) {
        this.idUsuarioInvitador = idUsuarioInvitador;
    }


    // =====================================================
    // 9. MÉTODOS GET Y SET DEL ESTADO
    // =====================================================

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }


    // =====================================================
    // 10. MÉTODOS GET Y SET DE LA FECHA
    // =====================================================

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
