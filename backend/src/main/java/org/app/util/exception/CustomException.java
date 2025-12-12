package org.app.util.exception;

import lombok.*;

@Getter
public abstract class CustomException extends RuntimeException {

    private final int code;
    private final String message;

    protected CustomException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    protected CustomException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
}
