package org.app.util.exception;

import org.springframework.http.*;

public class InternalServerErrorException extends CustomException {

    public InternalServerErrorException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
    }
}
