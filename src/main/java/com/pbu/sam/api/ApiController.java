package com.pbu.sam.api;

import com.pbu.sam.common.AppLoggerService;
import com.pbu.sam.common.NetworkService;
import com.pbu.sam.common.Secure;
import com.pbu.sam.dtos.*;
import com.pbu.sam.exceptions.*;
import com.pbu.sam.services.*;
import com.pbu.sam.utils.Generators;
import com.pbu.sam.utils.Literals;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
public class ApiController {
    private final AppLoggerService logger;
    private final WendiExceptionHandler errorHandler;
    private final NetworkService networkService;
    private final SystemUserService userService;
    private final BranchService branchService;
    private final RoleService roleService;
    private final PermissionSetService setService;
    private final PermissionService permissionService;
    private final SystemLogService logService;

    public ApiController(AppLoggerService logger,
                         WendiExceptionHandler errorHandler,
                         NetworkService networkService,
                         SystemUserService userService,
                         BranchService branchService,
                         RoleService roleService, PermissionSetService setService, PermissionService permissionService, SystemLogService logService) {
        this.logger = logger;
        this.errorHandler = errorHandler;
        this.networkService = networkService;
        this.userService = userService;
        this.branchService = branchService;
        this.roleService = roleService;
        this.setService = setService;
        this.permissionService = permissionService;
        this.logService = logService;
    }

    //region Branches

    @Async
    @GetMapping("/getBranch/{solId}/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> getBranch(@PathVariable("solId") String solId, @PathVariable("loggedUserId") long loggedUserId, HttpServletRequest request){
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();

        //try finding resource
        String ip = networkService.getIncomingIpAddress(request);
        CompletableFuture<BranchDto> branch = this.branchService.findBySolId(solId, ip, loggedUserId);

        // Wait for the CompletableFuture to complete and get the result
        BranchDto record = branch.join();

        //branch not found, throw NOT_FOUND response
        if(record == null){
            ResponseEntity<WendiError> error = errorHandler.resourceNotFoundExceptionHandler(
                    new WendiResourceNotFoundException("Branch","SolId",solId),
                    request);
            future.complete(error);
            return future;
        }

        //return user record
        future.complete(new ResponseEntity<>(record, HttpStatus.OK));
        return future;
    }

    @Async
    @GetMapping("/getBranches/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> getBranches(@PathVariable Long loggedUserId, HttpServletRequest request) {
        String date = Generators.currentDate();
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();
        logger.info(String.format("Retrieving a list of branches. User with id %s on %s",loggedUserId, date));

        //get client ip address
        String ip = networkService.getIncomingIpAddress(request);
        CompletableFuture<List<BranchDto>> branches = branchService.getAll(ip, loggedUserId);

        // Wait for the CompletableFuture to complete and get the result
        List<BranchDto> records = branches.join();
        future.complete(ResponseEntity.ok(records));
        return future;
    }

    @Async
    @PutMapping("/activateBranch/{id}/{isActive}/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> activateBranch(@PathVariable Long id, @PathVariable Boolean isActive, @PathVariable Long loggedUserId, HttpServletRequest request){
        String date = Generators.currentDate();
        logger.info(String.format("Activate branch record. User with id %s on %s",loggedUserId, date));

        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();

        //check if user exists
        if (!branchService.checkIfExistsById(id)) {
            ResponseEntity<WendiError> error = errorHandler.resourceNotFoundExceptionHandler(
                    new WendiResourceNotFoundException("Branch", "ID",id),
                    request);
            future.complete(error);
            return future;
        }

        //make sure verifying user is not the same who created the record
        String ip = networkService.getIncomingIpAddress(request);
        branchService.activateBranch(id, isActive, ip, loggedUserId);

        //return result
        future.complete(new ResponseEntity<>("User activate successfully", HttpStatus.OK));
        return future;
    }

    @Async
    @PostMapping("/createBranch/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>>  createBranch(@RequestBody @Valid BranchDto branch, @PathVariable Long loggedUserId, BindingResult bindingResult, HttpServletRequest request){
        String date = Generators.currentDate();
        logger.info(String.format("Creating new branch %s on %s",branch.getBranchName(), date));
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();

        //Validate request object
        logger.info("Validating branch record >>>");
        if (bindingResult.hasErrors()) {
            logger.info("Branch record Validation error....");
            WendiRequestValidationException validationException =  new WendiRequestValidationException(Generators.buildErrorMessage(bindingResult));
            logger.info(String.format("%s",validationException.getMessage()));
            future.complete(errorHandler.validationExceptionHandler(validationException,request));
            return future;
        }

        try{
            //check whether branch name is not in use
            logger.info("Checking whether branch assigned name is not in use...");
            String branchName = branch.getBranchName();
            boolean exists = this.branchService.checkIfExistsByName(branchName);
            if(exists){
                logger.info(String.format("Resource Conflict! Another branch with name '%s' exists", branchName));
                ResponseEntity<WendiError> error = errorHandler.duplicatesResourceExceptionHandler(
                        new WendiConflictResourceException("Branch", "Name", branchName),
                        request);
                future.complete(error);
                return future;
            }

            //check whether branch Sol ID is not in use
            logger.info("Checking whether branch assigned SOLID is not in use...");
            String solId = branch.getSolid();
            exists = this.branchService.checkIfExistsBySolId(solId);
            if(exists){
                logger.info(String.format("Resource Conflict! Another branch with SolId '%s' exists", solId));
                ResponseEntity<WendiError> error = errorHandler.duplicatesResourceExceptionHandler(
                        new WendiConflictResourceException("Branch", "SolID", solId),
                        request);
                future.complete(error);
                return future;
            }

            //save role to database
            String ip = networkService.getIncomingIpAddress(request);
            CompletableFuture<BranchDto> record = this.branchService.create(branch, ip, loggedUserId);
            BranchDto result = record.join();

            //return branch record
            future.complete(new ResponseEntity<>(result, HttpStatus.OK));
            return future;
        } catch (InterruptedException e) {
            ResponseEntity<WendiError> error = errorHandler.threadCanceledHandler(
                    new WendiThreadCanceledException(),
                    request);

            future.complete(error);
            //log error
            logger.info("Error creating branch");
            logger.info("StackTrace Details >>>>>");
            String stackTrace = ExceptionUtils.getStackTrace(e);
            logger.info(stackTrace);
            return future;
        }
    }

    @Async
    @PutMapping("/updateBranch/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> updateBranch(@RequestBody @Valid BranchDto branch, @PathVariable Long loggedUserId, BindingResult bindingResult, HttpServletRequest request){
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();
        String date = Generators.currentDate();
        logger.info(String.format("Modifying branch with name %s on %s",branch.getBranchName(), date));

        //Validate request object
        logger.info("Validating role record >>>");
        if (bindingResult.hasErrors()) {
            logger.info("Branch record Validation error....");
            WendiRequestValidationException validationException =  new WendiRequestValidationException(Generators.buildErrorMessage(bindingResult));
            logger.info(String.format("%s",validationException.getMessage()));
            future.complete(errorHandler.validationExceptionHandler(validationException,request));
            return future;
        }

        try{
            //check whether branch name is not in use
            logger.info("Checking whether branch assigned name is not in use...");
            String branchName = branch.getBranchName();
            boolean exists = this.branchService.checkNameDuplication(branch.getId(),branchName);
            if(exists){
                logger.info(String.format("Resource Conflict! Another branch with name '%s' exists", branchName));
                ResponseEntity<WendiError> error = errorHandler.duplicatesResourceExceptionHandler(
                        new WendiConflictResourceException("Branch", "Name", branchName),
                        request);
                future.complete(error);
                return future;
            }

            //check whether branch Sol ID is not in use
            logger.info("Checking whether branch assigned SOLID is not in use...");
            String solId = branch.getSolid();
            exists = this.branchService.checkSolIdDuplication(branch.getId(),solId);
            if(exists){
                logger.info(String.format("Resource Conflict! Another branch with SolId '%s' exists", solId));
                ResponseEntity<WendiError> error = errorHandler.duplicatesResourceExceptionHandler(
                        new WendiConflictResourceException("Branch", "SolID", solId),
                        request);
                future.complete(error);
                return future;
            }

            //save branch to database
            String ip = networkService.getIncomingIpAddress(request);
            this.branchService.updateBranch(branch, ip, loggedUserId);

            //return branch record
            future.complete(new ResponseEntity<>(branch, HttpStatus.OK));
            return future;
        } catch (Exception e) {
            ResponseEntity<WendiError> error = errorHandler.threadCanceledHandler(
                    new WendiThreadCanceledException(),
                    request);

            future.complete(error);
            //log error
            logger.info("Error updating system role");
            logger.info("StackTrace Details >>>>>");
            String stackTrace = ExceptionUtils.getStackTrace(e);
            logger.info(stackTrace);
            return future;
        }
    }

    @Async
    @DeleteMapping("/softDeleteBranch/{id}/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> softDeleteBranch(@PathVariable Long id, @PathVariable Long loggedUserId, HttpServletRequest request) {
        String date = Generators.currentDate();
        logger.info(String.format("Deleting branch record. Role with id %s on %s",loggedUserId, date));

        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();
        if (!branchService.checkIfExistsById(id)) {
            ResponseEntity<WendiError> error = errorHandler.resourceNotFoundExceptionHandler(
                    new WendiResourceNotFoundException("Branch", "BranchId",id),
                    request);
            future.complete(error);
            return future;
        }

        //delete branch
        String ip = networkService.getIncomingIpAddress(request);
        branchService.deleteBranch(id, ip, loggedUserId);

        //return result
        future.complete(new ResponseEntity<>("Branch deleted successfully", HttpStatus.OK));
        return future;
    }

    @Async
    @DeleteMapping("/deleteBranch/{id}/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> deleteBranch(@PathVariable Long id, @PathVariable Long loggedUserId, HttpServletRequest request) {
        String date = Generators.currentDate();
        logger.info(String.format("Deleting branch record. Branch with id %s on %s",loggedUserId, date));

        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();
        if (!branchService.checkIfExistsById(id)) {
            ResponseEntity<WendiError> error = errorHandler.resourceNotFoundExceptionHandler(
                    new WendiResourceNotFoundException("Branch", "BranchId",id),
                    request);
            future.complete(error);
            return future;
        }

        //delete branch
        String ip = networkService.getIncomingIpAddress(request);
        branchService.purgeBranch(id, ip, loggedUserId);

        //return result
        future.complete(new ResponseEntity<>("Branch deleted successfully", HttpStatus.OK));
        return future;
    }

    //endregion

    //region system users
    @Async
    @GetMapping("/getUserById/{id}/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> getUserById(@PathVariable("id") long userId, @PathVariable("loggedUserId") long loggedUserId, HttpServletRequest request){
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();

        //try finding resource
        String ip = networkService.getIncomingIpAddress(request);
        CompletableFuture<UserDto> user = this.userService.findById(userId, ip, loggedUserId);

        // Wait for the CompletableFuture to complete and get the result
        UserDto record = user.join();

        //user not found, throw NOT_FOUND response
        if(record == null){
            ResponseEntity<WendiError> error = errorHandler.resourceNotFoundExceptionHandler(
                    new WendiResourceNotFoundException("User","UserId",userId),
                    request);
            future.complete(error);
            return future;
        }

        //decrypt user fields
        logger.info("Decrypting email, firstname and lastname...");

        String decryptedEmail = Secure.decrypt(record.getEmail(), Literals.HASH_KEY, logger, "User Email");
        if(decryptedEmail != null){
            record.setEmail(decryptedEmail);
            logger.info(String.format("Decrypted User Email value >>> %s", decryptedEmail));
        } else {
            ResponseEntity<WendiError> error = errorHandler.exceptionHandler(
                    new WendiException("Processing Error! An Error occurred while decrypting User Email"),
                    request);
            future.complete(error);
            return future;
        }

        String decryptedFirstName = Secure.decrypt(record.getFirstname(), Literals.HASH_KEY, logger, "FirstName");
        if(decryptedFirstName != null){
            record.setFirstname(decryptedFirstName);
            logger.info(String.format("Encrypted Firstname value >>> %s", decryptedFirstName));
        } else {
            ResponseEntity<WendiError> error = errorHandler.exceptionHandler(
                    new WendiException("Processing Error! An Error occurred while decrypting FirstName"),
                    request);
            future.complete(error);
            return future;
        }

        String decryptedLastName = Secure.decrypt(record.getLastname(), Literals.HASH_KEY, logger, "LastName");
        if(decryptedLastName != null){
            record.setLastname(decryptedLastName);
            logger.info(String.format("Decrypted Lastname value >>> %s", decryptedLastName));
        } else {
            ResponseEntity<WendiError> error = errorHandler.exceptionHandler(
                    new WendiException("Processing Error! An Error occurred while decrypting LastName"),
                    request);
            future.complete(error);
            return future;
        }

        //return user record
        future.complete(new ResponseEntity<>(record, HttpStatus.OK));
        return future;
    }

    @Async
    @GetMapping("/getByUsername/{username}/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> getUserByUsername(@PathVariable("username") String username, @PathVariable("loggedUserId") long loggedUserId, HttpServletRequest request){
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();

        //get client ip address
        String ip = networkService.getIncomingIpAddress(request);
        CompletableFuture<UserDto> user = this.userService.findByUsername(username, ip, loggedUserId);

        // Wait for the CompletableFuture to complete and get the result
        UserDto record = user.join();

        //user not found, throw NOT_FOUND response
        if(record == null){
            ResponseEntity<WendiError> error = errorHandler.resourceNotFoundExceptionHandler(
                    new WendiResourceNotFoundException("User","Username",username),
                    request);
            future.complete(error);
            return future;
        }

        //decrypt user fields
        logger.info("Decrypting email, firstname and lastname...");
        String decryptedEmail = Secure.decrypt(record.getEmail(), Literals.HASH_KEY, logger, "User Email");
        if(decryptedEmail != null){
            record.setEmail(decryptedEmail);
            logger.info(String.format("Decrypted User Email value >>> %s", decryptedEmail));
        } else {
            ResponseEntity<WendiError> error =  errorHandler.exceptionHandler(
                    new WendiException("Processing Error! An Error occurred while decrypting User Email"),
                    request);
            future.complete(error);
            return future;
        }

        String decryptedFirstName = Secure.decrypt(record.getFirstname(), Literals.HASH_KEY, logger, "FirstName");
        if(decryptedFirstName != null){
            record.setFirstname(decryptedFirstName);
            logger.info(String.format("Encrypted Firstname value >>> %s", decryptedFirstName));
        } else {
            ResponseEntity<WendiError> error =  errorHandler.exceptionHandler(
                    new WendiException("Processing Error! An Error occurred while decrypting FirstName"),
                    request);
            future.complete(error);
            return future;
        }

        String decryptedLastName = Secure.decrypt(record.getLastname(), Literals.HASH_KEY, logger, "LastName");
        if(decryptedLastName != null){
            record.setLastname(decryptedLastName);
            logger.info(String.format("Decrypted Lastname value >>> %s", decryptedLastName));
        } else {
            ResponseEntity<WendiError> error =  errorHandler.exceptionHandler(
                    new WendiException("Processing Error! An Error occurred while decrypting LastName"),
                    request);
            future.complete(error);
            return future;
        }

        //return user record
        future.complete(new ResponseEntity<>(record, HttpStatus.OK));
        return future;
    }

    @Async
    @GetMapping("/getByPfNo/{pfNo}/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> getUserByPfNo(@PathVariable("pfNo") String pfNo, @PathVariable("loggedUserId") long loggedUserId, HttpServletRequest request){
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();

         //try finding resource
        String ip = networkService.getIncomingIpAddress(request);
        CompletableFuture<UserDto> user = this.userService.findByPfNo(pfNo, ip, loggedUserId);
        // Wait for the CompletableFuture to complete and get the result
        UserDto record = user.join();

        //user not found, throw NOT_FOUND response
        if(record == null){
            ResponseEntity<WendiError> error = errorHandler.resourceNotFoundExceptionHandler(
                    new WendiResourceNotFoundException("User","PF Number",pfNo),
                    request);
            future.complete(error);
            return future;
        }

        logger.info("Decrypting email, firstname and lastname...");
        String decryptedEmail = Secure.decrypt(record.getEmail(), Literals.HASH_KEY, logger, "User Email");
        if(decryptedEmail != null){
            record.setEmail(decryptedEmail);
            logger.info(String.format("Decrypted User Email value >>> %s", decryptedEmail));
        } else {
            ResponseEntity<WendiError> error = errorHandler.exceptionHandler(
                    new WendiException("Processing Error! An Error occurred while decrypting User Email"),
                    request);
            future.complete(error);
            return future;
        }

        String decryptedFirstName = Secure.decrypt(record.getFirstname(), Literals.HASH_KEY, logger, "FirstName");
        if(decryptedFirstName != null){
            record.setFirstname(decryptedFirstName);
            logger.info(String.format("Encrypted Firstname value >>> %s", decryptedFirstName));
        } else {
            ResponseEntity<WendiError> error = errorHandler.exceptionHandler(
                    new WendiException("Processing Error! An Error occurred while decrypting FirstName"),
                    request);
            future.complete(error);
            return future;
        }

        String decryptedLastName = Secure.decrypt(record.getLastname(), Literals.HASH_KEY, logger, "LastName");
        if(decryptedLastName != null){
            record.setLastname(decryptedLastName);
            logger.info(String.format("Decrypted Lastname value >>> %s", decryptedLastName));
        } else {
            ResponseEntity<WendiError> error = errorHandler.exceptionHandler(
                    new WendiException("Processing Error! An Error occurred while decrypting LastName"),
                    request);
            future.complete(error);
            return future;
        }

         //return user record
         future.complete(new ResponseEntity<>(record, HttpStatus.OK));
         return future;
    }

    @Async
    @GetMapping("/getUsers/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> getAllUsers(@PathVariable Long loggedUserId, HttpServletRequest request) {
        String date = Generators.currentDate();
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();
        logger.info(String.format("Retrieving a list of users. User with id %s on %s",loggedUserId, date));

        //get client ip address
        String ip = networkService.getIncomingIpAddress(request);
        CompletableFuture<List<UserDto>> users = userService.getAll(ip, loggedUserId);

        // Wait for the CompletableFuture to complete and get the result
        List<UserDto> records = users.join();

        //decrypt fields
        List<UserDto> result = new ArrayList<>();
        for (UserDto user :records){
            logger.info("Decrypting email, firstname and lastname...");
            String decryptedEmail = Secure.decrypt(user.getEmail(), Literals.HASH_KEY, logger, "User Email");
            if(decryptedEmail != null){
                user.setEmail(decryptedEmail);
                logger.info(String.format("Decrypted User Email value >>> %s", decryptedEmail));
            } else {
                ResponseEntity<WendiError> error = errorHandler.exceptionHandler(
                        new WendiException("Processing Error! An Error occurred while decrypting User Email"),
                        request);
                future.complete(error);
                return future;
            }

            String decryptedFirstName = Secure.decrypt(user.getFirstname(), Literals.HASH_KEY, logger, "FirstName");
            if(decryptedFirstName != null){
                user.setFirstname(decryptedFirstName);
                logger.info(String.format("Encrypted Firstname value >>> %s", decryptedFirstName));
            } else {
                ResponseEntity<WendiError> error = errorHandler.exceptionHandler(
                        new WendiException("Processing Error! An Error occurred while decrypting FirstName"),
                        request);

                future.complete(error);
                return future;
            }

            String decryptedLastName = Secure.decrypt(user.getLastname(), Literals.HASH_KEY, logger, "LastName");
            if(decryptedLastName != null){
                user.setLastname(decryptedLastName);
                logger.info(String.format("Decrypted Lastname value >>> %s", decryptedLastName));
            } else {
                ResponseEntity<WendiError> error = errorHandler.exceptionHandler(
                        new WendiException("Processing Error! An Error occurred while decrypting LastName"),
                        request);

                future.complete(error);
                return future;
            }

            result.add(user);
        }

        future.complete(ResponseEntity.ok(result));
        return future;
    }

    @Async
    @PostMapping("/createUser")
    public CompletableFuture<ResponseEntity<?>>  createUser(@RequestBody @Valid UserDto user, BindingResult bindingResult, HttpServletRequest request) {
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();
        String date = Generators.currentDate();
        logger.info(String.format("Creating new user %s on %s",user.getUsername(), date));

        //Validate request object
        logger.info("Validating user record >>>");
        if (bindingResult.hasErrors()) {
            logger.info("User record Validation error....");
            WendiRequestValidationException validationException =  new WendiRequestValidationException(Generators.buildErrorMessage(bindingResult));
            logger.info(String.format("%s",validationException.getMessage()));
            future.complete(errorHandler.validationExceptionHandler(validationException,request));
            return future;
        }

        //get records data
        UserDto userRec;
        try {

            //make sure assigned branch exists
            BranchDto branch = branchService.findById(user.getBranchId());
            if(branch == null){
                logger.info(String.format("Resource not found! Branch with BranchId '%s' not found", user.getBranchId()));
                ResponseEntity<WendiError> error = errorHandler.resourceNotFoundExceptionHandler(
                        new WendiResourceNotFoundException("Branch", "BranchId", String.format("%s", user.getBranchId())),
                        request);
                future.complete(error);
                return future;
            }

            //check whether user is assigned to an active branch
            logger.info("Checking whether user assigned branch is active...");
            if(!branch.isActive()){
                logger.info(String.format("Branch with BranchId '%s' is inactive", branch.getId()));
                ResponseEntity<WendiError> error = errorHandler.resourceInactiveExceptionHandler(
                        new WendiResourceNotActiveException("Branch", "BranchId", String.format("%s", branch.getId())),
                        request);
                future.complete(error);
            }

            //make sure assigned role exists
            RoleDto role = roleService.findById(user.getRoleId());
            if(role == null){
                logger.info(String.format("Resource not found! Role with RoleId '%s' not found", user.getRoleId()));
                ResponseEntity<WendiError> error = errorHandler.resourceNotFoundExceptionHandler(
                        new WendiResourceNotFoundException("Role", "RoleId", String.format("%s", user.getRoleId())),
                        request);
                future.complete(error);
                return future;
            }

            //check whether username is not in use
            logger.info("Checking whether user assigned username is not in use...");
            String username = user.getUsername();
            boolean exists = this.userService.usernameTaken(username);
            if(exists){
                logger.info(String.format("Resource Conflict! Another user with name '%s' exists", username));
                ResponseEntity<WendiError> error = errorHandler.duplicatesResourceExceptionHandler(
                        new WendiConflictResourceException("User", "Username", username),
                        request);
                future.complete(error);
                return future;
            }

            //check if PfNo does not exist
            logger.info("Checking whether user assigned PF Number is not in use...");
            String pfNo = user.getPfNo();
            exists = this.userService.pfNoTaken(pfNo);
            if(exists){
                logger.info(String.format("Resource Conflict! Another user with PF number '%s' found", pfNo));
                ResponseEntity<WendiError> error = errorHandler.duplicatesResourceExceptionHandler(
                        new WendiConflictResourceException("User", "PF Number", pfNo),
                        request);
                future.complete(error);
                return future;
            }

            //..set active,verification and record dates to current date
            user.setActive(false);
            user.setDeleted(false);
            user.setVerified(false);
            user.setCreatedOn(date);
            user.setModifiedOn(date);

            //logger.info("Securing user record with hashing and encryption...");
            String hashedPassword = Generators.getHashedPassword(user.getPassword(), logger);
            if(hashedPassword != null){
                user.setPassword(hashedPassword);
                logger.info(String.format("Password value >>> %s", hashedPassword));
            } else {
                logger.info("Processing Error! An Error occurred while hashing password");
                ResponseEntity<WendiError> error =  errorHandler.exceptionHandler(
                        new WendiException("System Error! An Error occurred while hashing password"),
                        request);
                future.complete(error);
                return future;
            }

            //encrypt user email, firstname, lastname
            logger.info("Encrypting username, email, firstname and lastname...");
            String email = user.getEmail();
            String encryptedEmail = Secure.encrypt(email, Literals.HASH_KEY, logger, "User Email");
            if(encryptedEmail != null){
                user.setEmail(encryptedEmail);
                logger.info(String.format("Encrypted User Email value >>> %s", encryptedEmail));
            } else {
                ResponseEntity<WendiError> error =  errorHandler.exceptionHandler(
                        new WendiException("System Error! An Error occurred while encrypting User Email"),
                        request);
                future.complete(error);
                return future;
            }

            String firstName = user.getFirstname();
            String encryptedFirstName = Secure.encrypt(firstName, Literals.HASH_KEY, logger, "FirstName");
            if(encryptedFirstName != null){
                user.setFirstname(encryptedFirstName);
                logger.info(String.format("Encrypted Firstname value >>> %s", encryptedFirstName));
            } else {
                ResponseEntity<WendiError> error =  errorHandler.exceptionHandler(
                        new WendiException("System Error! An Error occurred while encrypting FirstName"),
                        request);
                future.complete(error);
                return future;
            }

            String lastName = user.getLastname();
            String encryptedLastName = Secure.encrypt(lastName, Literals.HASH_KEY, logger, "LastName");
            if(encryptedLastName != null){
                user.setLastname(encryptedLastName);
                logger.info(String.format("Encrypted Lastname value >>> %s", encryptedLastName));
            } else {
                ResponseEntity<WendiError> error =  errorHandler.exceptionHandler(
                        new WendiException("System Error! An Error occurred while encrypting LastName"),
                        request);
                future.complete(error);
                return future;
            }

            logger.info("Saving user to the database...");
            String ip = networkService.getIncomingIpAddress(request);

            CompletableFuture<UserDto> record = this.userService.create(user, ip, user.getLoggedUserId());
            logger.info(String.format("User %s successfully saved.", username));

            //save log to database
            logger.info("Saving action log to the database...");

            //get client ip address
            userRec = record.join();
            userRec.setFirstname(firstName);
            userRec.setLastname(lastName);
            userRec.setEmail(encryptedEmail);
        } catch (InterruptedException e) {
            ResponseEntity<WendiError> error = errorHandler.threadCanceledHandler(
                    new WendiThreadCanceledException(),
                    request);

            future.complete(error);
            //log error
            logger.info("Error creating user");
            logger.info("StackTrace Details >>>>>");
            String stackTrace = ExceptionUtils.getStackTrace(e);
            logger.stackTrace(stackTrace);
            return future;
        }

        future.complete(new ResponseEntity<>(userRec, HttpStatus.OK));
        return future;
    }

    @Async
    @DeleteMapping("/deleteUser/{id}/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> deleteUser(@PathVariable Long id, @PathVariable Long loggedUserId, HttpServletRequest request) {
        String date = Generators.currentDate();
        logger.info(String.format("Deleting user record. User with id %s on %s",loggedUserId, date));

        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();
        if (!userService.exists(id)) {
            ResponseEntity<WendiError> error = errorHandler.resourceNotFoundExceptionHandler(
                    new WendiResourceNotFoundException("User", "ID",id),
                    request);
            future.complete(error);
            return future;
        }

        //delete user
        String ip = networkService.getIncomingIpAddress(request);
        userService.softDeleted(id, ip, loggedUserId);

        //return result
        future.complete(new ResponseEntity<>("User deleted successfully", HttpStatus.OK));
        return future;
    }

    @Async
    @PutMapping("/verifyUser/{id}/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> verifyUser(@PathVariable Long id, @PathVariable Long loggedUserId, HttpServletRequest request){
        String date = Generators.currentDate();
        logger.info(String.format("Verify user record. User with id %s on %s",loggedUserId, date));

        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();

        //check if user exists
        if (!userService.exists(id)) {
            ResponseEntity<WendiError> error = errorHandler.resourceNotFoundExceptionHandler(
                    new WendiResourceNotFoundException("User", "ID",id),
                    request);
            future.complete(error);
            return future;
        }

        //make sure user is neither inactive nor deleted
        if (userService.isDeleted(id)) {
            logger.info(String.format("User record with id '%s' is deleted and cannot be verified", id));
            ResponseEntity<WendiError> error =  errorHandler.resourceInactiveExceptionHandler(
                    new WendiResourceNotActiveException("User", "ID",id),
                    request);
            future.complete(error);
            return future;
        }

        //make sure verifying user is not the same who created the record
        String ip = networkService.getIncomingIpAddress(request);
        CompletableFuture<UserDto> loggedInUser = userService.findById(loggedUserId, ip, loggedUserId);
        UserDto user = loggedInUser.join();
        if (userService.isCreator(id, user.getUsername())) {
            logger.info("User record cannot be verified by the person who created it");
            ResponseEntity<WendiError> error =  errorHandler.clientErrorHandler(
                    new WendiClientException("User record cannot be verified by the person who created it"),
                    request);
            future.complete(error);
            return future;
        }

        //verify user
        String modifiedOn = Generators.currentDate();
        userService.verifiedUser(true, user.getUsername(), modifiedOn, id, ip, loggedUserId);

        //return result
        future.complete(new ResponseEntity<>("User verified successfully", HttpStatus.OK));
        return future;
    }

    @Async
    @PutMapping("/activateUser/{id}/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> activateUser(@PathVariable Long id, @PathVariable Long loggedUserId, HttpServletRequest request){
        String date = Generators.currentDate();
        logger.info(String.format("Activate user record. User with id %s on %s",loggedUserId, date));

        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();

        //check if user exists
        if (!userService.exists(id)) {
            ResponseEntity<WendiError> error = errorHandler.resourceNotFoundExceptionHandler(
                    new WendiResourceNotFoundException("User", "ID",id),
                    request);
            future.complete(error);
            return future;
        }

        //make sure verifying user is not the same who created the record
        String ip = networkService.getIncomingIpAddress(request);
        CompletableFuture<UserDto> loggedInUser = userService.findById(loggedUserId, ip, loggedUserId);
        UserDto user = loggedInUser.join();

        //verify user
        String modifiedOn = Generators.currentDate();
        userService.activeUser(true, user.getUsername(), modifiedOn, id, ip, loggedUserId);

        //return result
        future.complete(new ResponseEntity<>("User activate successfully", HttpStatus.OK));
        return future;
    }

    @Async
    @PutMapping("/setLogIn/{isLoggedIn}/{id}")
    public CompletableFuture<ResponseEntity<?>> setLogInStatus(@PathVariable boolean isLoggedIn, @PathVariable Long id,HttpServletRequest request){
        String date = Generators.currentDate();
        logger.info(String.format("Updating user login status. User with id %s on %s",id, date));

        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();

        //get client IP Address
        String ip = networkService.getIncomingIpAddress(request);

        //update user log in status
        userService.updateLoginStatus(isLoggedIn, id, ip, id);

        //return result
        future.complete(new ResponseEntity<>("Login status update successfully", HttpStatus.OK));
        return future;
    }

    @Async
    @PutMapping("/resetPassword/{oldPassword}/{newPassword}/{userId}/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> resetPassword(@PathVariable String oldPassword, @PathVariable String newPassword, @PathVariable long userId, @PathVariable long loggedUserId, HttpServletRequest request){
        String date = Generators.currentDate();
        logger.info(String.format("Activate user record. User with id %s on %s",loggedUserId, date));

        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();

        //check if user exists
        if (!userService.exists(userId)) {
            ResponseEntity<WendiError> error = errorHandler.resourceNotFoundExceptionHandler(
                    new WendiResourceNotFoundException("User", "ID",userId),
                    request);
            future.complete(error);
            return future;
        }

        //make sure user is neither inactive nor deleted
        if (userService.isDeleted(userId)) {
            logger.info(String.format("User record with id '%s' is deleted and cannot be verified", userId));
            ResponseEntity<WendiError> error =  errorHandler.resourceInactiveExceptionHandler(
                    new WendiResourceNotActiveException("User", "ID",userId),
                    request);
            future.complete(error);
            return future;
        }

        //get user record
        String ip = networkService.getIncomingIpAddress(request);
        CompletableFuture<UserDto> loggedInUser = userService.findById(userId, ip, loggedUserId);
        UserDto user = loggedInUser.join();

        //...match old passwords to be sure they match
        logger.info("Verifying old password match...");
        boolean isMatch = Generators.isPasswordMatch(oldPassword, user.getPassword(), logger);
        if(!isMatch){
            logger.info("Processing Error! Unmatched passwords");
            ResponseEntity<WendiError> error =  errorHandler.clientErrorHandler(
                    new WendiClientException("Password not correct! Your old password is not correct"),
                    request);
            future.complete(error);
            return future;
        }

        logger.info("Securing password with hashing");
        String hashedPassword = Generators.getHashedPassword(newPassword , logger);
        if(hashedPassword == null){
            logger.info("Processing Error! An Error occurred while hashing password");
            ResponseEntity<WendiError> error =  errorHandler.exceptionHandler(
                    new WendiException("System Error! An Error occurred while hashing password"),
                    request);
            future.complete(error);
            return future;
        }

        //update user log in status
        userService.updatePassword(hashedPassword, userId, ip, loggedUserId);

        //return result
        future.complete(new ResponseEntity<>("User password updated successfully", HttpStatus.OK));
        return future;
    }

    @Async
    @PutMapping("/updateUser")
    public CompletableFuture<ResponseEntity<?>> updateUser(@RequestBody UserDto user, BindingResult bindingResult, HttpServletRequest request) {
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();
        String date = Generators.currentDate();
        logger.info(String.format("Modifying user %s on %s",user.getUsername(), date));

        //Validate request object
        logger.info("Validating user record >>>");
        if (bindingResult.hasErrors()) {
            logger.info("User record Validation error....");
            WendiRequestValidationException validationException =  new WendiRequestValidationException(Generators.buildErrorMessage(bindingResult));
            logger.info(String.format("%s",validationException.getMessage()));
            future.complete(errorHandler.validationExceptionHandler(validationException,request));
            return future;
        }

        try {
            // Set the user id in the user object
            user.setModifiedOn(date);
            logger.info(String.format("Modifying system user. User modified by %s on %s",user.getModifiedBy(), date));

            //make sure assigned branch exists
            BranchDto branch = branchService.findById(user.getBranchId());
            if(branch == null){
                logger.info(String.format("Resource not found! Branch with BranchId '%s' not found", user.getBranchId()));
                ResponseEntity<WendiError> error = errorHandler.resourceNotFoundExceptionHandler(
                        new WendiResourceNotFoundException("Branch", "BranchId", String.format("%s", user.getBranchId())),
                        request);
                future.complete(error);
                return future;
            }

            //check whether user is assigned to an active branch
            logger.info("Checking whether user assigned branch is active...");
            if(!branch.isActive()){
                logger.info(String.format("Branch with BranchId '%s' is inactive", branch.getId()));
                ResponseEntity<WendiError> error = errorHandler.resourceInactiveExceptionHandler(
                        new WendiResourceNotActiveException("Branch", "BranchId", String.format("%s", branch.getId())),
                        request);
                future.complete(error);
            }

            //make sure assigned role exists
            RoleDto role = roleService.findById(user.getRoleId());
            if(role == null){
                logger.info(String.format("Resource not found! Role with RoleId '%s' not found", user.getRoleId()));
                ResponseEntity<WendiError> error = errorHandler.resourceNotFoundExceptionHandler(
                        new WendiResourceNotFoundException("Role", "RoleId", String.format("%s", user.getRoleId())),
                        request);
                future.complete(error);
                return future;
            }

            //check whether username is not in use
            logger.info("Checking whether user assigned username is assigned to another user...");
            String username = user.getUsername();
            boolean exists = this.userService.usernameDuplicated(username, user.getId());
            if(exists){
                logger.info(String.format("Resource Conflict! Another user with name '%s' exists", username));
                ResponseEntity<WendiError> error = errorHandler.duplicatesResourceExceptionHandler(
                        new WendiConflictResourceException("User", "Username", username),
                        request);
                future.complete(error);
                return future;
            }

            //check if PfNo does not exist
            logger.info("Checking whether user assigned PF Number is not in use...");
            String pfNo = user.getPfNo();
            exists = this.userService.pfNoDuplicated(pfNo, user.getId());
            if(exists){
                logger.info(String.format("Resource Conflict! Another user with PF number '%s' found", pfNo));
                throw new WendiConflictResourceException("User", "PF Number", pfNo);
            }

            //encrypt user email, firstname, lastname
            logger.info("Encrypting username, email, firstname and lastname...");
            String email = user.getEmail();
            String encryptedEmail = Secure.encrypt(email, Literals.HASH_KEY, logger, "User Email");
            if(encryptedEmail != null){
                user.setEmail(encryptedEmail);
                logger.info(String.format("Encrypted User Email value >>> %s", encryptedEmail));
            } else {
                ResponseEntity<WendiError> error =  errorHandler.exceptionHandler(
                        new WendiException("System Error! An Error occurred while encrypting User Email"),
                        request);
                future.complete(error);
                return future;
            }

            String firstName = user.getFirstname();
            String encryptedFirstName = Secure.encrypt(firstName, Literals.HASH_KEY, logger, "FirstName");
            if(encryptedFirstName != null){
                user.setFirstname(encryptedFirstName);
                logger.info(String.format("Encrypted Firstname value >>> %s", encryptedFirstName));
            } else {
                ResponseEntity<WendiError> error =  errorHandler.exceptionHandler(
                        new WendiException("System Error! An Error occurred while encrypting FirstName"),
                        request);
                future.complete(error);
                return future;
            }

            String lastName = user.getLastname();
            String encryptedLastName = Secure.encrypt(lastName, Literals.HASH_KEY, logger, "LastName");
            if(encryptedLastName != null){
                user.setLastname(encryptedLastName);
                logger.info(String.format("Encrypted Lastname value >>> %s", encryptedLastName));
            } else {
                ResponseEntity<WendiError> error =  errorHandler.exceptionHandler(
                        new WendiException("System Error! An Error occurred while encrypting LastName"),
                        request);
                future.complete(error);
                return future;
            }

            //get client ip address
            String ip = networkService.getIncomingIpAddress(request);

            //update user who modified record
            long loggedId = user.getLoggedUserId();
            CompletableFuture<UserDto> loggedInUser = userService.findById(loggedId, ip, loggedId);
            UserDto loggedUser = loggedInUser.join();
            user.setModifiedBy(loggedUser.getUsername());
            user.setModifiedOn(date);

            logger.info("Updating user to the database...");
            this.userService.updateRecord(user, ip, user.getLoggedUserId());

            //save log to database
            logger.info(String.format("User '%s' successfully updated", username));

            user.setFirstname(firstName);
            user.setLastname(lastName);
            user.setEmail(encryptedEmail);

        } catch (InterruptedException e) {
            ResponseEntity<WendiError> error = errorHandler.threadCanceledHandler(
                    new WendiThreadCanceledException(),
                    request);

            future.complete(error);
            //log error
            logger.info("Error updating user");
            logger.info("StackTrace Details >>>>>");
            String stackTrace = ExceptionUtils.getStackTrace(e);
            logger.stackTrace(stackTrace);

            return future;
        }

        future.complete(new ResponseEntity<>(user, HttpStatus.OK));
        return future;
    }

    //endregion

    //region system roles
    @Async
    @GetMapping("/getRoleById/{id}/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> getRoleById(@PathVariable("id") long id, @PathVariable("loggedUserId") long loggedUserId, HttpServletRequest request){
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();

        //try finding resource
        String ip = networkService.getIncomingIpAddress(request);
        CompletableFuture<RoleDto> role = this.roleService.findByIdAsync(id, ip, loggedUserId);

        // Wait for the CompletableFuture to complete and get the result
        RoleDto record = role.join();

        //role not found, throw NOT_FOUND response
        if(record == null){
            ResponseEntity<WendiError> error = errorHandler.resourceNotFoundExceptionHandler(
                    new WendiResourceNotFoundException("Role","RoleId",id),
                    request);
            future.complete(error);
            return future;
        }

        //return user record
        future.complete(new ResponseEntity<>(record, HttpStatus.OK));
        return future;
    }

    @Async
    @GetMapping("/getRoleByName/{name}/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> getRoleByName(@PathVariable("name") String name, @PathVariable("loggedUserId") long loggedUserId, HttpServletRequest request){
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();

        //try finding resource
        String ip = networkService.getIncomingIpAddress(request);
        CompletableFuture<RoleDto> user = this.roleService.findByName(name, ip, loggedUserId);

        // Wait for the CompletableFuture to complete and get the result
        RoleDto record = user.join();

        //role not found, throw NOT_FOUND response
        if(record == null){
            ResponseEntity<WendiError> error = errorHandler.resourceNotFoundExceptionHandler(
                    new WendiResourceNotFoundException("Role","Name",name),
                    request);
            future.complete(error);
            return future;
        }

        //return user record
        future.complete(new ResponseEntity<>(record, HttpStatus.OK));
        return future;
    }

    @Async
    @GetMapping("/getAllRoles/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> getAllRoles(@PathVariable Long loggedUserId, HttpServletRequest request) {
        String date = Generators.currentDate();
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();
        logger.info(String.format("Retrieving a list of roles. User with id %s on %s",loggedUserId, date));

        //get client ip address
        String ip = networkService.getIncomingIpAddress(request);
        CompletableFuture<List<RoleDto>> roles = roleService.getAll(ip, loggedUserId);

        // Wait for the CompletableFuture to complete and get the result
        List<RoleDto> records = roles.join();
        future.complete(ResponseEntity.ok(records));
        return future;
    }

    @Async
    @PostMapping("/createRole")
    public CompletableFuture<ResponseEntity<?>>  createRole(@RequestBody @Valid RoleDto role, BindingResult bindingResult, HttpServletRequest request){
        String date = Generators.currentDate();
        logger.info(String.format("Creating new System role %s on %s",role.getName(), date));
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();

        //Validate request object
        logger.info("Validating role record >>>");
        if (bindingResult.hasErrors()) {
            logger.info("Role record Validation error....");
            WendiRequestValidationException validationException =  new WendiRequestValidationException(Generators.buildErrorMessage(bindingResult));
            logger.info(String.format("%s",validationException.getMessage()));
            future.complete(errorHandler.validationExceptionHandler(validationException,request));
            return future;
        }

        try{
            //check whether role name is not in use
            logger.info("Checking whether role assigned name is not in use...");
            String roleName = role.getName();
            boolean exists = this.roleService.existsByName(roleName);
            if(exists){
                logger.info(String.format("Resource Conflict! Another role with name '%s' exists", roleName));
                ResponseEntity<WendiError> error = errorHandler.duplicatesResourceExceptionHandler(
                        new WendiConflictResourceException("Role", "Name", roleName),
                        request);
                future.complete(error);
                return future;
            }

            //save role to database
            String ip = networkService.getIncomingIpAddress(request);
            CompletableFuture<RoleDto> record = this.roleService.create(role, ip, role.getLoggedUserId());
            RoleDto result = record.join();

            //return role record
            future.complete(new ResponseEntity<>(result, HttpStatus.OK));
            return future;
        } catch (InterruptedException e) {
            ResponseEntity<WendiError> error = errorHandler.threadCanceledHandler(
                    new WendiThreadCanceledException(),
                    request);

            future.complete(error);
            //log error
            logger.info("Error creating system role");
            logger.info("StackTrace Details >>>>>");
            String stackTrace = ExceptionUtils.getStackTrace(e);
            logger.info(stackTrace);
            return future;
        }
    }

    @Async
    @PutMapping("/updateRole")
    public CompletableFuture<ResponseEntity<?>> updateRole(@RequestBody RoleDto role, BindingResult bindingResult, HttpServletRequest request){
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();
        String date = Generators.currentDate();
        logger.info(String.format("Modifying role with name %s on %s",role.getName(), date));

        //Validate request object
        logger.info("Validating role record >>>");
        if (bindingResult.hasErrors()) {
            logger.info("Role record Validation error....");
            WendiRequestValidationException validationException =  new WendiRequestValidationException(Generators.buildErrorMessage(bindingResult));
            logger.info(String.format("%s",validationException.getMessage()));
            future.complete(errorHandler.validationExceptionHandler(validationException,request));
            return future;
        }

        try{
            //check whether role name is not in use
            logger.info("Checking whether role assigned name is not in use...");
            String roleName = role.getName();
            boolean exists = this.roleService.roleNameDuplicated(roleName, role.getId());
            if(exists){
                logger.info(String.format("Resource Conflict! Another role with name '%s' exists", roleName));
                ResponseEntity<WendiError> error = errorHandler.duplicatesResourceExceptionHandler(
                        new WendiConflictResourceException("Role", "Name", roleName),
                        request);
                future.complete(error);
                return future;
            }

            //save role to database
            String ip = networkService.getIncomingIpAddress(request);
            this.roleService.updateRole(role, ip, role.getLoggedUserId());

            //return role record
            future.complete(new ResponseEntity<>(role, HttpStatus.OK));
            return future;
        } catch (InterruptedException e) {
            ResponseEntity<WendiError> error = errorHandler.threadCanceledHandler(
                    new WendiThreadCanceledException(),
                    request);

            future.complete(error);
            //log error
            logger.info("Error updating system role");
            logger.info("StackTrace Details >>>>>");
            String stackTrace = ExceptionUtils.getStackTrace(e);
            logger.info(stackTrace);
            return future;
        }
    }

    @Async
    @DeleteMapping("/softDeleteRole/{id}/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> softDeleteRole(@PathVariable Long id, @PathVariable Long loggedUserId, HttpServletRequest request) {
        String date = Generators.currentDate();
        logger.info(String.format("Deleting role record. Role with id %s on %s",loggedUserId, date));

        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();
        if (!roleService.existsById(id)) {
            ResponseEntity<WendiError> error = errorHandler.resourceNotFoundExceptionHandler(
                    new WendiResourceNotFoundException("Role", "RoleId",id),
                    request);
            future.complete(error);
            return future;
        }

        //delete role
        String ip = networkService.getIncomingIpAddress(request);
        roleService.delete(id, ip, loggedUserId);

        //return result
        future.complete(new ResponseEntity<>("Role deleted successfully", HttpStatus.OK));
        return future;
    }

    @Async
    @DeleteMapping("/deleteRole/{id}/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> deleteRole(@PathVariable Long id, @PathVariable Long loggedUserId, HttpServletRequest request) {
        String date = Generators.currentDate();
        logger.info(String.format("Deleting role record. Role with id %s on %s",loggedUserId, date));

        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();
        if (!roleService.existsById(id)) {
            ResponseEntity<WendiError> error = errorHandler.resourceNotFoundExceptionHandler(
                    new WendiResourceNotFoundException("Role", "RoleId",id),
                    request);
            future.complete(error);
            return future;
        }

        //delete role
        String ip = networkService.getIncomingIpAddress(request);
        roleService.purgeRole(id, ip, loggedUserId);

        //return result
        future.complete(new ResponseEntity<>("Role deleted successfully", HttpStatus.OK));
        return future;
    }

    @Async
    @PutMapping("setRolePermissions/{roleId}/{userId}/setIds")
    public CompletableFuture<ResponseEntity<?>>  setRolePermissions(@PathVariable("roleId") long roleId, @PathVariable("userId") long userId, @RequestBody List<Long> setIds, HttpServletRequest request) {
        //set client IP Address
        String ip = networkService.getIncomingIpAddress(request);
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();

        try {
            logger.info("Adding permissions sets to system role");
            roleService.grantPermissions(roleId, setIds, ip,userId);
        } catch (Exception e) {
            logger.info(String.format("Processing Error! %s", e.getMessage()));
            ResponseEntity<WendiError> error =  errorHandler.exceptionHandler(
                    new WendiException(e.getMessage()),
                    request);
            future.complete(error);
            return future;
        }

        //return result
        future.complete(new ResponseEntity<>("Role assigned permissions successfully", HttpStatus.OK));
        return future;
    }

    @Async
    @PutMapping("removeRolePermissions/{roleId}/{userId}/setIds")
    public CompletableFuture<ResponseEntity<?>>  removeRolePermissions(@PathVariable long roleId,@PathVariable("userId") long userId, @RequestBody List<Long> setIds, HttpServletRequest request) {
        //set client IP Address
        String ip = networkService.getIncomingIpAddress(request);
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();

        try {
            logger.info("Removing permissions from system role");
            roleService.denyPermissions(roleId, setIds, ip,userId);
        } catch (Exception e) {
            logger.info(String.format("Processing Error! %s", e.getMessage()));
            ResponseEntity<WendiError> error =  errorHandler.exceptionHandler(
                    new WendiException(e.getMessage()),
                    request);
            future.complete(error);
            return future;
        }

        //return result
        future.complete(new ResponseEntity<>("Role permissions updated successfully", HttpStatus.OK));
        return future;
    }

    //endregion

    //region permissions sets
    @Async
    @GetMapping("/getPermissionSetById/{setId}/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> getPermissionSetById(@PathVariable("setId") long setId, @PathVariable("loggedUserId") long loggedUserId, HttpServletRequest request){
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();

        //try finding resource
        String ip = networkService.getIncomingIpAddress(request);
        CompletableFuture<PermissionSetDto> setRecord;
        try {
            setRecord = this.setService.findById(setId, ip, loggedUserId);
        } catch (Exception e) {
            logger.info(String.format("Processing Error! %s", e.getMessage()));
            ResponseEntity<WendiError> error =  errorHandler.exceptionHandler(
                    new WendiException(e.getMessage()),
                    request);
            future.complete(error);
            return future;
        }

        // Wait for the CompletableFuture to complete and get the result
        PermissionSetDto record = setRecord.join();

        //set not found, throw NOT_FOUND response
        if(record == null){
            ResponseEntity<WendiError> error = errorHandler.resourceNotFoundExceptionHandler(
                    new WendiResourceNotFoundException("Permission Set","SetId",setId),
                    request);
            future.complete(error);
            return future;
        }

        //return user record
        future.complete(new ResponseEntity<>(record, HttpStatus.OK));
        return future;
    }

    @Async
    @GetMapping("/getPermissionSetByName/{setName}/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> getPermissionSetByName(@PathVariable("setName") String setName, @PathVariable("loggedUserId") long loggedUserId, HttpServletRequest request){
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();

        //try finding resource
        String ip = networkService.getIncomingIpAddress(request);
        CompletableFuture<PermissionSetDto> setRecord;
        try {
            setRecord = this.setService.findByName(setName, ip, loggedUserId);
        } catch (Exception e) {
            logger.info(String.format("Processing Error! %s", e.getMessage()));
            ResponseEntity<WendiError> error =  errorHandler.exceptionHandler(
                    new WendiException(e.getMessage()),
                    request);
            future.complete(error);
            return future;
        }

        // Wait for the CompletableFuture to complete and get the result
        PermissionSetDto record = setRecord.join();

        //Set not found, throw NOT_FOUND response
        if(record == null){
            ResponseEntity<WendiError> error = errorHandler.resourceNotFoundExceptionHandler(
                    new WendiResourceNotFoundException("Permission Set","SetName",setName),
                    request);
            future.complete(error);
            return future;
        }

        //return set record
        future.complete(new ResponseEntity<>(record, HttpStatus.OK));
        return future;
    }

    @Async
    @GetMapping("/getPermissionSets/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> getPermissionSets(@PathVariable Long loggedUserId, HttpServletRequest request) {
        String date = Generators.currentDate();
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();
        logger.info(String.format("Retrieving a list of Permission Sets. User with id %s on %s",loggedUserId, date));

        //get client ip address
        String ip = networkService.getIncomingIpAddress(request);
        CompletableFuture<List<PermissionSetDto>> sets = setService.getAll(ip, loggedUserId);

        // Wait for the CompletableFuture to complete and get the result
        List<PermissionSetDto> records = sets.join();
        future.complete(ResponseEntity.ok(records));
        return future;
    }

    @Async
    @PostMapping("/createPermissionSetWithPermissions/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> createPermissionSetWithPermissions(@RequestBody PermissionSetDto set,
                                                                                   @PathVariable Long loggedUserId,
                                                                                   BindingResult bindingResult,
                                                                                   HttpServletRequest request) {
        String date = Generators.currentDate();
        logger.info(String.format("Creating new System Permission Set %s on %s",set.getName(), date));
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();

        //Validate request object
        logger.info("Validating role record >>>");
        if (bindingResult.hasErrors()) {
            logger.info("Permission Set record Validation error....");
            WendiRequestValidationException validationException =  new WendiRequestValidationException(Generators.buildErrorMessage(bindingResult));
            logger.info(String.format("%s",validationException.getMessage()));
            future.complete(errorHandler.validationExceptionHandler(validationException,request));
            return future;
        }

        try{
            //check whether role name is not in use
            logger.info("Checking whether Permission Set assigned name is not in use...");
            String setName = set.getName();
            boolean exists = this.setService.existsByName(setName);
            if(exists){
                logger.info(String.format("Resource Conflict! Another Permission Set with name '%s' exists", setName));
                ResponseEntity<WendiError> error = errorHandler.duplicatesResourceExceptionHandler(
                        new WendiConflictResourceException("PermissionSet", "Name", setName),
                        request);
                future.complete(error);
                return future;
            }

            //..extract Permission Ids from objects
            List<Long> permissionIds = null;
            if(set.getPermissions() != null && !set.getPermissions().isEmpty()){
                permissionIds = new ArrayList<>();
                for (PermissionDto p : set.getPermissions()){
                    permissionIds.add(p.getId());
                }
            }

            //save permission set to database
            String ip = networkService.getIncomingIpAddress(request);
            CompletableFuture<PermissionSetDto> record = this.setService.create(set, permissionIds, ip, loggedUserId);
            PermissionSetDto result = record.join();

            //return role record
            future.complete(new ResponseEntity<>(result, HttpStatus.OK));
            return future;
        }catch(Exception ex) {
            ResponseEntity<WendiError> error = errorHandler.threadCanceledHandler(
                    new WendiThreadCanceledException(),
                    request);

            future.complete(error);
            //log error
            logger.info("Error creating system permission Set");
            logger.info("StackTrace Details >>>>>");
            String stackTrace = ExceptionUtils.getStackTrace(ex);
            logger.info(stackTrace);
            return future;
        }

    }

    @Async
    @PutMapping("/updatePermissionSet/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> updatePermissionSet(@RequestBody PermissionSetDto set,
                                                                    @PathVariable Long loggedUserId,
                                                                    BindingResult bindingResult,
                                                                    HttpServletRequest request){
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();
        String date = Generators.currentDate();
        logger.info(String.format("Modifying update %s on %s", set.getName(), date));

        //Validate request object
        logger.info("Validating Permission Set record >>>");
        if (bindingResult.hasErrors()) {
            logger.info("Permission Set record Validation error....");
            WendiRequestValidationException validationException =  new WendiRequestValidationException(Generators.buildErrorMessage(bindingResult));
            logger.info(String.format("%s",validationException.getMessage()));
            future.complete(errorHandler.validationExceptionHandler(validationException,request));
            return future;
        }

        try{
            //check whether permission Set name is not in use
            logger.info("Checking whether Permission Set assigned name is not in use...");
            String name = set.getName();
            boolean exists = this.setService.existsByNameAndIdNot(name, set.getId());
            if(exists){
                logger.info(String.format("Resource Conflict! Another permission set with name '%s' exists", name));
                ResponseEntity<WendiError> error = errorHandler.duplicatesResourceExceptionHandler(
                        new WendiConflictResourceException("PermissionSet", "Name", name),
                        request);
                future.complete(error);
                return future;
            }

            //save role to database
            String ip = networkService.getIncomingIpAddress(request);
            this.setService.updateSet(set, ip, loggedUserId);

            //return permission set record
            future.complete(new ResponseEntity<>(set, HttpStatus.OK));
            return future;
        } catch (Exception ex) {
            ResponseEntity<WendiError> error = errorHandler.threadCanceledHandler(
                    new WendiThreadCanceledException(),
                    request);

            future.complete(error);
            //log error
            logger.info("Error updating system permission set");
            logger.info("StackTrace Details >>>>>");
            String stackTrace = ExceptionUtils.getStackTrace(ex);
            logger.info(stackTrace);
            return future;
        }
    }

    @Async
    @PutMapping("removePermissions/{userId}/{setId}/permissionIds")
    public CompletableFuture<ResponseEntity<?>>  removePermissionsFromSet(@PathVariable("userId") long userId, @PathVariable long setId, @RequestBody List<Long> permissionIds, HttpServletRequest request) {
        //set client IP Address
        String ip = networkService.getIncomingIpAddress(request);
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();

        try {
            logger.info("Removing permissions from permission set");
            setService.removePermissionFromSet(permissionIds, setId, ip,userId);
        } catch (Exception e) {
            logger.info(String.format("Processing Error! %s", e.getMessage()));
            ResponseEntity<WendiError> error =  errorHandler.exceptionHandler(
                    new WendiException(e.getMessage()),
                    request);
            future.complete(error);
            return future;
        }

        //return result
        future.complete(new ResponseEntity<>("Permission set updated successfully", HttpStatus.OK));
        return future;
    }

    @Async
    @PutMapping("addPermissionsToSet/{userId}/{setId}/{isLocked}/permissionIds")
    public CompletableFuture<ResponseEntity<?>>  addPermissionsFromSet(@PathVariable("userId") long userId, @PathVariable long setId, @PathVariable boolean isLocked, @RequestBody List<Long> permissionIds, HttpServletRequest request) {
        //set client IP Address
        String ip = networkService.getIncomingIpAddress(request);
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();

        try {
            logger.info("Adding permissions to permission set");
            setService.addPermissionToSet(permissionIds, setId, isLocked, ip,userId);
        } catch (Exception e) {
            logger.info(String.format("Processing Error! %s", e.getMessage()));
            ResponseEntity<WendiError> error =  errorHandler.exceptionHandler(
                    new WendiException(e.getMessage()),
                    request);
            future.complete(error);
            return future;
        }

        //return result
        future.complete(new ResponseEntity<>("Permission set updated successfully", HttpStatus.OK));
        return future;
    }

    @Async
    @DeleteMapping("/softDeletePermissionSet/{id}/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> softDeletePermissionSet(@PathVariable Long id, @PathVariable Long loggedUserId, HttpServletRequest request) {
        String date = Generators.currentDate();
        logger.info(String.format("Deleting permission set record. Permission with id %s on %s",loggedUserId, date));

        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();
        if (!setService.existsById(id)) {
            ResponseEntity<WendiError> error = errorHandler.resourceNotFoundExceptionHandler(
                    new WendiResourceNotFoundException("PermissionSet", "SetId",id),
                    request);
            future.complete(error);
            return future;
        }

        //delete permission set
        String ip = networkService.getIncomingIpAddress(request);
        setService.delete(id, ip, loggedUserId);

        //return result
        future.complete(new ResponseEntity<>("Permission set deleted successfully", HttpStatus.OK));
        return future;
    }

    @Async
    @DeleteMapping("/deletePermissionSet/{id}/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> deletePermissionSet(@PathVariable Long id, @PathVariable Long loggedUserId, HttpServletRequest request) {
        String date = Generators.currentDate();
        logger.info(String.format("Deleting permission set record. PermissionSet with id %s on %s",loggedUserId, date));

        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();
        if (!setService.existsById(id)) {
            ResponseEntity<WendiError> error = errorHandler.resourceNotFoundExceptionHandler(
                    new WendiResourceNotFoundException("PermissionSet", "SetId",id),
                    request);
            future.complete(error);
            return future;
        }

        //permanently delete set
        String ip = networkService.getIncomingIpAddress(request);
        setService.purgeSet(id, ip, loggedUserId);

        //return result
        future.complete(new ResponseEntity<>("Permission set deleted successfully", HttpStatus.OK));
        return future;
    }

    //endregion

    //region permissions
    @Async
    @GetMapping("/getPermissionById/{permissionId}/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> getPermissionById(@PathVariable("permissionId") long permissionId, @PathVariable("loggedUserId") long loggedUserId, HttpServletRequest request){
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();

        //try finding resource
        String ip = networkService.getIncomingIpAddress(request);
        CompletableFuture<PermissionDto> permissionRecord;
        try {
            permissionRecord = this.permissionService.findById(permissionId, ip, loggedUserId);
        } catch (Exception e) {
            logger.info(String.format("Processing Error! %s", e.getMessage()));
            ResponseEntity<WendiError> error =  errorHandler.exceptionHandler(
                    new WendiException(e.getMessage()),
                    request);
            future.complete(error);
            return future;
        }

        // Wait for the CompletableFuture to complete and get the result
        PermissionDto record = permissionRecord.join();

        //permission not found, throw NOT_FOUND response
        if(record == null){
            ResponseEntity<WendiError> error = errorHandler.resourceNotFoundExceptionHandler(
                    new WendiResourceNotFoundException("Permission","PermissionId",permissionId),
                    request);
            future.complete(error);
            return future;
        }

        //return permission record
        future.complete(new ResponseEntity<>(record, HttpStatus.OK));
        return future;
    }

    @Async
    @GetMapping("/getPermissionByName/{name}/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> getPermissionByName(@PathVariable("name") String name, @PathVariable("loggedUserId") long loggedUserId, HttpServletRequest request){
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();

        //try finding resource
        String ip = networkService.getIncomingIpAddress(request);
        CompletableFuture<PermissionDto> permissionRecord;
        try {
            permissionRecord = this.permissionService.findByName(name, ip, loggedUserId);
        } catch (Exception e) {
            logger.info(String.format("Processing Error! %s", e.getMessage()));
            ResponseEntity<WendiError> error =  errorHandler.exceptionHandler(
                    new WendiException(e.getMessage()),
                    request);
            future.complete(error);
            return future;
        }

        // Wait for the CompletableFuture to complete and get the result
        PermissionDto record = permissionRecord.join();

        //permission not found, throw NOT_FOUND response
        if(record == null){
            ResponseEntity<WendiError> error = errorHandler.resourceNotFoundExceptionHandler(
                    new WendiResourceNotFoundException("Permission","Name",name),
                    request);
            future.complete(error);
            return future;
        }

        //return permission record
        future.complete(new ResponseEntity<>(record, HttpStatus.OK));
        return future;
    }

    @Async
    @GetMapping("/getAllPermissions/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> getAllPermissions(Pageable page, @PathVariable Long loggedUserId, HttpServletRequest request) {
        String date = Generators.currentDate();
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();
        logger.info(String.format("Retrieving a list of permissions. User with id %s on %s",loggedUserId, date));

        //get client ip address
        String ip = networkService.getIncomingIpAddress(request);
        CompletableFuture<List<PermissionDto>> roles = permissionService.getAll(ip, loggedUserId);

        // Wait for the CompletableFuture to complete and get the result
        List<PermissionDto> records = roles.join();
        future.complete(ResponseEntity.ok(records));
        return future;
    }

    @Async
    @PutMapping("/updatePermission/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> updatePermission(@RequestBody PermissionDto permission, @PathVariable Long loggedUserId, BindingResult bindingResult, HttpServletRequest request){
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();
        String date = Generators.currentDate();
        logger.info(String.format("Modifying permission with name'%s' on %s",permission.getName(), date));

        //Validate request object
        logger.info("Validating role record >>>");
        if (bindingResult.hasErrors()) {
            logger.info("Permission record Validation error....");
            WendiRequestValidationException validationException =  new WendiRequestValidationException(Generators.buildErrorMessage(bindingResult));
            logger.info(String.format("%s",validationException.getMessage()));
            future.complete(errorHandler.validationExceptionHandler(validationException,request));
            return future;
        }

        try{

            //update permission to database
            String ip = networkService.getIncomingIpAddress(request);
            this.permissionService.updatePermission(permission, ip, loggedUserId);

            //return role record
            future.complete(new ResponseEntity<>(permission, HttpStatus.OK));
            return future;
        } catch (Exception e) {
            ResponseEntity<WendiError> error = errorHandler.threadCanceledHandler(
                    new WendiThreadCanceledException(),
                    request);

            future.complete(error);
            //log error
            logger.info("Error updating system role");
            logger.info("StackTrace Details >>>>>");
            String stackTrace = ExceptionUtils.getStackTrace(e);
            logger.info(stackTrace);
            return future;
        }
    }

    //endregion

    //region System Logs

    @Async
    @GetMapping("/getLogs/{loggedUserId}")
    public CompletableFuture<ResponseEntity<?>> getLogs(@PathVariable Long loggedUserId, HttpServletRequest request) {
        String date = Generators.currentDate();
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();
        logger.info(String.format("Retrieving a list of system logs. Accessed by user with id %s on %s",loggedUserId, date));

        //get client ip address
        String ip = networkService.getIncomingIpAddress(request);
        CompletableFuture<List<LogDto>> logs = logService.getLogs(ip, loggedUserId);

        // Wait for the CompletableFuture to complete and get the result
        List<LogDto> records = logs.join();
        future.complete(ResponseEntity.ok(records));
        return future;
    }

    @Async
    @GetMapping("/getUserLogs/{loggedUserId}/{startDate}/{endDate}")
    public CompletableFuture<ResponseEntity<?>> getUserLogs(@PathVariable Long loggedUserId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate,
            HttpServletRequest request) {

        String date = Generators.currentDate();
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();
        logger.info(String.format("Retrieving a list of system logs. Accessed by user with id %s on %s",loggedUserId, date));

        //get client ip address
        String ip = networkService.getIncomingIpAddress(request);
        CompletableFuture<List<LogDto>> logs = logService.getLogs(ip, loggedUserId, startDate, endDate);

        // Wait for the CompletableFuture to complete and get the result
        List<LogDto> records = logs.join();
        future.complete(ResponseEntity.ok(records));
        return future;
    }
    //endregion

}
