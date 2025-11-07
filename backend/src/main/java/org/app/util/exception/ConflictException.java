package org.app.util.exception;

import org.springframework.http.*;

public class ConflictException extends CustomException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT.value(), message);
    }

}
