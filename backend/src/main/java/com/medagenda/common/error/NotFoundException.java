package com.medagenda.common.error;

/** The requested resource does not exist. Maps to 404. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException of(String resource, Object id) {
        return new NotFoundException("%s não encontrado(a) para o id %s.".formatted(resource, id));
    }
}
