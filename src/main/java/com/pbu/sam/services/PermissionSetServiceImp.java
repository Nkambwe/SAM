package com.pbu.sam.services;

import com.pbu.sam.dtos.PermissionSetDto;
import com.pbu.sam.entities.*;
import com.pbu.sam.exceptions.DbRecordNotFound;
import com.pbu.sam.repositories.SystemLogRepository;
import com.pbu.sam.repositories.SystemPermissionRepository;
import com.pbu.sam.repositories.SystemPermissionSetRepository;
import com.pbu.sam.repositories.SystemUserRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
public class PermissionSetServiceImp implements PermissionSetService{
    private final List<PermissionSetDto> records = new ArrayList<>();
    private final ModelMapper mapper;
    private final SystemUserRepository usersRepo;
    private final SystemLogRepository logRepo;
    private final SystemPermissionRepository permissionRepo;
    private final SystemPermissionSetRepository setRepo;

    public PermissionSetServiceImp(ModelMapper mapper, SystemUserRepository usersRepo, SystemLogRepository logRepo, SystemPermissionRepository permissionRepo, SystemPermissionSetRepository setRepo) {
        this.mapper = mapper;
        this.usersRepo = usersRepo;
        this.logRepo = logRepo;
        this.permissionRepo = permissionRepo;
        this.setRepo = setRepo;
    }

    @Async
    @Override
    public CompletableFuture<PermissionSetDto> findById(long setId, String ip, long userId) throws DbRecordNotFound {

        //..get system logged user
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }

        ///create action log
        SystemLog log = new SystemLog();
        log.setAction(String.format("Retrieved system permission set record with SetID '%s'", setId));
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);

        SystemPermissionSet setRecord = setRepo.findById(setId).orElse(null);
        if(setRecord == null){
            throw new DbRecordNotFound("Permission Set", "Set Id", String.format("%s",setId));
        }

        //get permission record
        PermissionSetDto record = this.mapper.map(setRecord, PermissionSetDto.class);

        //return record
        return CompletableFuture.completedFuture(record);
    }

    @Override
    public CompletableFuture<PermissionSetDto> findByName(String setName, String ip, long userId) throws DbRecordNotFound {
        SystemPermissionSet setRecord = setRepo.findBySetName(setName).orElse(null);
        if(setRecord == null){
            throw new DbRecordNotFound("Permission Set", "Set name", String.format("%s",setName));
        }

        //get permission record
        PermissionSetDto record = this.mapper.map(setRecord, PermissionSetDto.class);

        //..get system logged user
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }

        //create action log
        SystemLog log = new SystemLog();
        log.setAction(String.format("Retrieved system permission Set record with name '%s'", setName));
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);

        //return record
        return CompletableFuture.completedFuture(record);
    }

    @Override
    public CompletableFuture<List<PermissionSetDto>> getAll(String ip, Long userId) {
        //..create log
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }

        //create log record
        SystemLog log = new SystemLog();
        log.setAction("Retrieving a list of system permission sets");
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);

        List<SystemPermissionSet> sets = setRepo.findAllWithSystemPermissions();
        if(sets.isEmpty()){
            return CompletableFuture.completedFuture(records);
        }

        for (SystemPermissionSet record : sets) {
            records.add(this.mapper.map(record, PermissionSetDto.class));
        }

        return CompletableFuture.completedFuture(records);
    }

    @Override
    public boolean existsById(long id) {
        return setRepo.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return setRepo.existsBySetName(name);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, long id) {
        return setRepo.existsBySetNameAndIdNot(name, id);
    }

    @Transactional
    @Override
    public void lockPermissionSet(long id, String ip, long userId) throws DbRecordNotFound {
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }

        //create action log
        SystemLog log = new SystemLog();
        log.setAction(String.format("Locking permission set with ID '%s'",id));
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);

        //lock record
        setRepo.isLocked(id);
    }

    @Transactional
    @Override
    public CompletableFuture<PermissionSetDto> create(PermissionSetDto permission, List<Long> permissions, String ip, long userId) throws Exception {
        //..get system user
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }

        //log action
        SystemLog log = new SystemLog();
        log.setAction("Adding new permission set");
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);

        //map record
        SystemPermissionSet record = this.mapper.map(permission, SystemPermissionSet.class);

        //..add permissions to set if any
        if(permissions != null && !permissions.isEmpty()){
            Set<SystemPermission> setPermission = this.permissionRepo.findAllByIdIn(permissions);
            if(!setPermission.isEmpty()){
                //lock permissions if set is locked
                if(permission.getIsLocked()){
                    for (SystemPermission p: setPermission){
                        p.setLocked(true);
                    }
                }
                record.setPermissions(setPermission);
            }
        }

        //..save record
        setRepo.save(record);

        //set role id
        permission.setId(record.getId());
        return CompletableFuture.completedFuture(permission);
    }

    @Async
    @Transactional
    @Override
    public void addPermissionToSet(List<Long> permissionIds, long setId, boolean isLocked, String ip, long userId) throws DbRecordNotFound {
        //..get system user
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }

        //log action
        SystemLog log = new SystemLog();
        log.setAction("Assign permissions to permission set");
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);

        //Get permission set
        SystemPermissionSet permissionSet = this.setRepo.findById(setId).orElse(null);
        if(permissionSet == null){
            throw new DbRecordNotFound("PermissionSet", "SetId", String.format("%s", setId));
        }

        Set<SystemPermission> permissions = this.permissionRepo.findAllByIdIn(permissionIds);
        if(permissions.isEmpty()){
            log = new SystemLog();
            log.setAction("No permissions found with in the provided list IDs");
            log.setIpAddress(ip);
            log.setLogTime(LocalDateTime.now());
            log.setUser(loggedIn);
            logRepo.save(log);
            throw new DbRecordNotFound("Permission Set", "ID", String.format("%s",setId));
        }

        log.setAction(String.format("Permission Set >>> %s", permissionSet));

        // Save the updated PermissionSet
        permissionSet.getPermissions().addAll(permissions);

        //lock set and its permissions
        if(isLocked){
            permissionSet.setLocked(true);
            for(SystemPermission p : permissionSet.getPermissions()){
                p.setLocked(true);
            }
        }

        //save changes
        setRepo.save(permissionSet);

        //log status
        log.setAction("Permission Set updated");
    }

    @Async
    @Transactional
    @Override
    public void removePermissionFromSet(List<Long> permissionIds, long setId, String ip, long userId) throws DbRecordNotFound {
        //..get system user
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }

        //log action
        SystemLog log = new SystemLog();
        log.setAction("Remove permissions from permission set");
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);

        //Get permission set
        SystemPermissionSet permissionSet = this.setRepo.findById(setId).orElse(null);
        if(permissionSet == null){
            throw new DbRecordNotFound("PermissionSet", "SetId", String.format("%s", setId));
        }

        Set<SystemPermission> permissions = this.permissionRepo.findAllByIdIn(permissionIds);
        if(permissions.isEmpty()){
            log = new SystemLog();
            log.setAction("No permissions found with in the provided list");
            log.setIpAddress(ip);
            log.setLogTime(LocalDateTime.now());
            log.setUser(loggedIn);
            logRepo.save(log);
            throw new DbRecordNotFound("Permission Set", "ID", String.format("%s",setId));
        }

        log.setAction(String.format("Permission Set >>> %s", permissionSet.toString()));

        //delete permissions
        permissionSet.getPermissions().removeAll(permissions);

        // unlock these permissions
        for (SystemPermission p:permissions) {
            p.setLocked(false);
        }
        setRepo.save(permissionSet);

        //log status
        log.setAction("Permission Set updated");
    }

    @Transactional
    @Override
    public void delete(long id, String ip, long userId) {
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }

        //create log record
        SystemLog log = new SystemLog();
        log.setAction(String.format("Marking permission set with ID '%s' as deleted",id));
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);

        //delete record
        setRepo.isDeleted(id);
    }

    @Transactional
    @Override
    public void purgeSet(long id, String ip, long userId) {
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }

        //create log record
        SystemLog log = new SystemLog();
        log.setAction(String.format("Purge permission Set with ID '%s'",id));
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);
        setRepo.deleteById(id);
    }

    @Transactional
    @Override
    public void updateSet(PermissionSetDto set, String ip, Long loggedUserId) throws DbRecordNotFound  {
        //...create logged in user record
        SystemUser loggedIn = usersRepo.findById(loggedUserId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", loggedUserId));
        }

        //create log record
        SystemLog log = new SystemLog();
        log.setAction("Updating system role");
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);


        //...map record
        if(!setRepo.existsById(set.getId())){
            throw new DbRecordNotFound("PermissionSet", "SetId", String.format("%s", set.getId()));
        }

        //..update record
        setRepo.updatePermissionSet(set);
    }

}
