package org.app.util.exception;

import org.springframework.http.*;

public class GatewayTimeoutException extends ExpectableServerErrorException {

    public GatewayTimeoutException(
            String message, String clientResponseMessage,
            Throwable cause
    ) {
        super(
                HttpStatus.GATEWAY_TIMEOUT.value(), message,
                clientResponseMessage, cause
        );
    }
}
