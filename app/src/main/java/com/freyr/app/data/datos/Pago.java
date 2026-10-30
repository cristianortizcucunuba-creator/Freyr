
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

// Representa la tabla "payments" en la base de datos Room.
@Entity(tableName = "payments")
public class Pago {

    // =====================================================
    // 3. ATRIBUTOS
    // =====================================================

    // Identificador único del pago.
    @PrimaryKey
    @NonNull
    private String id;

    // Identificador de la deuda a la que pertenece el pago.
    private String idDeuda;

    // Identificador del usuario que realizó el pago.
    private String idUsuario;

    // Cantidad de dinero abonada.
    private double mont;

    // Fecha en la que se realizó el pago.
    private String fecha;

    // Descripción del pago.
    private String descripciones;

    // Fecha en la que se registró el pago.
    private String fechaCreado;


    // =====================================================
    // 4. CONSTRUCTOR
    // =====================================================

    public Pago(
            @NonNull String id,
            String idDeuda,
            String idUsuario,
            double mont,
            String fecha,
            String descripciones,
            String fechaCreado) {

        this.id = id;
        this.idDeuda = idDeuda;
        this.idUsuario = idUsuario;
        this.mont = mont;
        this.fecha = fecha;
        this.descripciones = descripciones;
        this.fechaCreado = fechaCreado;
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
    // 6. MÉTODOS GET Y SET DE LA DEUDA
    // =====================================================

    public String getIdDeuda() {
        return idDeuda;
    }

    // Método alternativo para obtener el identificador de la deuda.
    public String getDeudaId() {
        return idDeuda;
    }

    public void setIdDeuda(String idDeuda) {
        this.idDeuda = idDeuda;
    }


    // =====================================================
    // 7. MÉTODOS GET Y SET DEL USUARIO
    // =====================================================

    public String getIdUsuario() {
        return idUsuario;
    }

    // Método alternativo para obtener el identificador del usuario.
    public String getUsuarioId() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }


    // =====================================================
    // 8. MÉTODOS GET Y SET DEL MONTO
    // =====================================================

    public double getMont() {
        return mont;
    }

    // Método alternativo para obtener el monto.
    public double getMonto() {
        return mont;
    }

    public void setMont(double mont) {
        this.mont = mont;
    }


    // =====================================================
    // 9. MÉTODOS GET Y SET DE LA FECHA
    // =====================================================

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }


    // =====================================================
    // 10. MÉTODOS GET Y SET DE LA DESCRIPCIÓN
    // =====================================================

    public String getDescripciones() {
        return descripciones;
    }

    // Método alternativo para obtener la descripción.
    public String getDescripcion() {
        return descripciones;
    }

    public void setDescripciones(String descripciones) {
        this.descripciones = descripciones;
    }


    // =====================================================
    // 11. MÉTODOS GET Y SET DE LA FECHA DE REGISTRO
    // =====================================================

    public String getFechaCreado() {
        return fechaCreado;
    }

    public void setFechaCreado(String fechaCreado) {
        this.fechaCreado = fechaCreado;
    }
}
