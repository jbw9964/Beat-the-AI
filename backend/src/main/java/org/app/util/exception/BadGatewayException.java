package org.app.util.exception;

import org.springframework.http.*;

public class BadGatewayException extends ExpectableServerErrorException {

    public BadGatewayException(
            String message, String clientResponseMessage,
            Throwable cause
    ) {
        super(
                HttpStatus.BAD_GATEWAY.value(), message,
                clientResponseMessage, cause
        );
    }
}
