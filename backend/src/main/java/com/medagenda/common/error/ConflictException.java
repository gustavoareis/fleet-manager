package com.medagenda.common.error;

/** The request clashes with existing data (duplicate key, resource still in use...). Maps to 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
