package com.pbu.sam.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public class RoleDto {
    private long id;
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    @NotNull(message = "Role name is required")
    @Size(min = 2, max = 120, message = "Role name must be between 2 and 120 characters")
    @Pattern(regexp = "^[a-zA-Z]*$", message = "Role name contains invalid characters")
    private String name;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @NotNull(message = "Role description is required")
    @Pattern(regexp = "^[a-zA-Z0-9 ._-]*$", message = "Role description contains invalid characters")
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

    private List<PermissionSetDto> permissions;
    public List<PermissionSetDto> getPermissions() {
        return permissions;
    }
    public void setPermissions(List<PermissionSetDto> permissions) {
        this.permissions = permissions;
    }

    private long userId;
    public long getLoggedUserId() {
        return userId;
    }
    public void setLoggedUserId(String userId) {
        this.description = userId;
    }
}

