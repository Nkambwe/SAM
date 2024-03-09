package com.pbu.sam.services;

import com.pbu.sam.dtos.BranchDto;
import com.pbu.sam.entities.SystemBranch;
import com.pbu.sam.entities.SystemLog;
import com.pbu.sam.entities.SystemUser;
import com.pbu.sam.exceptions.DbRecordNotFound;
import com.pbu.sam.repositories.BranchRepository;
import com.pbu.sam.repositories.SystemLogRepository;
import com.pbu.sam.repositories.SystemUserRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class BranchServiceImp implements BranchService{
    private final List<BranchDto> records = new ArrayList<>();
    private final ModelMapper mapper;
    private final BranchRepository branchRepo;
    private final SystemUserRepository usersRepo;
    private final SystemLogRepository logRepo;

    public BranchServiceImp(ModelMapper mapper, BranchRepository branchRepo, SystemUserRepository usersRepo, SystemLogRepository logRepo) {
        this.mapper = mapper;
        this.branchRepo = branchRepo;
        this.usersRepo = usersRepo;
        this.logRepo = logRepo;
    }

    @Override
    public BranchDto findById(long id){
        SystemBranch branch = branchRepo.findById(id).orElse(null);
        if(branch != null){
            return this.mapper.map(branch, BranchDto.class);
        }
        return null;
    }

    @Transactional
    @Override
    public CompletableFuture<BranchDto>  findBySolId(String solId, String ip, long userId) {
        //..create log
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }
        SystemLog log = new SystemLog();
        log.setAction(String.format("Retrieve branch record with solId %s", solId));
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);

        SystemBranch branch = branchRepo.findBySolId(solId);
        if(branch != null){
            BranchDto record = this.mapper.map(branch, BranchDto.class);
            return CompletableFuture.completedFuture(record);
        }
        return null;
    }

    @Transactional
    @Override
    public CompletableFuture<List<BranchDto>> getAll(String ip, long userId) {

        //..create log
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }
        SystemLog log = new SystemLog();
        log.setAction("Retrieved list of branches");
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);

        List<SystemBranch> branches = branchRepo.findAll();
        if(!branches.isEmpty()){
            for (SystemBranch record : branches) {
                records.add(this.mapper.map(record, BranchDto.class));
            }
        }

        return CompletableFuture.completedFuture(records);
    }

    public boolean  checkIfExistsById(long id){
        return  branchRepo.existsById(id);
    }

    @Override
    public boolean checkIfExistsBySolId(String solId) {
        return  branchRepo.existsBySolId(solId);
    }

    @Override
    public boolean checkSolIdDuplication(long branchId, String solId) {
        return branchRepo.existsBySolIdAndIdNot(solId, branchId);
    }

    @Override
    public boolean checkIfExistsByName(String branchName) {
        return  branchRepo.existsByBranchName(branchName);
    }

    @Override
    public boolean checkNameDuplication(long branchId, String branchName) {
        return branchRepo.existsByBranchNameAndIdNot(branchName, branchId);
    }

    @Transactional
    @Override
    public void activateBranch(long id, boolean status, String ip, long userId) {
        //..create log
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }
        SystemLog log = new SystemLog();
        log.setAction(String.format("Activate branch record with ID %s",id));
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);
        branchRepo.updateIsActiveById(id, status);
    }

    @Transactional
    @Override
    public CompletableFuture<BranchDto> create(BranchDto branch, String ip, long userId) throws InterruptedException {
        //..create log
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }
        SystemLog log = new SystemLog();
        log.setAction("Add new branch record");
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);

        //map record
        SystemBranch record = this.mapper.map(branch, SystemBranch.class);
        branchRepo.save(record);

        //set branch id
        branch.setId(record.getId());
        return CompletableFuture.completedFuture(branch);
    }

    @Transactional
    @Override
    public void updateBranch(BranchDto branch, String ip, long userId) {
        //..create log
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }
        SystemLog log = new SystemLog();
        log.setAction(String.format("Update branch record with ID %s",branch.getId()));
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);

        branchRepo.updateBranch(this.mapper.map(branch, SystemBranch.class));
    }

    @Transactional
    @Override
    public void deleteBranch(long id, String ip, long userId){
        //..create log
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }
        SystemLog log = new SystemLog();
        log.setAction(String.format("Delete branch record with ID %s", id));
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);

        branchRepo.isDeleted(id);
    }

    @Transactional
    @Override
    public void purgeBranch(long id, String ip, long userId){
        //..create log
        SystemUser loggedIn = usersRepo.findById(userId).orElse(null);
        if(loggedIn == null){
            throw new DbRecordNotFound("User", "UserId", String.format("%s", userId));
        }
        SystemLog log = new SystemLog();
        log.setAction(String.format("Permanently delete branch record with ID %s", id));
        log.setIpAddress(ip);
        log.setLogTime(LocalDateTime.now());
        log.setUser(loggedIn);
        logRepo.save(log);

        branchRepo.deleteById(id);
    }
}
