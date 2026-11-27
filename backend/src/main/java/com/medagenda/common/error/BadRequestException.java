package com.medagenda.common.error;

/** Invalid request parameters that Bean Validation cannot express. Maps to 400. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
