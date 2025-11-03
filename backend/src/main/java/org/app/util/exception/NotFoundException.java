package org.app.util.exception;

import org.springframework.http.*;

public class NotFoundException extends CustomException {

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND.value(), message);
    }

    public static NotFoundException of(String message) {
        return new NotFoundException(message);
    }
}
