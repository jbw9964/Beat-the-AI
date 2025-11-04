package org.app.user.domain.exception;

import org.app.util.exception.*;

public class PasswordMismatchException extends ForbiddenException {

    private static final String message = "패스워드가 일치하지 않습니다.";

    public PasswordMismatchException() {
        super(message);
    }

    public PasswordMismatchException(String message) {
        super(message);
    }
}
