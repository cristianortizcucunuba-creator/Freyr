package com.freyr.app.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class Usuario {
    @PrimaryKey
    @NonNull
    private String id;
    private String nombreCompl;
    private String nomUsuar;
    private String email;
    private String contraseña;
    private String colorAvatar;
    private String fechaCreacion;

    public Usuario(@NonNull String id, String nombreCompl, String nomUsuar, String email, String contraseña, String colorAvatar, String fechaCreacion) {
        this.id = id;
        this.nombreCompl = nombreCompl;
        this.nomUsuar = nomUsuar;
        this.email = email;
        this.contraseña = contraseña;
        this.colorAvatar = colorAvatar;
        this.fechaCreacion = fechaCreacion;
    }
    public String getNomUsuar() {
        return nomUsuar;
    }
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getNombreCompl() { return nombreCompl; }
    public String getNomCompl() { return nombreCompl; }
    public void setNombreCompl(String nombreCompl) { this.nombreCompl = nombreCompl; }


    public String getNomUsuario() { return nomUsuar; }
    public void setNomUsuar(String nomUsuar) { this.nomUsuar = nomUsuar; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContraseña() { return contraseña; }
    public void setContraseña(String contraseña) { this.contraseña = contraseña; }

    public String getColorAvatar() { return colorAvatar; }
    public void setColorAvatar(String colorAvatar) { this.colorAvatar = colorAvatar; }

    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
