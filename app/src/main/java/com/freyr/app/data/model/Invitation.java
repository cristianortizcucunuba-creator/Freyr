package com.freyr.app.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "invitations")
public class Invitation {
    @PrimaryKey
    @NonNull
    private String id;
    private String familyGroupId;
    private String invitedUserId;
    private String invitedByUserId;
    private String status; // "pending", "accepted", "declined"
    private String createdAt;

    public Invitation(@NonNull String id, String familyGroupId, String invitedUserId, String invitedByUserId, String status, String createdAt) {
        this.id = id;
        this.familyGroupId = familyGroupId;
        this.invitedUserId = invitedUserId;
        this.invitedByUserId = invitedByUserId;
        this.status = status;
        this.createdAt = createdAt;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getFamilyGroupId() { return familyGroupId; }
    public void setFamilyGroupId(String familyGroupId) { this.familyGroupId = familyGroupId; }

    public String getInvitedUserId() { return invitedUserId; }
    public void setInvitedUserId(String invitedUserId) { this.invitedUserId = invitedUserId; }

    public String getInvitedByUserId() { return invitedByUserId; }
    public void setInvitedByUserId(String invitedByUserId) { this.invitedByUserId = invitedByUserId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
