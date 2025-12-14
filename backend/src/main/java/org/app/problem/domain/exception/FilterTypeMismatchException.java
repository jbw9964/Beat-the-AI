package org.app.problem.domain.exception;

import org.app.util.exception.*;

public class FilterTypeMismatchException
        extends InternalServerErrorException {

    private static final String clientShowMsg = "필터링 종류를 처리하던 중 문제가 발생했습니다.";

    public FilterTypeMismatchException(String message) {
        super(message, clientShowMsg);
    }
}
