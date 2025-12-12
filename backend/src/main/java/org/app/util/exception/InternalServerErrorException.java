package org.app.util.exception;

import org.springframework.http.*;

public class InternalServerErrorException extends ExpectableServerErrorException {

    public InternalServerErrorException(
            String message, String clientResponseMessage,
            Throwable cause
    ) {
        super(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), message,
                clientResponseMessage, cause
        );
    }

    public InternalServerErrorException(
            String message, String clientResponseMessage
    ) {
        this(message, clientResponseMessage, null);
    }
}
