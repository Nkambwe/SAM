package com.pbu.sam.services;

import com.pbu.sam.dtos.PermissionDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PermissionService {
    /**Find system permission with ID {@code id}
     * @param id System permission ID to look for
     * @param ip IP Address of user logged in
     * @param userId User ID of user logged in
     * @return System permission record if found else null
     **/
    CompletableFuture<PermissionDto> findById(long id, String ip, long userId);
    /**Find system permission with ID {@code id}
     * @param name System permission name to look for
     * @param ip IP Address of user logged in
     * @param userId User ID of user logged in
     * @return System permission record if found else null
     **/
    CompletableFuture<PermissionDto> findByName(String name, String ip, long userId);

    /**Get all system permissions
     * @param ip IP Address of user logged in
     * @param userId User ID of user logged in
     * @return a list of system permissions if any
     **/
    CompletableFuture<List<PermissionDto>> getAll(String ip, long userId);

    /**
     * Remove permission to permission set
     * @param permission Permission record to update
     * @param ip IP Address of user logged in
     * @param userId User ID of user logged in
     **/
    void  updatePermission(PermissionDto permission, String ip, long userId);

}
