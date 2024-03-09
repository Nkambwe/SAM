package com.pbu.sam.services;

import com.pbu.sam.dtos.PermissionSetDto;
import com.pbu.sam.dtos.RoleDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RoleService {
    /**Find system role with ID {@code id}
     * @param id System role ID to look for
     * @return role record if found else null
     **/
    RoleDto  findById(long id);

    /**Find system role with ID {@code id}
     * @param id System role ID to look for
     * @param ip logged in user IP Address
     * @param userId logged in user ID
     * @return role record if found else null
     **/
    CompletableFuture<RoleDto>  findByIdAsync(long id, String ip, long userId);

    /**Find system role with ID {@code name}
     * @param name System role name to look for
     * @param ip logged in user IP Address
     * @param userId logged in user ID
     * @return role record if found else null
     **/
    CompletableFuture<RoleDto>  findByName(String name, String ip, long userId);

    /***Check whether system role exists by ID
     * @param id System role ID to look for
     * @return true if role exists false otherwise
     **/
    boolean existsById(long id);
    /**Check whether system role exists by ID
     * @param name System role name to look for
     * @return true if role exists false otherwise
     **/
    boolean existsByName(String name);

    /**
     * Check whether role name is already in use by another role other than role with specified id
     * @param name is role name to search for. Argument is ignores character casing
     * @param id is id of primary role of role name
     * @return true if another record with the same role name is found, otherwise returns false
     **/
    boolean roleNameDuplicated(String name, long id);

    /**Get all system roles
     * @param ip logged in user IP Address
     * @param userId logged in user ID
     * @return a list of system roles if any
     **/
    CompletableFuture<List<RoleDto>> getAll(String ip, long userId);

    /**Create new system role record
     * @param role System role to create
     * @param ip logged in user IP Address
     * @param userId logged in user ID
     * @return System role created
     **/
    CompletableFuture<RoleDto> create(RoleDto role, String ip, long userId) throws InterruptedException;

    /**Update role record
     * @param role System role to update
     * @param ip logged in user IP Address
     * @param userId logged in user ID
     **/
    void updateRole(RoleDto role, String ip, long userId) throws InterruptedException;

    /**
     * Grant system permissions to a given role
     * @param id role id to get permissions for
     * @param ip logged in user IP Address
     * @param userId logged in user ID
     * @return List of role permission sets
     **/
    CompletableFuture<List<PermissionSetDto>> getRolePermissions(long id, String ip, long userId);

    /**
     * Grant system permissions to a given role
     * @param roleId role id to grant permissions
     * @param ip logged in user IP Address
     * @param userId logged in user ID
     * @param setIds list of permission set Ids to assign to role
     **/
    void grantPermissions(Long roleId, List<Long> setIds, String ip, long userId) throws InterruptedException;

    /**
     * Deny system permissions to a given role
     * @param roleId role id to deny permissions
     * @param ip logged in user IP Address
     * @param userId logged in user ID
     * @param setIds list of permission set Ids to deny to role
     **/
    void denyPermissions(Long roleId, List<Long> setIds, String ip, long userId) throws InterruptedException;

    /**Soft Delete system role with ID {@code id}
     * @param id System role ID to delete
     **/
    void delete(long id, String ip, long userId);

    /**Permanently delete system role with ID {@code id}
     * @param id System role ID to delete
     **/
    void purgeRole(long id, String ip, long userId);
}
