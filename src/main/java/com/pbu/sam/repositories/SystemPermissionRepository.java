package com.pbu.sam.repositories;

import com.pbu.sam.dtos.PermissionDto;
import com.pbu.sam.entities.SystemPermission;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SystemPermissionRepository extends JpaRepository<SystemPermission, Long> {

    Optional<SystemPermission> findById(long id);

    Optional<SystemPermission> findByName(String name);

    /**Method updates permission description**/
    @Transactional
    @Modifying
    @Query("UPDATE SystemPermission p SET p.description = :#{#permission.description} WHERE p.id = :#{#permission.id}")
    void updatePermission(@Param("permission") SystemPermission permission);

    /**
     * Get role permissions
     * @param permissionIds list of permission IDs to find
     * @return a list of system permissions for a role if any
     **/
    Set<SystemPermission> findAllByIdIn(List<Long> permissionIds);

}
