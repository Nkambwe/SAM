package com.pbu.sam.services;

import com.pbu.sam.dtos.PermissionSetDto;
import com.pbu.sam.dtos.RoleDto;
import com.pbu.sam.entities.*;
import com.pbu.sam.exceptions.DbRecordNotFound;
import com.pbu.sam.repositories.SystemLogRepository;
import com.pbu.sam.repositories.SystemPermissionSetRepository;
import com.pbu.sam.repositories.SystemRoleRepository;
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
public class RoleServiceImp implements RoleService{
    private final List<RoleDto> records = new ArrayList<>();
    private final ModelMapper mapper;
    private final SystemRoleRepository rolesRepo;
    private final SystemUserRepository usersRepo;
    private final SystemPermissionSetRepository setRepo;
    private final SystemLogRepository logRepo;

    public RoleServiceImp(ModelMapper mapper, SystemRoleRepository rolesRepo, SystemUserRepository usersRepo, SystemPermissionSetRepository setRepo, SystemLogRepository logRepo) {
        this.mapper = mapper;
        this.rolesRepo = rolesRepo;
        this.usersRepo = usersRepo;
        this.setRepo = setRepo;
        this.logRepo = logRepo;
    }

    @Override
    public RoleDto findById(long id) {
        SystemRole role = rolesRepo.findById(id).orElse(null);
        if(role != null){
            return this.mapper.map(role, RoleDto.class);
        }
        return null;
    }

    @Async
    @Transactional
    @Override
    public CompletableFuture<RoleDto> findByIdAsync(long id, String ip, long userId) {
        SystemRole role = rolesRepo.findById(id).orElse(null);
        if(role != null){
            RoleDto record = this.mapper.map(role, RoleDto.class);
            //..create log
            SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
            if(loggedIn != null){
                SystemLog log = new SystemLog();
                log.setAction(String.format("Retrieved system role record with ID '%s'", id));
                log.setIpAddress(ip);
                log.setLogTime(LocalDateTime.now());
                log.setUser(loggedIn);
                logRepo.save(log);
            }
            return CompletableFuture.completedFuture(record);
        }
        return null;
    }

    @Async
    @Transactional
    @Override
    public CompletableFuture<RoleDto> findByName(String name, String ip, long userId) {
        SystemRole role = rolesRepo.findByName(name).orElse(null);
        if(role != null){
            RoleDto record = this.mapper.map(role, RoleDto.class);

            //..create log
            SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
            if(loggedIn == null){
                throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
            }

            SystemLog log = new SystemLog();
            log.setAction(String.format("Retrieved system role record with name '%s'", name));
            log.setIpAddress(ip);
            log.setLogTime(LocalDateTime.now());
            log.setUser(loggedIn);
            logRepo.save(log);
            return CompletableFuture.completedFuture(record);
        }
        return null;
    }

    @Async
    @Transactional
    @Override
    public CompletableFuture<List<RoleDto>> getAll(String ip, long userId) {

        List<SystemRole> roles = rolesRepo.findAll();
        if(roles.isEmpty()){
            return CompletableFuture.completedFuture(records);
        }

        for (SystemRole user : roles) {
            records.add(this.mapper.map(user, RoleDto.class));
        }

        //..create log
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }

        SystemLog log = new SystemLog();
        log.setAction("Retrieving a list of system roles");
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);

        return CompletableFuture.completedFuture(records);
    }

    @Override
    public boolean existsById(long id) {
        return rolesRepo.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return rolesRepo.existsByName(name);
    }

    @Override
    public boolean roleNameDuplicated(String name, long id){
        return rolesRepo.existsByNameAndIdNot(name, id);
    }

    @Async
    @Transactional
    @Override
    public CompletableFuture<RoleDto>  create(RoleDto role, String ip, long userId) throws InterruptedException {

        //..create log
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }

        SystemLog log = new SystemLog();
        log.setAction("Create new system role");
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);

        //map record
        SystemRole record = this.mapper.map(role, SystemRole.class);
        rolesRepo.save(record);

        //set role id
        role.setId(record.getId());
        return CompletableFuture.completedFuture(role);
    }

    @Async
    @Transactional
    @Override
    public void updateRole(RoleDto role, String ip, long userId) {
        //map record
        SystemRole record = this.mapper.map(role, SystemRole.class);
        rolesRepo.updateRoleRecord(record);

        //..create log
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn != null){
            SystemLog log = new SystemLog();
            log.setAction("Updating system role");
            log.setIpAddress(ip);
            log.setLogTime(LocalDateTime.now());
            log.setUser(loggedIn);
            logRepo.save(log);
        }
    }

    @Transactional
    @Override
    public CompletableFuture<List<PermissionSetDto>> getRolePermissions(long id, String ip, long userId) {

        //..create log
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }

        SystemRole role = rolesRepo.findById(id).orElse(null);
        if(role == null){
            throw new DbRecordNotFound("Role", "RoleId", String.format("%s", id));
        }
        //create log record
        SystemLog log = new SystemLog();
        log.setAction("Retrieving a list of system permission sets");
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);

        Set<SystemPermissionSet> sets = role.getPermissions();
        List<PermissionSetDto> permissions = new ArrayList<>();
        if(sets.isEmpty()){
            return CompletableFuture.completedFuture(permissions);
        }

        //convert records
        for (SystemPermissionSet record : sets) {
            permissions.add(this.mapper.map(record, PermissionSetDto.class));
        }

        return CompletableFuture.completedFuture(permissions);
    }

    @Transactional
    @Override
    public void purgeRole(long id, String ip, long userId) {
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn != null){
            SystemLog log = new SystemLog();
            log.setAction(String.format("Purge role with ID '%s'",id));
            log.setIpAddress(ip);
            log.setLogTime(LocalDateTime.now());
            log.setUser(loggedIn);
            logRepo.save(log);
        }
        rolesRepo.deleteById(id);

    }

    @Transactional
    @Override
    public void delete(long id, String ip, long userId) {
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn != null){
            SystemLog log = new SystemLog();
            log.setAction(String.format("Marking role with ID '%s' as deleted",id));
            log.setIpAddress(ip);
            log.setLogTime(LocalDateTime.now());
            log.setUser(loggedIn);
            logRepo.save(log);
        }

        rolesRepo.isDeleted(id);
    }

    @Async
    @Transactional
    @Override
    public void grantPermissions(Long roleId, List<Long> setIds, String ip, long userId) {
        //..get system user
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }

        //log action
        SystemLog log = new SystemLog();
        log.setAction("Assign permissions to system role");
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);

        //Get role
        SystemRole role = this.rolesRepo.findById(roleId).orElse(null);
        if(role == null){
            throw new DbRecordNotFound("Role", "RoleId", String.format("%s", roleId));
        }

        Set<SystemPermissionSet> permissions = this.setRepo.findAllByIdIn(setIds);
        if(permissions.isEmpty()){
            log = new SystemLog();
            log.setAction("No permission sets found with in the provided list of IDs");
            log.setIpAddress(ip);
            log.setLogTime(LocalDateTime.now());
            log.setUser(loggedIn);
            logRepo.save(log);
            throw new DbRecordNotFound("Role", "RoleId", String.format("%s",roleId));
        }

        log.setAction(String.format("Assign Permissions to role >>> %s", role));

        // Save the updated role
        role.getPermissions().addAll(permissions);
        rolesRepo.save(role);

        //log status
        log.setAction("Permission assigned to role successfully");
    }

    @Async
    @Transactional
    @Override
    public void denyPermissions(Long roleId, List<Long> setIds, String ip, long userId) {
        //..get system user
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }

        //log action
        SystemLog log = new SystemLog();
        log.setAction("Remove permissions from system role");
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);

        //Get role record
        SystemRole role = this.rolesRepo.findById(roleId).orElse(null);
        if(role == null){
            throw new DbRecordNotFound("Role", "RoleId", String.format("%s", roleId));
        }

        Set<SystemPermissionSet> permissions = this.setRepo.findAllByIdIn(setIds);
        if(permissions.isEmpty()){
            log = new SystemLog();
            log.setAction("No permissions found with in the provided list");
            log.setIpAddress(ip);
            log.setLogTime(LocalDateTime.now());
            log.setUser(loggedIn);
            logRepo.save(log);
            throw new DbRecordNotFound("Permission set", "ID", String.format("%s",setIds));
        }

        log.setAction(String.format("Remove permissions from role >>> %s", role));

        //delete permissions
        role.getPermissions().removeAll(permissions);
        rolesRepo.save(role);

        //log status
        log.setAction("Role permissions updated succesfully");
    }
}
