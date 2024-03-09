package com.pbu.sam.services;

import com.pbu.sam.dtos.PermissionSetDto;
import com.pbu.sam.dtos.RoleDto;
import com.pbu.sam.entities.SystemPermission;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface PermissionSetService {

    /**Get a system permission set by its ID
     * @param setId Permission Set ID to look for
     * @param ip IP Address of user logged in
     * @param userId User ID of user logged in
     * @return System Permission record if found otherwise
     **/
    CompletableFuture<PermissionSetDto> findById(long setId, String ip, long userId) throws Exception;

    /**Get a system permission set by its ID
     * @param setName Permission Set name to look for
     * @param ip IP Address of user logged in
     * @param userId User ID of user logged in
     * @return System Permission record if found otherwise
     **/
    CompletableFuture<PermissionSetDto> findByName(String setName, String ip, long userId) throws Exception;
    /**Get all permission sets
     * @param ip IP Address of user logged in
     * @param loggedUserId User ID of user logged in
     * @return System Permission record if found otherwise
     **/
    CompletableFuture<List<PermissionSetDto>> getAll(String ip, Long loggedUserId);
    /***Check whether system permission set exists by ID
     * @param id System permission set ID to look for
     * @return true if role exists false otherwise
     **/

    boolean existsById(long id);

    /***Check whether system permission set exists by set name
     * @param name System permission set name to look for
     * @return true if role exists false otherwise
     **/
    boolean existsByName(String name);
    /**Check whether Permission Set name is already in use by another Set other than the set with specified id
     * @param name is Set name to search for. Argument is ignores character casing
     * @param id is id of primary Permission set
     * @return true if another record with the same set name is found, otherwise returns false
     **/
    boolean existsByNameAndIdNot(String name, long id);
    /**
     * Lock a list of permissions
     * @param id Permission set ID to lock
     * @param ip IP Address of user logged in
     * @param userId User ID of user logged in
     * remarks >> Locked permissions set cannot be modified and its permissions cannot be assigned to another role
     **/
    void lockPermissionSet(long id, String ip, long userId) throws Exception;

    /**Create new system permission Set record
     * @param set System permission set to create
     * @param permissions Set of System permissions assigned to this set
     * @param ip logged in user IP Address
     * @param userId logged in user ID
     * @return System role created
     **/
    CompletableFuture<PermissionSetDto> create(PermissionSetDto set, List<Long> permissions, String ip, long userId) throws Exception;

    /**Add permission to permission set
     * @param permissionIds List of Permission IDs to add to permission set
     * @param setId Permission set ID to which permissions belong to
     * @param ip IP Address of user logged in
     * @param userId User ID of user logged in
     **/
    void addPermissionToSet(List<Long> permissionIds, long setId, boolean isLocked, String ip, long userId) throws Exception;

    /**Remove permission to permission set
     *
     * @param permissionIds List of Permission IDs to remove from permission set
     * @param setId         Permission set ID to which permissions belong to
     * @param ip            IP Address of user logged in
     * @param userId        User ID of user logged in
     **/
    void removePermissionFromSet(List<Long> permissionIds, long setId, String ip, long userId) throws Exception;

    /**Soft Delete system permission set with ID {@code id}
     * @param id System permission set ID to delete
     **/
    void delete(long id, String ip, long userId);

    /**Permanently delete system permission set with ID {@code id}
     * @param id System permission set ID to delete
     **/
    void purgeSet(long id, String ip, long userId);

    void updateSet(PermissionSetDto set, String ip, Long loggedUserId);
}
