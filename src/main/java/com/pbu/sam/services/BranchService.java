package com.pbu.sam.services;

import com.pbu.sam.dtos.BranchDto;
import com.pbu.sam.dtos.UserDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface BranchService {
    /**
     * Find branch by its name
     * @param id branch ID to find
     * @return System branch object if found else returns null
     * **/
    BranchDto findById(long id);

    /**
     * find branch by its Solid
     * @param solId branch solId to find
     * @param ip logged in user IP Address
     * @param userId logged in user ID
     * @return System branch object if found else returns null
     **/
    CompletableFuture<BranchDto> findBySolId(String solId, String ip, long userId);
    /**
     * Get all system branches.
     * @param ip logged in user IP Address
     * @param userId logged in user ID
     * @return list of system branches else returns an empty list
     */
    CompletableFuture<List<BranchDto>> getAll(String ip, long userId);
    /**
     * check if branch exists with given id
     * @param id branch ID to find
     * @return true if branch exists else false
     **/
    boolean checkIfExistsById(long id);
    /**
     * check if branch exists with given solid
     * @param solId branch solId to find
     * @return true if branch exists else false
     **/
    boolean checkIfExistsBySolId(String solId);

    /**check whether branch with solID {@code solId} exists other than one with ID
     * @param branchId branch ID which owns name
     * @param solId branch solId to find
     * @return true if branch exists else false
     **/
    boolean checkSolIdDuplication(long branchId, String solId);

    /**check whether branch exists with name {@code branchName}
     * @param branchName branch name to find
     * @return true if branch exists else false
     **/
    boolean checkIfExistsByName(String branchName);

    /**check whether branch with name {@code branchName} exists other than one with ID
     * @param branchId branch ID which owns name
     * @param branchName branch name to find
     * @return true if branch exists else false
     **/
    boolean checkNameDuplication(long branchId, String branchName);

    /**Update branch object with {@code id} to active status {@code status}
     * @param id branch ID to update
     * @param ip logged in user IP Address
     * @param userId logged in user ID
     * @param status branch status to set
     **/
    void activateBranch(long id, boolean status, String ip, long userId);
    /**
     * Create new branch object {@code branch}
     * @param branch User object to create
     * @param ip logged in user IP Address
     * @param userId logged in user ID
     * @return User record created
     **/
    CompletableFuture<BranchDto> create(BranchDto branch, String ip, long userId) throws InterruptedException;

    /**Update branch record
     * @param branch branch to update
     * @param ip logged in user IP Address
     * @param userId logged in user ID
     **/
    void updateBranch(BranchDto branch, String ip, long userId);
    /**
     * Soft delete branch with specified ID
     * @param id branch ID to find
     * @param ip logged in user IP Address
     * @param userId logged in user ID
     * **/
    void deleteBranch(long id, String ip, long userId);
    /**
     * Permanently delete branch with specified ID
     * @param id branch ID to find
     * @param ip logged in user IP Address
     * @param userId logged in user ID
     * **/
    void purgeBranch(long id, String ip, long userId);
}
