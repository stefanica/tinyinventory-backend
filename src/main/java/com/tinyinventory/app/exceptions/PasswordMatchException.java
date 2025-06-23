package com.tinyinventory.app.exceptions;

public class PasswordMatchException extends RuntimeException {
    public PasswordMatchException(String message) {
        super(message);
    }
}
