package org.app.util.exception;

public class BadGatewayException extends CustomException {

    public BadGatewayException(String message) {
        super(502, message);
    }

    public static BadGatewayException of(String message) {
        return new BadGatewayException(message);
    }
}
