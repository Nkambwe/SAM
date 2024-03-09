package com.pbu.sam.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class ArchiveLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "action_performed", length = 100, nullable = false)
    private String action;

    @Column(name = "log_time", nullable = false)
    private LocalDateTime logTime;

    @Column(name = "ip_address", length = 40, nullable = false)
    private String ipAddress;

    @Column(name = "archive_Time", nullable = false)
    private LocalDateTime archiveTime;
}
