package com.freyr.app.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "debts")
public class Debt {
    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String description;
    private double amount;
    private String assignedUserId; // null means whole family or unassigned
    private String familyGroupId; // null means personal debt
    private String status; // "pending" or "paid"
    private String createdAt;
    private String createdBy;

    public Debt(@NonNull String id, String name, String description, double amount, String assignedUserId, String familyGroupId, String status, String createdAt, String createdBy) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.amount = amount;
        this.assignedUserId = assignedUserId;
        this.familyGroupId = familyGroupId;
        this.status = status;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getAssignedUserId() { return assignedUserId; }
    public void setAssignedUserId(String assignedUserId) { this.assignedUserId = assignedUserId; }

    public String getFamilyGroupId() { return familyGroupId; }
    public void setFamilyGroupId(String familyGroupId) { this.familyGroupId = familyGroupId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
