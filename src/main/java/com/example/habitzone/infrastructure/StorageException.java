package com.example.habitzone.infrastructure;

/** Wraps failures that occur while loading or saving persistent application data. */
public class StorageException extends RuntimeException {
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
