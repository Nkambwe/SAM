package com.pbu.sam.dtos;

import java.time.LocalDateTime;

public class LogDto {
    private long id;
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    private long userId;
    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }

    private String action;
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }

    private LocalDateTime logTime;
    public LocalDateTime getLogTime() {
        return logTime;
    }
    public void setLogTime(LocalDateTime logTime) {
        this.logTime = logTime;
    }

    private String ipAddress;
    public String getIpAddress() {
        return ipAddress;
    }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    private String username;
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    /*Default Constructor*/
    public LogDto() {}

    public LogDto(long userId, String action, LocalDateTime logTime, String ipAddress) {
        this.userId = userId;
        this.action = action;
        this.logTime = logTime;
        this.ipAddress = ipAddress;
    }
}
