
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

// Representa la tabla "users" en la base de datos Room.
@Entity(tableName = "users")
public class Usuario {

    // =====================================================
    // 3. ATRIBUTOS
    // =====================================================

    // Identificador único del usuario.
    @PrimaryKey
    @NonNull
    private String id;

    // Nombre completo del usuario.
    private String nombreCompl;

    // Nombre de usuario.
    private String nomUsuar;

    // Correo electrónico.
    private String email;

    // Contraseña del usuario.
    private String contraseña;

    // Color asignado al avatar del usuario.
    private String colorAvatar;

    // Fecha de creación de la cuenta.
    private String fechaCreacion;


    // =====================================================
    // 4. CONSTRUCTOR
    // =====================================================

    public Usuario(
            @NonNull String id,
            String nombreCompl,
            String nomUsuar,
            String email,
            String contraseña,
            String colorAvatar,
            String fechaCreacion) {

        this.id = id;
        this.nombreCompl = nombreCompl;
        this.nomUsuar = nomUsuar;
        this.email = email;
        this.contraseña = contraseña;
        this.colorAvatar = colorAvatar;
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
    // 6. MÉTODOS GET Y SET DEL NOMBRE COMPLETO
    // =====================================================

    public String getNombreCompl() {
        return nombreCompl;
    }

    // Método alternativo para obtener el nombre completo.
    public String getNomCompl() {
        return nombreCompl;
    }

    public void setNombreCompl(String nombreCompl) {
        this.nombreCompl = nombreCompl;
    }


    // =====================================================
    // 7. MÉTODOS GET Y SET DEL NOMBRE DE USUARIO
    // =====================================================

    public String getNomUsuar() {
        return nomUsuar;
    }

    public String getNomUsuario() {
        return nomUsuar;
    }

    public void setNomUsuar(String nomUsuar) {
        this.nomUsuar = nomUsuar;
    }


    // =====================================================
    // 8. MÉTODOS GET Y SET DEL CORREO ELECTRÓNICO
    // =====================================================

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    // =====================================================
    // 9. MÉTODOS GET Y SET DE LA CONTRASEÑA
    // =====================================================

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }


    // =====================================================
    // 10. MÉTODOS GET Y SET DEL AVATAR
    // =====================================================

    public String getColorAvatar() {
        return colorAvatar;
    }

    public void setColorAvatar(String colorAvatar) {
        this.colorAvatar = colorAvatar;
    }


    // =====================================================
    // 11. MÉTODOS GET Y SET DE LA FECHA DE CREACIÓN
    // =====================================================

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
