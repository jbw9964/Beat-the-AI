package org.app.util.exception;

public class NotImplementedException extends CustomException {

    public NotImplementedException(String message) {
        super(501, message);
    }

    public static NotImplementedException of(String message) {
        return new NotImplementedException(message);
    }
}
