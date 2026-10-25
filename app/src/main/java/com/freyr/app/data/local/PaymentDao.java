package com.freyr.app.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.freyr.app.data.model.Payment;
import java.util.List;

@Dao
public interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY date DESC, createdAt DESC")
    LiveData<List<Payment>> getAllPaymentsLive();

    @Query("SELECT * FROM payments ORDER BY date DESC, createdAt DESC")
    List<Payment> getAllPayments();

    @Query("SELECT * FROM payments WHERE debtId = :debtId ORDER BY date DESC")
    LiveData<List<Payment>> getPaymentsForDebtLive(String debtId);

    @Query("SELECT * FROM payments WHERE debtId = :debtId ORDER BY date DESC")
    List<Payment> getPaymentsForDebt(String debtId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPayment(Payment payment);

    @Delete
    void deletePayment(Payment payment);

    @Query("DELETE FROM payments WHERE id = :id")
    void deletePaymentById(String id);

    @Query("DELETE FROM payments WHERE debtId = :debtId")
    void deletePaymentsForDebt(String debtId);
}
