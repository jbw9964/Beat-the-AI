package org.app.util.exception;

import org.springframework.http.*;

public class ForbiddenException extends CustomException {

    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN.value(), message);
    }

    public static ForbiddenException of(String message) {
        return new ForbiddenException(message);
    }
}
