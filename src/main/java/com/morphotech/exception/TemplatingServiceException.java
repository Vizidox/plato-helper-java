package com.morphotech.exception;

public class TemplatingServiceException extends RuntimeException {

    public TemplatingServiceException(String message) {
        super(message);
    }

    public TemplatingServiceException(String message, Exception e) {
        super(message, e);
    }

}
