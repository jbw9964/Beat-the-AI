package org.app.util.exception;

import org.springframework.http.*;

public class NotImplementedException extends ExpectableServerErrorException {

    private static final String defaultClientShowMsg = "해당 기능은 아직 구현되지 않았습니다.";

    public NotImplementedException(
            String message, String clientResponseMessage,
            Throwable cause
    ) {
        super(
                HttpStatus.NOT_IMPLEMENTED.value(), message,
                clientResponseMessage, cause
        );
    }

    public NotImplementedException(
            String message, String clientResponseMessage
    ) {
        this(message, clientResponseMessage, null);
    }

    public NotImplementedException(String message) {
        this(message, defaultClientShowMsg);
    }
}
