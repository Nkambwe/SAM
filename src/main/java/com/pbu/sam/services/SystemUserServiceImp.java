package com.pbu.sam.services;

import com.pbu.sam.dtos.UserDto;
import com.pbu.sam.entities.SystemLog;
import com.pbu.sam.entities.SystemUser;
import com.pbu.sam.exceptions.DbRecordNotFound;
import com.pbu.sam.repositories.SystemLogRepository;
import com.pbu.sam.repositories.SystemUserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class SystemUserServiceImp implements SystemUserService {

    private final List<UserDto> records = new ArrayList<>();
    private final ModelMapper mapper;
    private final SystemUserRepository usersRepo;
    private final SystemLogRepository logRepo;

    public SystemUserServiceImp(ModelMapper mapper, SystemUserRepository usersRepo, SystemLogRepository logRepo) {
        this.mapper = mapper;
        this.usersRepo = usersRepo;
        this.logRepo = logRepo;
    }

    @Async
    @Transactional
    @Override
    public CompletableFuture<UserDto> findById(long id, String ip, long userId) {
        SystemUser user = usersRepo.findById(id).orElse(null);
        if(user != null){
            UserDto record = this.mapper.map(user, UserDto.class);

            //..create log
            SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
            if(loggedIn != null){
                SystemLog log = new SystemLog();
                log.setAction(String.format("Retrieved user with Id '%s'", id));
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
    public CompletableFuture<UserDto> findByUsername(String username, String ip, long userId) {
        SystemUser user = usersRepo.findByUsernameIgnoreCase(username);
        if(user != null){
            UserDto record = this.mapper.map(user, UserDto.class);

            //..create log
            SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
            if(loggedIn != null){
                SystemLog log = new SystemLog();
                log.setAction(String.format("Retrieved user with username '%s'", username));
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
    public CompletableFuture<UserDto> findByPfNo(String pfNo, String ip, long userId) {
        SystemUser user = usersRepo.findByPfNoIgnoreCase(pfNo);
        if(user != null){
            UserDto record = this.mapper.map(user, UserDto.class);
            //..create log
            SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
            if(loggedIn != null){
                SystemLog log = new SystemLog();
                log.setAction(String.format("Retrieved user with PF number '%s'", pfNo));
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
    public CompletableFuture<List<UserDto>> getAll(String ip, long userId) {

        //..create log
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }
        SystemLog log = new SystemLog();
        log.setAction("Retrieved list of users");
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);

        List<SystemUser> users = usersRepo.findAll();
        if(!users.isEmpty()){
            for (SystemUser user : users) {
                records.add(this.mapper.map(user, UserDto.class));
            }
        }

        return CompletableFuture.completedFuture(records);
    }

    @Transactional
    @Override
    public void softDeleted(long id, String ip, long userId) {
        //..create log
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }
        SystemLog log = new SystemLog();
        log.setAction(String.format("Delete user with ID %s", id));
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);

        usersRepo.updateIsDeletedById(id);
    }

    @Override
    public boolean isDeleted(long userId){
        return usersRepo.isDeleted(userId);
    }

    @Override
    public boolean isCreator(long userId, String creator){
        return usersRepo.isCreator(userId, creator);
    }

    @Transactional
    @Override
    public void updateRecord(UserDto user, String ip, long userId) throws DbRecordNotFound  {
        SystemUser loggedUser = usersRepo.findById(userId).orElse(null);
        //..create log
        if(loggedUser == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }

        usersRepo.updateUserRecord(this.mapper.map(user, SystemUser.class));

        SystemLog log = new SystemLog();
        log.setAction(String.format("Updating user record for '%s'",  user.getUsername()));
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedUser);
        logRepo.save(log);
    }

    @Transactional
    @Override
    public void verifiedUser(boolean verify, String modifiedBy, String modifiedOn, long id, String ip, long userId) {
        SystemUser loggedUser = usersRepo.findById(userId).orElse(null);
        //..create log
        if(loggedUser == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }

        usersRepo.updateIsVerifiedById(verify,modifiedBy,modifiedOn, id);

        SystemLog log = new SystemLog();
        log.setAction(String.format("Verifying user record for user ID '%s'", id));
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedUser);
        logRepo.save(log);
    }

    @Transactional
    @Override
    public void activeUser(boolean active, String modifiedBy, String modifiedOn, long id, String ip, long userId) {
        SystemUser loggedUser = usersRepo.findById(userId).orElse(null);
        //..create log
        if(loggedUser == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }

        usersRepo.updateIsActiveById(active, modifiedBy, modifiedOn, id);

        SystemLog log = new SystemLog();
        log.setAction(String.format("Changing user active status to '%s' for user record with user ID '%s'", active, id));
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedUser);
        logRepo.save(log);
    }

    @Transactional
    @Override
    public void updateLoginStatus(boolean loggedIn, long id, String ip, long userId) {

        SystemUser loggedUser = usersRepo.findById(userId).orElse(null);
        if(loggedUser == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }
        //retrieve record
        usersRepo.updateIsLoggedInById(loggedIn, id);

        //create log record
        SystemLog log = new SystemLog();
        log.setAction(String.format("Changing user logged status to '%s' for user record with user ID '%s'", loggedIn, id));
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedUser);
        logRepo.save(log);
    }

    @Transactional
    @Override
    public void updatePassword(String password, long id, String ip, long userId) {
        SystemUser loggedUser = usersRepo.findById(userId).orElse(null);
        if(loggedUser == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }

        //retrieve record
        usersRepo.updatePasswordById(password, id);

        //create log record
        SystemLog log = new SystemLog();
        log.setAction(String.format("Update user password for user record with user ID '%s'", id));
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedUser);
        logRepo.save(log);
    }

    @Override
    public boolean exists(long id) {
        return usersRepo.existsById(id);
    }

    @Override
    public boolean pfNoTaken(String pfNo) {
        return usersRepo.existsByPfNoIgnoreCase(pfNo);
    }

    @Override
    public boolean pfNoDuplicated(String pfNo, long id) {
        return usersRepo.existsByPfNoAndIdNot(pfNo, id);
    }

    @Override
    public boolean usernameTaken(String username) {
        return usersRepo.existsByUsernameIgnoreCase(username);
    }

    @Override
    public boolean usernameDuplicated(String username, long id) {
        return usersRepo.existsByUsernameAndIdNot(username, id);
    }

    @Transactional
    @Override
    public CompletableFuture<UserDto> create(UserDto user, String ip, long userId) throws InterruptedException {
        //map record
        SystemUser record = this.mapper.map(user, SystemUser.class);

        //save record
        usersRepo.save(record);
        //..create log
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn != null){
            SystemLog log = new SystemLog();
            log.setAction(String.format("Adding system user '%s'",user.getUsername()));
            log.setIpAddress(ip);
            log.setLogTime(LocalDateTime.now());
            log.setUser(loggedIn);
            logRepo.save(log);
        }

        //set user id
        user.setId(record.getId());
        return CompletableFuture.completedFuture(user);
    }
}
