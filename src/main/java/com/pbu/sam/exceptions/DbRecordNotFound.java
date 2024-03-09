package com.pbu.sam.exceptions;

public class DbRecordNotFound extends RuntimeException {
    public DbRecordNotFound(String recordType, String searchField, String searchValue) {
        super(String.format("No %s with '%s' '%s' found", recordType, searchField, searchValue));
    }
}
