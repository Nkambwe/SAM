package com.pbu.sam.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name="System_Users")
public class SystemUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "row_id", nullable = false)
    @JdbcTypeCode(SqlTypes.BIGINT)
    private long id;
    @Column(name = "username", nullable = false, length = 80)
    private String username;
    @Column(name = "first_name", nullable = false, length = 80)
    private String firstname;
    @Column(name = "last_name", nullable = false, length = 80)
    private String lastname;
    @Column(name = "gender", nullable = false, length = 10)
    private String gender;
    @Column(name = "pf_no", nullable = false, length = 10)
    private String pfNo;
    @Column(name="email")
    private String email;
    @Column(name="password", nullable = false)
    private String password;
    @Column(name="is_deleted")
    private boolean isDeleted;
    @Column
    private boolean isLoggedIn;
    @Column
    private boolean isActive;
    @Column
    private boolean isVerified;
    @Column(name = "verified_by", length = 80)
    private String verifiedBy;
    @Column(name = "created_by", length = 80)
    private String createdBy;
    @Column(name = "created_on", length = 40)
    private String createdOn;
    @Column(name = "modified_on", length = 40)
    private String modifiedOn;
    @Column(name = "modified_by", length = 80)
    private String modifiedBy;

    @ManyToOne
    @JoinColumn(name = "role_id", referencedColumnName = "row_id")
    private SystemRole role;

    //Relationship between SystemUser and branches
    @ManyToOne
    @JoinColumn(name = "branch_id", referencedColumnName = "row_id")
    private SystemBranch branch;

    //Relationship link between SystemUser and SystemLog
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<SystemLog> logs = new ArrayList<>();

    @Override
    public String toString() {
        return username != null &&!username.isEmpty() && firstname != null &&!firstname.isEmpty() ?
                String.format("%s::%s", firstname, username)
                :super.toString();
    }

    @Override
    public boolean equals(Object otherUser) {
        if (this == otherUser) return true;
        if (otherUser == null || Hibernate.getClass(this) != Hibernate.getClass(otherUser)) return false;
        SystemUser thisUser = (SystemUser) otherUser;
        return getId() != 0 && Objects.equals(getId(), thisUser.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode()^3;
    }
}
