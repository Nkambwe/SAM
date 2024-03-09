package com.pbu.sam.repositories;

import com.pbu.sam.entities.SystemBranch;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BranchRepository  extends JpaRepository<SystemBranch, Long> {

    SystemBranch findByBranchName(String name);

    SystemBranch findBySolId(String solId);

    boolean existsBySolId(String solId);

    boolean existsByBranchName(String branchName);

    boolean existsBySolIdAndIdNot(String solId, long id);

    boolean existsByBranchNameAndIdNot(String branchName, long id);

    @Transactional
    @Modifying
    @Query("UPDATE SystemBranch b SET b.isActive = :status WHERE b.id = :id")
    void updateIsActiveById(@Param("id") long id, @Param("status") boolean status);

    @Transactional
    @Modifying
    @Query("UPDATE SystemBranch b SET b.solId = :#{#branch.solId}, b.branchName = :#{#branch.branchName}, b.isActive = :#{#branch.isActive}, b.createdOn = :#{#branch.createdOn} WHERE b.id = :#{#branch.id}")
    void updateBranch(@Param("branch") SystemBranch branch);

    @Transactional
    @Modifying
    @Query("SELECT CASE WHEN b.isDeleted = true THEN true ELSE false END FROM SystemBranch b WHERE b.id = :branchId")
    boolean isDeleted(@Param("branchId") long branchId);

}
