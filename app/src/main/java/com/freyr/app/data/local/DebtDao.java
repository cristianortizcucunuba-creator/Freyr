package com.freyr.app.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.freyr.app.data.model.Debt;
import java.util.List;

@Dao
public interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY createdAt DESC")
    LiveData<List<Debt>> getAllDebtsLive();

    @Query("SELECT * FROM debts ORDER BY createdAt DESC")
    List<Debt> getAllDebts();

    @Query("SELECT * FROM debts WHERE id = :id LIMIT 1")
    Debt getDebtById(String id);

    @Query("SELECT * FROM debts WHERE id = :id LIMIT 1")
    LiveData<Debt> getDebtByIdLive(String id);

    @Query("SELECT * FROM debts WHERE familyGroupId = :groupId ORDER BY createdAt DESC")
    LiveData<List<Debt>> getDebtsForGroupLive(String groupId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDebt(Debt debt);

    @Update
    void updateDebt(Debt debt);

    @Delete
    void deleteDebt(Debt debt);

    @Query("DELETE FROM debts WHERE id = :id")
    void deleteDebtById(String id);
}
