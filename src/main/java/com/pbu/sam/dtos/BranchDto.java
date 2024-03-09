package com.pbu.sam.dtos;

import jakarta.validation.constraints.*;
/**
 * Class Name : BranchModel
 * Created By : Nkambwe Mark
 * Description: Class handles Branch records
 **/
public class BranchDto {
    private long id;
    //get branch id
    public long getId() {
        return id;
    }
    //set branch
    public void setId(long id) {
        this.id = id;
    }

    @NotNull(message = "SolId is required")
    @Size(min = 2, max = 10, message = "SolId must be between 2 and 10 characters")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "Branch SolId contains invalid characters")
    private String solid;
    //get sol Id
    public String getSolid() {
        return solid;
    }
    //set sol Id
    public void setSolId(String solId) {
        this.solid = solId;
    }

    @NotNull(message = "Branch name is required")
    @Size(min = 2, max = 120, message = "Branch name must be between 2 and 120 characters")
    @Pattern(regexp = "^[a-zA-Z0-9 -_]*$", message = "Branch name contains invalid characters")
    private String branchName;
    //get branch name
    public String getBranchName() { return branchName; }
    //set branch name
    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    //Check whether branch is active or not
    private boolean isActive;
    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean active) {
        isActive = active;
    }


    //Check whether branch is active or not
    private boolean isDeleted;
    public boolean isDeleted() {
        return isDeleted;
    }
    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    //branch created on date
    private String createdOn;
    public String getCreatedOn() {
        return createdOn;
    }
    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    /*Default constructor*/
    public BranchDto() {
    }

    /*Constructor with solId and branch name argument*/
    public BranchDto(String solId, String branchName) {
        this.solid = solId;
        this.branchName = branchName;
    }

    /*Constructor with id, solID and branch name argument*/
    public BranchDto(long id, String solId, String branchName) {
        this.id = id;
        this.solid = solId;
        this.branchName = branchName;
    }
}

