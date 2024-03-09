package com.pbu.sam.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

public class PermissionSetDto {
    private long id;
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    @NotNull(message = "Permission Set name is required")
    @Size(min = 2, max = 120, message = "Permission Set name must be between 2 and 120 characters")
    @Pattern(regexp = "^[a-zA-Z]*$", message = "Permission Set name contains invalid characters")
    private String name;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @NotNull(message = "Permission Set description is required")
    @Pattern(regexp = "^[a-zA-Z0-9 ._-]*$", message = "Permission Set description contains invalid characters")
    private String description;
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    private boolean isDeleted;
    public boolean getIsDeleted() {
        return isDeleted;
    }
    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    private long userId;
    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }

    private boolean isLocked;
    public boolean getIsLocked() {
        return isLocked;
    }
    public void setIsLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    private Set<PermissionDto> permissions;
    public Set<PermissionDto> getPermissions() {
        return permissions;
    }
    public void setPermissions(Set<PermissionDto> permissions) {
        this.permissions = permissions;
    }

}
