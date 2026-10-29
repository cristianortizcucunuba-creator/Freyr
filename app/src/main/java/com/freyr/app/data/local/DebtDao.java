package com.freyr.app.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.freyr.app.data.model.Deuda;

import java.util.List;

@Dao
public interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY fechaCreacion DESC")
    LiveData<List<Deuda>> getAllDebtsLive();

    @Query("SELECT * FROM debts ORDER BY fechaCreacion DESC")
    List<Deuda> getAllDebts();

    @Query("SELECT * FROM debts WHERE id = :id LIMIT 1")
    Deuda getDebtById(String id);

    @Query("SELECT * FROM debts WHERE id = :id LIMIT 1")
    LiveData<Deuda> getDebtByIdLive(String id);

    @Query("SELECT * FROM debts WHERE idGrupoFamiliar = :groupId ORDER BY fechaCreacion DESC")
    LiveData<List<Deuda>> getDebtsForGroupLive(String groupId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDebt(Deuda debt);

    @Update
    void updateDebt(Deuda debt);

    @Delete
    void deleteDebt(Deuda debt);

    @Query("DELETE FROM debts WHERE id = :id")
    void deleteDebtById(String id);
}
