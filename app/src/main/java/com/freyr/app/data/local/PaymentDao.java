package com.freyr.app.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import com.freyr.app.data.datos.Pago;

import java.util.List;

@Dao
public interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY fecha DESC, fechaCreado DESC")
    LiveData<List<Pago>> getAllPaymentsLive();

    @Query("SELECT * FROM payments ORDER BY fecha DESC, fechaCreado DESC")
    List<Pago> getAllPayments();

    @Query("SELECT * FROM payments WHERE idDeuda = :debtId ORDER BY fecha DESC")
    LiveData<List<Pago>> getPaymentsForDebtLive(String debtId);

    @Query("SELECT * FROM payments WHERE idDeuda = :debtId ORDER BY fecha DESC")
    List<Pago> getPaymentsForDebt(String debtId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPayment(Pago payment);

    @Delete
    void deletePayment(Pago payment);

    @Query("DELETE FROM payments WHERE id = :id")
    void deletePaymentById(String id);

    @Query("DELETE FROM payments WHERE idDeuda = :debtId")
    void deletePaymentsForDebt(String debtId);
}
