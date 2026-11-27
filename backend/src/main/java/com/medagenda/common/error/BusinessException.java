package com.medagenda.common.error;

/** The request is well-formed but breaks a business rule. Maps to 422. */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
