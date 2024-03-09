package com.pbu.sam.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name="Permission_set")
public class SystemPermissionSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @JdbcTypeCode(SqlTypes.BIGINT)
    private long id;

    @Column(name = "set_name", length = 80, nullable = false)
    private String setName;

    @Column(name = "set_descr")
    private String description;

    @Column(name = "is_locked")
    private boolean isLocked;

    @Column
    private boolean isDeleted;

    //Relationship link between SystemPermissions and SystemRoles
    @ManyToMany
    @JoinTable(
            name = "set_permission",
            joinColumns = @JoinColumn(name = "set_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<SystemPermission> permissions;

    //Relationship link between SystemPermissions and SystemRoles
    @ManyToMany(mappedBy = "permissions")
    private Set<SystemRole> roles;

    @Override
    public String toString() {
        return setName != null &&!setName.isEmpty() ?  String.format("%s", setName) : super.toString();
    }

    @Override
    public boolean equals(Object otherSet) {
        if (this == otherSet) return true;
        if (otherSet == null || Hibernate.getClass(this) != Hibernate.getClass(otherSet)) return false;
        SystemPermissionSet thisSet = (SystemPermissionSet) otherSet;
        return getId() != 0 && Objects.equals(getId(), thisSet.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode()^3;
    }

}
