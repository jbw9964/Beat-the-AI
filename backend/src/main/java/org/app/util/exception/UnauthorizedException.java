package org.app.util.exception;

import org.springframework.http.*;

public class UnauthorizedException extends CustomException {

    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED.value(), message);
    }

    public static UnauthorizedException of(String message) {
        return new UnauthorizedException(message);
    }
}
