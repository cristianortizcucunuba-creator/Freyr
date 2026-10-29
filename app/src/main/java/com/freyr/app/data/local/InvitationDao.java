package com.freyr.app.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import com.freyr.app.data.model.Invitacion;

import java.util.List;

@Dao
public interface InvitationDao {
    @Query("SELECT * FROM invitations ORDER BY createdAt DESC")
    LiveData<List<Invitacion>> getAllInvitationsLive();

    @Query("SELECT * FROM invitations ORDER BY createdAt DESC")
    List<Invitacion> getAllInvitations();

    @Query("SELECT * FROM invitations WHERE invitedUserId = :userId AND status = 'pending'")
    LiveData<List<Invitacion>> getPendingInvitationsForUserLive(String userId);

    @Query("SELECT * FROM invitations WHERE id = :id LIMIT 1")
    Invitacion getInvitationById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertInvitation(Invitacion invitacion);

    @Update
    void updateInvitation(Invitacion invitacion);
}
