package com.freyr.app.data.datos;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "payments")
public class Pago {
    @PrimaryKey
    @NonNull
    private String id;
    private String idDeuda;
    private String idUsuario;
    private double mont;
    private String fecha;
    private String descripciones;
    private String fechaCreado;

    public Pago(@NonNull String id, String idDeuda, String idUsuario, double mont, String fecha, String descripciones, String fechaCreado) {
        this.id = id;
        this.idDeuda = idDeuda;
        this.idUsuario = idUsuario;
        this.mont = mont;
        this.fecha = fecha;
        this.descripciones = descripciones;
        this.fechaCreado = fechaCreado;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getIdDeuda() { return idDeuda; }
    public String getDeudaId() { return idDeuda; }
    public void setIdDeuda(String idDeuda) { this.idDeuda = idDeuda; }

    public String getIdUsuario() { return idUsuario; }
    public String getUsuarioId() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public double getMont() { return mont; }
    public double getMonto() { return mont; }
    public void setMont(double mont) { this.mont = mont; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getDescripciones() { return descripciones; }
    public String getDescripcion() { return descripciones; }
    public void setDescripciones(String descripciones) { this.descripciones = descripciones; }

    public String getFechaCreado() { return fechaCreado; }
    public void setFechaCreado(String fechaCreado) { this.fechaCreado = fechaCreado; }
}
