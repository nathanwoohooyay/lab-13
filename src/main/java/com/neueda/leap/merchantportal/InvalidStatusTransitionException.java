package com.neueda.leap.merchantportal;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(String message) {
        super(message);
    }

    public InvalidStatusTransitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
