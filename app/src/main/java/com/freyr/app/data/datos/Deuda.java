
package com.freyr.app.data.datos;

// 1. IMPORTACIONES
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


// 2. DEFINICIÓN DE LA ENTIDAD
// Representa una deuda dentro de la base de datos.
// Room utilizará esta clase para crear la tabla "debts".
@Entity(tableName = "debts")
public class Deuda {


    // 3. ATRIBUTOS

    // Identificador único de la deuda.
    @PrimaryKey
    @NonNull
    private String id;

    // Nombre y descripción de la deuda.
    private String nombre;
    private String descripcion;

    // Monto total de la deuda.
    private double monto;

    // Usuario al que se le asignó la deuda.
    // Puede ser null si pertenece a toda la familia
    // o todavía no tiene un usuario asignado.
    private String idUsuarioAsignado;

    // Identificador del grupo familiar.
    // Puede ser null si se trata de una deuda personal.
    private String idGrupoFamiliar;

    // Estado actual de la deuda.
    // Ejemplos: "pending" o "paid".
    private String estado;

    // Fecha de creación y usuario que registró la deuda.
    private String fechaCreacion;
    private String creadoPor;


    // 4. CONSTRUCTOR
    public Deuda(
            @NonNull String id,
            String nombre,
            String descripcion,
            double monto,
            String idUsuarioAsignado,
            String idGrupoFamiliar,
            String estado,
            String fechaCreacion,
            String creadoPor) {

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


    // 5. MÉTODOS GETTERS Y SETTERS

    // 5.1. IDENTIFICADOR
    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }


    // 5.2. NOMBRE
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }


    // 5.3. DESCRIPCIÓN
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }


    // 5.4. MONTO
    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }


    // 5.5. USUARIO ASIGNADO
    public String getIdUsuarioAsignado() {
        return idUsuarioAsignado;
    }

    public void setIdUsuarioAsignado(String idUsuarioAsignado) {
        this.idUsuarioAsignado = idUsuarioAsignado;
    }


    // 5.6. GRUPO FAMILIAR
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


    // 5.7. ESTADO
    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }


    // 5.8. FECHA DE CREACIÓN
    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }


    // 5.9. CREADOR DE LA DEUDA
    public String getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = creadoPor;
    }
}
