package org.app.util.exception;

public class NotFoundException extends CustomException {

    public NotFoundException(String message) {
        super(404, message);
    }

    public static NotFoundException of(String message) {
        return new NotFoundException(message);
    }
}
