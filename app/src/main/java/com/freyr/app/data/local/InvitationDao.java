package com.freyr.app.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import com.freyr.app.data.datos.Invitacion;

import java.util.List;

@Dao
public interface InvitationDao {
    @Query("SELECT * FROM invitations ORDER BY fechaCreacion DESC")
    LiveData<List<Invitacion>> getAllInvitationsLive();

    @Query("SELECT * FROM invitations ORDER BY fechaCreacion DESC")
    List<Invitacion> getAllInvitations();

    @Query("SELECT * FROM invitations WHERE idUsuarioInvitado = :userId AND estado = 'pendiente'")
    LiveData<List<Invitacion>> getPendingInvitationsForUserLive(String userId);

    @Query("SELECT * FROM invitations WHERE id = :id LIMIT 1")
    Invitacion getInvitationById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertInvitation(Invitacion invitacion);

    @Update
    void updateInvitation(Invitacion invitacion);
}
