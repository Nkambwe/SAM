package com.pbu.sam.repositories;

import com.pbu.sam.dtos.PermissionSetDto;
import com.pbu.sam.entities.SystemPermissionSet;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SystemPermissionSetRepository extends JpaRepository<SystemPermissionSet, Long> {
    @EntityGraph(attributePaths = "permissions")
    Optional<SystemPermissionSet> findById(Long id);

    @EntityGraph(attributePaths = "permissions")
    Optional<SystemPermissionSet> findBySetName(String name);

    boolean existsById(long id);

    boolean existsBySetName(String name);

    boolean existsBySetNameAndIdNot(String name, long id);

    Set<SystemPermissionSet> findAllByIdIn(List<Long> setIds);

    @Query("SELECT DISTINCT ps FROM SystemPermissionSet ps LEFT JOIN FETCH ps.permissions")
    List<SystemPermissionSet> findAllWithSystemPermissions();

    @Modifying
    @Query("UPDATE SystemPermissionSet s SET s.isLocked =true WHERE s.id = :setId")
    void lockPermissionSet(@Param("setId")long setId);

    @Transactional
    @Modifying
    @Query("UPDATE SystemPermissionSet s SET s.setName = :#{#permissionSet.name}, s.description = :#{#permissionSet.description} WHERE s.id = :#{#permissionSet.id}")
    void updatePermissionSet(@Param("permissionSet") PermissionSetDto permissionSet);

    @Transactional
    @Modifying
    @Query("SELECT CASE WHEN s.isDeleted = true THEN true ELSE false END FROM SystemPermissionSet s WHERE s.id = :setId")
    boolean isDeleted(@Param("setId") long setId);

    @Transactional
    @Modifying
    @Query("SELECT CASE WHEN s.isLocked = true THEN true ELSE false END FROM SystemPermissionSet s WHERE s.id = :setId")
    void isLocked(@Param("setId") long setId);

}
