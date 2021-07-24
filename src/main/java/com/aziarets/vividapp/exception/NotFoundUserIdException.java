package com.aziarets.vividapp.exception;

public class NotFoundUserIdException extends RuntimeException{
    public NotFoundUserIdException(String message) {
        super(message);
    }
}
