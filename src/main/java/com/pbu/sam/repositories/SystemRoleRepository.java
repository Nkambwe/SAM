package com.pbu.sam.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pbu.sam.entities.SystemRole;

import java.util.Optional;

public interface SystemRoleRepository extends JpaRepository<SystemRole, Long> {
    /**Find system role with name {@code name}
     * @param name System role name to look for
     * @return true if role exists false otherwise
     **/
    Optional<SystemRole> findByName(String name);

    /***Check whether system role exists by ID
     * @param id System role ID to look for
     * @return true if role exists false otherwise
     **/
    boolean existsById(long id);
    /***Check whether system role exists by ID
     * @param name System role name to look for
     * @return true if role exists false otherwise
     **/
    boolean existsByName(String name);

    /***Check whether system role exists by name but not with the same Id
     * @param name System role name to look for
     * @return true if role exists false otherwise
     **/
    boolean existsByNameAndIdNot(String name, long id);

    /***Update system role object**/
    @Transactional
    @Modifying
    @Query("UPDATE SystemRole r SET r.name = :#{#role.name}, r.description = :#{#role.description} WHERE r.id = :#{#role.id}")
    void updateRoleRecord(@Param("role") SystemRole role);

    @Transactional
    @Modifying
    @Query("SELECT CASE WHEN r.isDeleted = true THEN true ELSE false END FROM SystemRole r WHERE r.id = :roleId")
    boolean isDeleted(@Param("roleId") long roleId);
}

