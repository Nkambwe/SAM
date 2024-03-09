package com.pbu.sam.services;

import com.pbu.sam.dtos.UserDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SystemUserService {
    /**
     * find system user by user id
     * @param id is ID to look for
     * @param ip logged in user IP Address
     * @param userId logged in user ID
     * @return user with id @id else returns null
     **/
    CompletableFuture<UserDto> findById(long id, String ip, long userId);
    /**
     * find system user by user username.
     * @param username is username to look for and ignores character casing
     * @param ip logged in user IP Address
     * @param userId logged in user ID
     * @return user with username @username else returns null
     **/
    CompletableFuture<UserDto> findByUsername(String username, String ip, long userId);
    /**
     * find system user by user pfNo.
     * @param pfNo is pf number to look for and ignores character casing
     * @param ip logged in user IP Address
     * @param userId logged in user ID
     * @return user with pf number @pfNo else returns null
     */
    CompletableFuture<UserDto> findByPfNo(String pfNo, String ip, long userId);
    /**
     * Get all system users.
     * @return list of system users else returns an empty list
     */
    CompletableFuture<List<UserDto>> getAll(String ip, long userId);
    /**
     * Soft delete user record with specified user id.
     * @param id is the ID of record to mark as delete
     * @param ip logged in user IP Address
     * @param userId logged in user ID
     * remarks  >> Soft deleted record is simply marked as deleted
     */
    void softDeleted(long id, String ip, long userId);
    /**
     * Check whether user record with specified user id is marked as deleted.
     * @param userId is the ID of record to look for
     * @return True if record is marked as deleted false otherwise
     */
    boolean isDeleted(long userId);

    /**
     * Check whether user record was created by specified user id
     * @param userId user id for user to look for
     * @param creator username of the person who created record
     * @return True if record is created by the same user false otherwise
     */
    boolean isCreator(long userId, String creator);

    /**
     * Update user record of specified user.
     * @param user is specified user record to update
     * @param ip logged in user IP Address
     * @param userId logged in user ID
     */
    void updateRecord(UserDto user, String ip, long userId) throws InterruptedException ;
    /**
     * Verify user with specified user id.
     * @param verify verification flag
     * @param id user id for user to verify
     * @param ip logged in user IP Address
     * @param userId logged in user ID
     */
    void verifiedUser(boolean verify, String modifiedBy, String modifiedOn, long id, String ip, long userId);
    /**
     * Activate user with specified user id.
     * @param active Activation flag
     * @param id user id for user to activate
     * @param ip logged in user IP Address
     * @param userId logged in user ID
     **/
    void activeUser(boolean active, String modifiedBy, String modifiedOn, long id, String ip, long userId);
    /**
     * Login user with specified user id.
     * @param loggedIn - LoggedIn flag
     * @param id - user id for user to logged in
     * @param ip logged in user IP Address
     * @param userId logged in user ID
     **/
    void updateLoginStatus(boolean loggedIn, long id, String ip, long userId);
    /**
     * Update password for user with specified user id.
     * @param password - Updated password values
     * @param id - user id for user to update
     * @param ip logged in user IP Address
     * @param userId logged in user ID
     */
    void updatePassword(String password, long id, String ip, long userId);
    /**
     * Check whether user record with specified username exists
     * @param id is user ID to look for
     * @return true if record is found, otherwise returns false
     **/
    boolean exists(long id);
    /**
     * Check whether user record with specified pf number exists
     * @param pfNo is pf number to search for
     * @return true if record is found, otherwise returns false
     **/
    boolean pfNoTaken(String pfNo);
    /**
     * Check whether pf number is already in use by another user other than user with specified id
     * @param pfNo is pf number to search for. Argument is ignores character casing
     * @param id is id of primary owner of pf number
     * @return true if another record with the same pg number is found, otherwise returns false
     **/
    boolean pfNoDuplicated(String pfNo, long id);
    /**
     * Check whether user record with specified username exists
     * @param username is username to search for. Argument is ignores character casing
     * @return true if record is found, otherwise returns false
     **/
    boolean usernameTaken(String username);
    /**
     * Check whether username is already in use by another user other than user with specified id
     * @param username is username to search for. Argument is ignores character casing
     * @param id is id of primary owner of username
     * @return true if another record with the same username is found, otherwise returns false
     **/
    boolean usernameDuplicated(String username, long id);

    /**
     * Create new user object {@code user}
     * @param user User object to create
     * @return User record created
     **/
    CompletableFuture<UserDto> create(UserDto user, String ip, long userId) throws InterruptedException;
}
