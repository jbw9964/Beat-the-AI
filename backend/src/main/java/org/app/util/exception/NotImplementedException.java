package org.app.util.exception;

import org.springframework.http.*;

public class NotImplementedException extends CustomException {

    public NotImplementedException(String message) {
        super(HttpStatus.NOT_IMPLEMENTED.value(), message);
    }

    public static NotImplementedException of(String message) {
        return new NotImplementedException(message);
    }
}
