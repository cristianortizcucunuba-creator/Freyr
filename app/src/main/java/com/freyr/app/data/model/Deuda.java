package com.freyr.app.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "debts")
public class Deuda {
    @PrimaryKey
    @NonNull
    private String id;
    private String nombre;
    private String descripcion;
    private double monto;
    private String idUsuarioAsignado; // null means whole family or unassigned
    private String idGrupoFamiliar; // null means personal debt
    private String estado; // "pending" or "paid"
    private String fechaCreacion;
    private String creadoPor;

    public Deuda(@NonNull String id, String nombre, String descripcion, double monto, String idUsuarioAsignado, String idGrupoFamiliar, String estado, String fechaCreacion, String creadoPor) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.monto = monto;
        this.idUsuarioAsignado = idUsuarioAsignado;
        this.idGrupoFamiliar = idGrupoFamiliar;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.creadoPor = creadoPor;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public String getIdUsuarioAsignado() { return idUsuarioAsignado; }
    public void setIdUsuarioAsignado(String idUsuarioAsignado) { this.idUsuarioAsignado = idUsuarioAsignado; }

    public String getIdGrupoFamiliar() { return idGrupoFamiliar; }
    public String getGrupoFamiliarId() { return idGrupoFamiliar; }
    public void setIdGrupoFamiliar(String idGrupoFamiliar) { this.idGrupoFamiliar = idGrupoFamiliar; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getCreadoPor() { return creadoPor; }
    public void setCreadoPor(String creadoPor) { this.creadoPor = creadoPor; }
}
