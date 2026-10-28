package com.freyr.app.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.freyr.app.data.model.Usuario;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM Usuario")
    LiveData<List<Usuario>> getAllUsersLive();

    @Query("SELECT * FROM Usuario")
    List<Usuario> getAllUsers();

    @Query("SELECT * FROM Usuario WHERE id = :id LIMIT 1")
    Usuario getUserById(String id);

    @Query("SELECT * FROM Usuario WHERE id = :id LIMIT 1")
    LiveData<Usuario> getUserByIdLive(String id);

    @Query("SELECT * FROM Usuario WHERE LOWER(email) = LOWER(:identifier) OR LOWER(username) = LOWER(:identifier) LIMIT 1")
    Usuario getUserByIdentifier(String identifier);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(Usuario user);

    @Update
    void updateUser(Usuario user);
}
