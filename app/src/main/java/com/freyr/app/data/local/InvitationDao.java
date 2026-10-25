package com.freyr.app.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.freyr.app.data.model.Invitation;
import java.util.List;

@Dao
public interface InvitationDao {
    @Query("SELECT * FROM invitations ORDER BY createdAt DESC")
    LiveData<List<Invitation>> getAllInvitationsLive();

    @Query("SELECT * FROM invitations ORDER BY createdAt DESC")
    List<Invitation> getAllInvitations();

    @Query("SELECT * FROM invitations WHERE invitedUserId = :userId AND status = 'pending'")
    LiveData<List<Invitation>> getPendingInvitationsForUserLive(String userId);

    @Query("SELECT * FROM invitations WHERE id = :id LIMIT 1")
    Invitation getInvitationById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertInvitation(Invitation invitation);

    @Update
    void updateInvitation(Invitation invitation);
}
