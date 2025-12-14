package org.app.util.exception;

import lombok.*;

@Getter
public class ExpectableServerErrorException extends CustomException {

    private final String clientResponseMessage;

    protected ExpectableServerErrorException(
            int code, String message,
            String clientResponseMessage, Throwable cause
    ) {
        super(code, message, cause);
        this.clientResponseMessage = clientResponseMessage;
    }

}
