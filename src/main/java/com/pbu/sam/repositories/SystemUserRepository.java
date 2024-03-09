package com.pbu.sam.repositories;

import com.pbu.sam.entities.SystemUser;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SystemUserRepository extends JpaRepository<SystemUser, Long> {

    boolean existsByPfNoIgnoreCase(String pfNo);
    boolean existsByPfNoAndIdNot(String pfNo, long userId);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByUsernameAndIdNot(String username, long userId);

    @Query("SELECT CASE WHEN u.isDeleted = true THEN true ELSE false END FROM SystemUser u WHERE u.id = :userId")
    boolean isDeleted(@Param("userId") long userId);

    @Query("SELECT CASE WHEN u.createdBy = :creator THEN true ELSE false END FROM SystemUser u WHERE u.id = :userId")
    boolean isCreator(@Param("userId") long userId, @Param("creator") String creator);

    SystemUser findByUsernameIgnoreCase(String username);
    SystemUser findByPfNoIgnoreCase(String pfNo);

    @Modifying
    @Query("UPDATE SystemUser u SET u.password = :password WHERE u.id = :id")
    void updatePasswordById(@Param("password") String password, @Param("id") long id);

    @Transactional
    @Modifying
    @Query("UPDATE SystemUser u SET u.isLoggedIn = :loggedIn WHERE u.id = :id")
    void updateIsLoggedInById(@Param("loggedIn") boolean loggedIn, @Param("id") long id);

    @Transactional
    @Modifying
    @Query("UPDATE SystemUser u SET u.isActive = :active, u.isDeleted = false, u.modifiedBy = :modifiedBy, u.modifiedOn = :modifiedOn WHERE u.id = :id")
    void updateIsActiveById(@Param("active") boolean active, @Param("modifiedBy") String modifiedBy, @Param("modifiedOn")String modifiedOn, @Param("id") long id);

    @Transactional
    @Modifying
    @Query("UPDATE SystemUser u SET u.isActive =true, u.isVerified = :verify, u.modifiedBy = :modifiedBy, u.modifiedOn = :modifiedOn WHERE u.id = :id")
    void updateIsVerifiedById(@Param("verify") boolean verify, @Param("modifiedBy") String modifiedBy, @Param("modifiedOn")String modifiedOn, @Param("id") long id);

    @Transactional
    @Modifying
    @Query("UPDATE SystemUser u SET u.isDeleted = true, u.isActive = false, u.isVerified = false WHERE u.id = :id")
    void updateIsDeletedById(@Param("id") long id);

    @Transactional
    @Modifying
    @Query("UPDATE SystemUser u SET u.firstname = :#{#user.firstname}, u.lastname = :#{#user.lastname}, u.gender = :#{#user.gender}, " +
            "u.pfNo = :#{#user.pfNo}, u.email = :#{#user.email}, u.modifiedOn = :#{#user.modifiedOn}, u.modifiedBy = :#{#user.modifiedBy} WHERE u.id = :#{#user.id}")
    void updateUserRecord(@Param("user") SystemUser user);
}
