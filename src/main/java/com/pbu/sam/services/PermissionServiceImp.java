package com.pbu.sam.services;

import com.pbu.sam.dtos.PermissionDto;
import com.pbu.sam.entities.*;
import com.pbu.sam.exceptions.DbRecordNotFound;
import com.pbu.sam.repositories.SystemLogRepository;
import com.pbu.sam.repositories.SystemPermissionRepository;
import com.pbu.sam.repositories.SystemUserRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class PermissionServiceImp implements PermissionService{

    private final List<PermissionDto> records = new ArrayList<>();
    private final ModelMapper mapper;
    private final SystemUserRepository usersRepo;
    private final SystemLogRepository logRepo;
    private final SystemPermissionRepository permissionRepo;

    public PermissionServiceImp(ModelMapper mapper, SystemUserRepository usersRepo, SystemLogRepository logRepo, SystemPermissionRepository permissionRepo) {
        this.mapper = mapper;
        this.usersRepo = usersRepo;
        this.logRepo = logRepo;
        this.permissionRepo = permissionRepo;
    }

    @Async
    @Override
    public CompletableFuture<PermissionDto> findById(long id, String ip, long userId) {
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }
        //create log record
        SystemLog log = new SystemLog();
        log.setAction(String.format("Retrieved system permission record with ID '%s'", id));
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);

        SystemPermission permission = permissionRepo.findById(id).orElse(null);
        if(permission == null){
            throw new DbRecordNotFound("Permission", "PermissionId", String.format("%s",id));
        }
        PermissionDto record = this.mapper.map(permission, PermissionDto.class);
        return CompletableFuture.completedFuture(record);
    }

    @Override
    public CompletableFuture<PermissionDto>  findByName(String name, String ip, long userId) {
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }

        SystemLog log = new SystemLog();
        log.setAction(String.format("Retrieved system permission record with name '%s'", name));
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);

        SystemPermission permission = permissionRepo.findByName(name).orElse(null);
        if(permission == null){
            throw new DbRecordNotFound("Permission", "Name", String.format("%s",name));
        }
        PermissionDto record = this.mapper.map(permission, PermissionDto.class);
        return CompletableFuture.completedFuture(record);
    }

    @Async
    @Transactional
    @Override
    public CompletableFuture<List<PermissionDto>> getAll(String ip, long userId) {
        //..create log
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }
        SystemLog log = new SystemLog();
        log.setAction("Retrieving a list of system permissions");
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);

        List<SystemPermission> permissions = permissionRepo.findAll();
        if(permissions.isEmpty()){
            return CompletableFuture.completedFuture(records);
        }

        //map permissions to DTO
        for (SystemPermission permission : permissions) {
            records.add(this.mapper.map(permission, PermissionDto.class));
        }

        return CompletableFuture.completedFuture(records);
    }

    @Async
    @Transactional
    @Override
    public void updatePermission(PermissionDto permission, String ip, long userId) {
        //..create log
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }

        SystemLog log = new SystemLog();
        log.setAction(String.format("Updating system permission description for permission with Id %s" , permission.getId()));
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);

        SystemPermission record = this.permissionRepo.findById(permission.getId()).orElse(null);
        if(record == null){
            log = new SystemLog();
            log.setAction(String.format("Updating system permission with id '%s' failed" , permission.getId()));
            log.setIpAddress(ip);
            log.setLogTime(LocalDateTime.now());
            log.setUser(loggedIn);
            logRepo.save(log);
        } else {
            this.permissionRepo.updatePermission(record);
        }
    }
}