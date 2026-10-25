package com.freyr.app.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.freyr.app.data.model.FamilyGroup;
import java.util.List;

@Dao
public interface FamilyGroupDao {
    @Query("SELECT * FROM family_groups")
    LiveData<List<FamilyGroup>> getAllGroupsLive();

    @Query("SELECT * FROM family_groups")
    List<FamilyGroup> getAllGroups();

    @Query("SELECT * FROM family_groups WHERE id = :id LIMIT 1")
    FamilyGroup getGroupById(String id);

    @Query("SELECT * FROM family_groups WHERE id = :id LIMIT 1")
    LiveData<FamilyGroup> getGroupByIdLive(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGroup(FamilyGroup group);

    @Update
    void updateGroup(FamilyGroup group);

    @Delete
    void deleteGroup(FamilyGroup group);
}
