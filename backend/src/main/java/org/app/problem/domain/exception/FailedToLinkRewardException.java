package org.app.problem.domain.exception;

import org.app.util.exception.*;

public class FailedToLinkRewardException extends InternalServerErrorException {

    private static final String clientShowMsg = "문제 보상을 저장하는 중 문제가 발생했습니다.";

    public FailedToLinkRewardException(String message, Throwable cause) {
        super(message, clientShowMsg, cause);
    }
}
