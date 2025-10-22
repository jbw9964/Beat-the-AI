package org.app.util.exception;

public class GatewayTimeoutException extends CustomException {

    public GatewayTimeoutException(String message) {
        super(504, message);
    }

    public static GatewayTimeoutException of(String message) {
        return new GatewayTimeoutException(message);
    }
}
