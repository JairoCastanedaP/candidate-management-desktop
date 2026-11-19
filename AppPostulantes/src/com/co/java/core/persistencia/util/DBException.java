package com.co.java.core.persistencia.util;

public class DBException extends RuntimeException {

    public DBException(String message) {
        super(message);
    }

    public DBException(String message, Throwable cause) {
        super(message, cause);
    }
}
