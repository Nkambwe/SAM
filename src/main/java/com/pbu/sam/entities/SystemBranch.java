package com.pbu.sam.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name="Branches")
public class SystemBranch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "row_id")
    @JdbcTypeCode(SqlTypes.BIGINT)
    private long id;

    @Column(name = "sol_id", length = 10, nullable = false)
    private String solId;

    @Column(name = "branch_name", length = 120, nullable = false)
    private String branchName;

    @Column
    private boolean isActive;

    @Column
    private boolean isDeleted;

    @Column
    private String createdOn;

    //Relationship link between SystemUser and branch
    @OneToMany(mappedBy = "branch")
    private List<SystemUser> users;

    @Override
    public String toString() {
        return solId != null &&!solId.isEmpty() && branchName != null &&!branchName.isEmpty() ?
                String.format("%s::%s", solId, branchName)
                :super.toString();
    }

    @Override
    public boolean equals(Object otherBranch) {
        if (this == otherBranch) return true;
        if (otherBranch == null || Hibernate.getClass(this) != Hibernate.getClass(otherBranch)) return false;
        SystemBranch thisBranch = (SystemBranch) otherBranch;
        return getId() != 0 && Objects.equals(getId(), thisBranch.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode()^3;
    }
}

