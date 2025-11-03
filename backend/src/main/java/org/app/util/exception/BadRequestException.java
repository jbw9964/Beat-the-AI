package org.app.util.exception;

import org.springframework.http.*;

public class BadRequestException extends CustomException {

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST.value(), message);
    }

    public static BadRequestException of(String message) {
        return new BadRequestException(message);
    }
}
