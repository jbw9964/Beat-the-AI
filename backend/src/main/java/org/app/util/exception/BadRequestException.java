package org.app.util.exception;

public class BadRequestException extends CustomException {

    public BadRequestException(String message) {
        super(400, message);
    }

    public static BadRequestException of(String message) {
        return new BadRequestException(message);
    }
}
