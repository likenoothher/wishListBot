package com.aziarets.vividapp.exception;

public class NotFoundUserNameException extends RuntimeException {
    public NotFoundUserNameException(String message) {
        super(message);
    }
}
