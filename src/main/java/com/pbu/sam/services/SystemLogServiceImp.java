package com.pbu.sam.services;
import com.pbu.sam.dtos.LogDto;
import com.pbu.sam.entities.SystemLog;
import com.pbu.sam.entities.SystemUser;
import com.pbu.sam.exceptions.DbRecordNotFound;
import com.pbu.sam.repositories.SystemLogRepository;
import com.pbu.sam.repositories.SystemUserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class SystemLogServiceImp implements SystemLogService {
    private final ModelMapper mapper;
    private final List<LogDto> records = new ArrayList<>();
    private final SystemLogRepository logRepo;
    private final SystemUserRepository usersRepo;

    public SystemLogServiceImp(ModelMapper mapper, SystemLogRepository logRepo, SystemUserRepository usersRepo) {
        this.mapper = mapper;
        this.logRepo = logRepo;
        this.usersRepo = usersRepo;
    }

    @Async
    @Override
    public void create(LogDto log) {
        SystemLog record = this.mapper.map(log, SystemLog.class);
        logRepo.save(record);
    }

    @Async
    @Override
    public CompletableFuture<List<LogDto>> getLogs(String ip, long userId) {
        //..create log
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }

        SystemLog log = new SystemLog();
        log.setAction("Retrieving a list of system logs");
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);

        List<SystemLog> logs = logRepo.findAll();
        if(logs.isEmpty()){
            return CompletableFuture.completedFuture(records);
        }

        for (SystemLog record : logs) {
            LogDto mappedLog = this.mapper.map(record, LogDto.class);
            records.add(mappedLog);
        }

        return CompletableFuture.completedFuture(records);
    }

    @Async
    @Override
    public CompletableFuture<List<LogDto>> getLogs(String ip, long userId, LocalDateTime startDate, LocalDateTime endDate) {

        //..create log
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }

        SystemLog log = new SystemLog();
        log.setAction("Retrieving a list of system logs between");
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);

        List<SystemLog> logs = logRepo.getLogs(startDate, endDate);
        if(logs.isEmpty()){
            return CompletableFuture.completedFuture(records);
        }

        for (SystemLog record : logs) {
            LogDto mappedLog = this.mapper.map(record, LogDto.class);
            records.add(mappedLog);
        }

        return CompletableFuture.completedFuture(records);
    }

}

