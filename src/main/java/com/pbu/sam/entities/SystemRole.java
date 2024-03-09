package com.pbu.sam.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name="System_Roles")
public class SystemRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "row_id", nullable = false)
    @JdbcTypeCode(SqlTypes.BIGINT)
    private long id;

    @Column(name = "role_name", length = 120, nullable = false)
    private String name;

    @Column(name = "role_descr")
    private String description;

    @Column
    private boolean isDeleted;

    //Relationship link between SystemUser and SystemRole
    @OneToMany(mappedBy = "role")
    private List<SystemUser> users;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permission",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<SystemPermissionSet> permissions= new HashSet<>();

    @Override
    public String toString() {
        return name != null &&!name.isEmpty() ?  String.format("%s", name) : super.toString();
    }

    @Override
    public boolean equals(Object otherRole) {
        if (this == otherRole) return true;
        if (otherRole == null || Hibernate.getClass(this) != Hibernate.getClass(otherRole)) return false;
        SystemRole thisUser = (SystemRole) otherRole;
        return getId() != 0 && Objects.equals(getId(), thisUser.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode()^3;
    }
}

