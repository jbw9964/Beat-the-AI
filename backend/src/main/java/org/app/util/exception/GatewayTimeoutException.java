package org.app.util.exception;

import org.springframework.http.*;

public class GatewayTimeoutException extends CustomException {

    public GatewayTimeoutException(String message) {
        super(HttpStatus.GATEWAY_TIMEOUT.value(), message);
    }

    public static GatewayTimeoutException of(String message) {
        return new GatewayTimeoutException(message);
    }
}
