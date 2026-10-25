package com.freyr.app.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.List;

@Entity(tableName = "family_groups")
public class FamilyGroup {
    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String creatorId;
    private List<String> memberIds;
    private String createdAt;

    public FamilyGroup(@NonNull String id, String name, String creatorId, List<String> memberIds, String createdAt) {
        this.id = id;
        this.name = name;
        this.creatorId = creatorId;
        this.memberIds = memberIds;
        this.createdAt = createdAt;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public List<String> getMemberIds() { return memberIds; }
    public void setMemberIds(List<String> memberIds) { this.memberIds = memberIds; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
