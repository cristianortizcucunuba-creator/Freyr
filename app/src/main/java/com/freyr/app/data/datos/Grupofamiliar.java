package com.freyr.app.data.datos;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.List;

@Entity(tableName = "family_groups")
public class Grupofamiliar {
    @PrimaryKey
    @NonNull
    private String id;
    private String nombr;
    private String idCreador;
    private List<String> idDeMiembros;
    private String fechaCreacion;

    public Grupofamiliar(@NonNull String id, String nombr, String idCreador, List<String> idDeMiembros, String fechaCreacion) {
        this.id = id;
        this.nombr = nombr;
        this.idCreador = idCreador;
        this.idDeMiembros = idDeMiembros;
        this.fechaCreacion = fechaCreacion;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getNombr() { return nombr; }

    public String getNombre() { return nombr; }
    public void setNombr(String nombr) { this.nombr = nombr; }

    public String getIdCreador() { return idCreador; }
    public String getCreadorId() { return idCreador; }
    public void setIdCreador(String idCreador) { this.idCreador = idCreador; }

    public List<String> getIdDeMiembros() { return idDeMiembros; }
    public List<String> getMiembrosIds() { return idDeMiembros; }
    public void setIdDeMiembros(List<String> idDeMiembros) { this.idDeMiembros = idDeMiembros; }

    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
