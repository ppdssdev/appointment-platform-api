package com.appointments_ws.appointments_ws.domain.exception;

public class ResourceNotFoundException extends  RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

}
