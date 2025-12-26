package org.app.problem.domain.exception;

import org.app.util.exception.*;

public class FailedToGetImageFromRequestException extends InternalServerErrorException {

    private static final String clientShowMsg = "요청 이미지를 가져오는 중 문제가 발생했습니다.";

    public FailedToGetImageFromRequestException(String message, Throwable cause) {
        super(message, clientShowMsg, cause);
    }
}
