package com.pbu.sam.services;

import com.pbu.sam.dtos.LogDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SystemLogService {
    void create(LogDto log);
    CompletableFuture<List<LogDto>> getLogs(String ip, long userId);
    CompletableFuture<List<LogDto>> getLogs(String ip, long userId, LocalDateTime startDate, LocalDateTime endDate);
}
