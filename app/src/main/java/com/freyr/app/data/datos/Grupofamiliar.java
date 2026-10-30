
package com.freyr.app.data.datos;

// =====================================================
// 1. IMPORTACIONES
// =====================================================

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;


// =====================================================
// 2. DEFINICIÓN DE LA ENTIDAD
// =====================================================

// Indica que esta clase representa una tabla de Room.
@Entity(tableName = "family_groups")
public class Grupofamiliar {

    // =====================================================
    // 3. ATRIBUTOS
    // =====================================================

    // Identificador único del grupo.
    @PrimaryKey
    @NonNull
    private String id;

    // Nombre del grupo familiar.
    private String nombr;

    // Identificador del usuario que creó el grupo.
    private String idCreador;

    // Lista con los identificadores de los miembros.
    private List<String> idDeMiembros;

    // Fecha en la que se creó el grupo.
    private String fechaCreacion;


    // =====================================================
    // 4. CONSTRUCTOR
    // =====================================================

    public Grupofamiliar(
            @NonNull String id,
            String nombr,
            String idCreador,
            List<String> idDeMiembros,
            String fechaCreacion) {

        this.id = id;
        this.nombr = nombr;
        this.idCreador = idCreador;
        this.idDeMiembros = idDeMiembros;
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
    // 6. MÉTODOS GET Y SET DEL NOMBRE
    // =====================================================

    public String getNombr() {
        return nombr;
    }

    // Método alternativo para obtener el nombre.
    public String getNombre() {
        return nombr;
    }

    public void setNombr(String nombr) {
        this.nombr = nombr;
    }


    // =====================================================
    // 7. MÉTODOS GET Y SET DEL CREADOR
    // =====================================================

    public String getIdCreador() {
        return idCreador;
    }

    // Método alternativo para obtener el identificador del creador.
    public String getCreadorId() {
        return idCreador;
    }

    public void setIdCreador(String idCreador) {
        this.idCreador = idCreador;
    }


    // =====================================================
    // 8. MÉTODOS GET Y SET DE LOS MIEMBROS
    // =====================================================

    public List<String> getIdDeMiembros() {
        return idDeMiembros;
    }

    // Método alternativo para obtener los identificadores de los miembros.
    public List<String> getMiembrosIds() {
        return idDeMiembros;
    }

    public void setIdDeMiembros(List<String> idDeMiembros) {
        this.idDeMiembros = idDeMiembros;
    }


    // =====================================================
    // 9. MÉTODOS GET Y SET DE LA FECHA
    // =====================================================

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
