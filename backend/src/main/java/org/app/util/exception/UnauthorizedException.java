package org.app.util.exception;

public class UnauthorizedException extends CustomException {

    public UnauthorizedException(String message) {
        super(401, message);
    }

    public static UnauthorizedException of(String message) {
        return new UnauthorizedException(message);
    }
}
