package com.freyr.app.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import com.freyr.app.data.model.Pago;

import java.util.List;

@Dao
public interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY date DESC, createdAt DESC")
    LiveData<List<Pago>> getAllPaymentsLive();

    @Query("SELECT * FROM payments ORDER BY date DESC, createdAt DESC")
    List<Pago> getAllPayments();

    @Query("SELECT * FROM payments WHERE debtId = :debtId ORDER BY date DESC")
    LiveData<List<Pago>> getPaymentsForDebtLive(String debtId);

    @Query("SELECT * FROM payments WHERE debtId = :debtId ORDER BY date DESC")
    List<Pago> getPaymentsForDebt(String debtId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPayment(Pago payment);

    @Delete
    void deletePayment(Pago payment);

    @Query("DELETE FROM payments WHERE id = :id")
    void deletePaymentById(String id);

    @Query("DELETE FROM payments WHERE debtId = :debtId")
    void deletePaymentsForDebt(String debtId);
}
