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
@Table(name="System_Permissions")
public class SystemPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "row_id", nullable = false)
    @JdbcTypeCode(SqlTypes.BIGINT)
    private long id;

    @Column(name = "permission_name", length = 80, nullable = false)
    private String name;

    @Column(name = "permission_descr")
    private String description;

    @Column(name = "is_locked")
    private boolean isLocked;

    //Relationship link between SystemPermissions and SystemPermissionSet
    @ManyToMany(mappedBy = "permissions")
    private Set<SystemPermissionSet> permissionSets;

    @Override
    public String toString() {
        return name != null &&!name.isEmpty() ?  String.format("%s", name) : super.toString();
    }

    @Override
    public boolean equals(Object OtherPermission) {
        if (this == OtherPermission) return true;
        if (OtherPermission == null || Hibernate.getClass(this) != Hibernate.getClass(OtherPermission)) return false;
        SystemPermission thisPermission = (SystemPermission) OtherPermission;
        return getId() != 0 && Objects.equals(getId(), thisPermission.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode()^3;
    }
}

