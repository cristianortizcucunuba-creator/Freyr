package com.freyr.app.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "payments")
public class Payment {
    @PrimaryKey
    @NonNull
    private String id;
    private String debtId;
    private String userId;
    private double amount;
    private String date;
    private String description;
    private String createdAt;

    public Payment(@NonNull String id, String debtId, String userId, double amount, String date, String description, String createdAt) {
        this.id = id;
        this.debtId = debtId;
        this.userId = userId;
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.createdAt = createdAt;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getDebtId() { return debtId; }
    public void setDebtId(String debtId) { this.debtId = debtId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
