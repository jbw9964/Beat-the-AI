package org.app.problem.domain.exception;

import org.app.util.exception.*;

public class OrderTypeMismatchException extends InternalServerErrorException {

    private static final String clientShowMsg = "정렬 종류를 처리하던 중 문제가 발생했습니다.";

    public OrderTypeMismatchException(String message) {
        super(message, clientShowMsg);
    }
}
