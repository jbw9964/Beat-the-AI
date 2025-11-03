package org.app.util.exception;

import org.springframework.http.*;

public class BadGatewayException extends CustomException {

    public BadGatewayException(String message) {
        super(HttpStatus.BAD_GATEWAY.value(), message);
    }

    public static BadGatewayException of(String message) {
        return new BadGatewayException(message);
    }
}
