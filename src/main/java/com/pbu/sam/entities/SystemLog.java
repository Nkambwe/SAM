package com.pbu.sam.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name="System_logs")
public class SystemLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "row_id", nullable = false)
    private long id;

    @Column(name = "action_performed", length = 100, nullable = false)
    private String action;

    @Column(name = "log_time", nullable = false)
    private LocalDateTime logTime;

    @Column(name = "ip_address", length = 40, nullable = false)
    private String ipAddress;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "row_id")
    private SystemUser user;

    @Override
    public String toString() {
        return logTime != null && action != null &&!action.isEmpty() ?
                String.format("%s::%s", logTime, action)
                :super.toString();
    }

    @Override
    public boolean equals(Object otherLog) {
        if (this == otherLog) return true;
        if (otherLog == null || Hibernate.getClass(this) != Hibernate.getClass(otherLog)) return false;
        SystemLog thisLog = (SystemLog) otherLog;
        return getId() != 0 && Objects.equals(getId(), thisLog.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode()^3;
    }
}

