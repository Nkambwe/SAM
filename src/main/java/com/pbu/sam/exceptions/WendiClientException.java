package com.pbu.sam.exceptions;

public class WendiClientException extends RuntimeException {
    public static final long serialVersionId = 6L;
    public WendiClientException(String message){
        super(message);
    }
}
