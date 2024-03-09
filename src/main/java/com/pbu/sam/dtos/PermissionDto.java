package com.pbu.sam.dtos;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PermissionDto {
    private long id;
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NotNull(message = "Permission name is required")
    @Size(min = 2, max = 120, message = "Permission name must be between 2 and 120 characters")
    @Pattern(regexp = "^[a-zA-Z]*$", message = "Permission name contains invalid characters")
    private String name;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotNull(message = "Permission description is required")
    @Pattern(regexp = "^[a-zA-Z0-9 ._-]*$", message = "Permission description contains invalid characters")
    private String description;
    public String getDescription() {
        return description;
    }

    private boolean isLocked;
    public boolean getIsLocked() {
        return isLocked;
    }
    public void setIsLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
