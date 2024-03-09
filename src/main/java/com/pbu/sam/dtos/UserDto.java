package com.pbu.sam.dtos;

import jakarta.validation.constraints.*;

public class UserDto {
    private long id;
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    @NotNull(message = "Username is required")
    @Size(min = 2, max = 80, message = "Username must be between 2 and 80 characters")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "Username contains invalid characters")
    private String username;
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    @NotNull(message = "First name is required")
    @Size(min = 2, max = 80, message = "First name must be between 2 and 80 characters")
    @Pattern(regexp = "^[a-zA-Z]*$", message = "First name contains invalid characters")
    private String firstname;
    public String getFirstname() {
        return firstname;
    }
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    @NotNull(message = "Lastname is required")
    @Size(min = 2, max = 80, message = "Lastname must be between 2 and 80 characters")
    @Pattern(regexp = "^[a-zA-Z]*$", message = "First name contains invalid characters")
    private String lastname;
    public String getLastname() {
        return lastname;
    }
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    @NotNull(message = "Gender is required")
    @Size(min = 1, max = 6, message = "Gender must be between 4 and 6 characters")
    @Pattern(regexp = "^[a-zA-Z]*$", message = "Gender contains invalid characters")
    private String gender;
    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }

    @NotNull(message = "PF Number is required")
    @Size(min = 1, max = 6, message = "PF number must be between 1 and 6 characters")
    @Pattern(regexp = "^[0-9]*$", message = "PF number contains invalid characters")
    private String pfNo;
    public String getPfNo() {
        return pfNo;
    }
    public void setPfNo(String pfNo) {
        this.pfNo = pfNo;
    }

    @PositiveOrZero(message = "Branch Id must be non-negative")
    @DecimalMin(value = "1", inclusive = true, message = "Branch Id must be greater than zero")
    private long branchId;
    public long getBranchId() {
        return branchId;
    }
    public void setBranchId(long branchId) {
        this.branchId = branchId;
    }

    private String branchSolId;
    public String getBranchSolId() {
        return branchSolId;
    }
    public void setBranchSolId(String branchSolId) {
        this.branchSolId = branchSolId;
    }

    @NotNull(message = "Email address is required")
    @Email(message = "Invalid email address")
    private String email;
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @NotNull(message = "Password is required")
    private String password;
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    private boolean isDeleted;
    public boolean isDeleted() {
        return isDeleted;
    }
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    private boolean isLoggedIn;
    public boolean isLoggedIn() {
        return isLoggedIn;
    }
    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    private boolean isActive;
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    private boolean isVerified;
    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    private String verifiedBy;
    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

    private long roleId;
    public long getRoleId() {
        return roleId;
    }
    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    private String roleName;
    public String getRoleName() {
        return roleName;
    }
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    private String createdBy;
    public String getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    private String createdOn;
    public String getCreatedOn() {
        return createdOn;
    }
    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    private String modifiedOn;
    public String getModifiedOn() {
        return modifiedOn;
    }
    public void setModifiedOn(String modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    private String modifiedBy;
    public String getModifiedBy() {
        return modifiedBy;
    }
    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }


    private long loggedUserId;
    public long getLoggedUserId() {
        return loggedUserId;
    }
    public void setLoggedUserId(long loggedUserId) {
        this.loggedUserId = loggedUserId;
    }
}
