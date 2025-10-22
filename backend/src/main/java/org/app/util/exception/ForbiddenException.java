package org.app.util.exception;

public class ForbiddenException extends CustomException {

    public ForbiddenException(String message) {
        super(403, message);
    }

    public static ForbiddenException of(String message) {
        return new ForbiddenException(message);
    }
}
