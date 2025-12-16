package org.app.problem.domain.exception;

import org.app.util.exception.*;

public class UserNotFoundException extends NotFoundException {

    private static final String message = "사용자를 찾을 수 없습니다.";

    public UserNotFoundException() {
        super(message);
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
