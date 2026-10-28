package com.freyr.app.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.freyr.app.data.model.Grupofamiliar;

import java.util.List;

@Dao
public interface FamilyGroupDao {
    @Query("SELECT * FROM Grupofamiliar")
    LiveData<List<Grupofamiliar>> getAllGroupsLive();

    @Query("SELECT * FROM Grupofamiliar")
    List<Grupofamiliar> getAllGroups();

    @Query("SELECT * FROM Grupofamiliar WHERE id = :id LIMIT 1")
    Grupofamiliar getGroupById(String id);

    @Query("SELECT * FROM Grupofamiliar WHERE id = :id LIMIT 1")
    LiveData<Grupofamiliar> getGroupByIdLive(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGroup(Grupofamiliar group);

    @Update
    void updateGroup(Grupofamiliar group);

    @Delete
    void deleteGroup(Grupofamiliar group);
}
