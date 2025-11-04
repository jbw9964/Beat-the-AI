package org.app.user.domain.exception;

import org.app.util.exception.*;

public class ProblemNotFoundException extends NotFoundException {

    private static final String message = "문제를 찾을 수 없습니다.";

    public ProblemNotFoundException() {
        super(message);
    }

    public ProblemNotFoundException(String message) {
        super(message);
    }
}
