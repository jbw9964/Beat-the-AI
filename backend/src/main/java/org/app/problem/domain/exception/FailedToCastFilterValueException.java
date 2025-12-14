package org.app.problem.domain.exception;

import org.app.util.exception.*;

public class FailedToCastFilterValueException extends InternalServerErrorException {

    private static final String clientShowMsg = "필터링 값을 읽는중 문제가 발생했습니다.";

    public FailedToCastFilterValueException(String message, ClassCastException cause) {
        super(message, clientShowMsg, cause);
    }
}
