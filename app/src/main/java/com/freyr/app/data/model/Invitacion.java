package com.freyr.app.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "invitations")
public class Invitacion {
    @PrimaryKey
    @NonNull
    private String id;
    private String idGrupoFamiliar;
    private String idUsuarioInvitado;
    private String idUsuarioInvitador;
    private String estado; // "pending", "accepted", "declined"
    private String fechaCreacion;

    public Invitacion(@NonNull String id, String idGrupoFamiliar, String idUsuarioInvitado, String idUsuarioInvitador, String estado, String fechaCreacion) {
        this.id = id;
        this.idGrupoFamiliar = idGrupoFamiliar;
        this.idUsuarioInvitado = idUsuarioInvitado;
        this.idUsuarioInvitador = idUsuarioInvitador;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getIdGrupoFamiliar() { return idGrupoFamiliar; }
    public String getGrupoFamiliarId() { return idGrupoFamiliar; }
    public void setIdGrupoFamiliar(String idGrupoFamiliar) { this.idGrupoFamiliar = idGrupoFamiliar; }

    public String getIdUsuarioInvitado() { return idUsuarioInvitado; }
    public void setIdUsuarioInvitado(String idUsuarioInvitado) { this.idUsuarioInvitado = idUsuarioInvitado; }

    public String getIdUsuarioInvitador() { return idUsuarioInvitador; }
    public String getUsuarioInvitadorId() { return idUsuarioInvitador; }
    public void setIdUsuarioInvitador(String idUsuarioInvitador) { this.idUsuarioInvitador = idUsuarioInvitador; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
