package com.aziarets.vividapp.exception;

public class GiftsLimitReachedException extends RuntimeException {
    public GiftsLimitReachedException(String message) {
        super(message);
    }
}
