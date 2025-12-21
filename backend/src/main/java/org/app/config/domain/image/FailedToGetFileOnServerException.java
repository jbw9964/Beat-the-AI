package org.app.config.domain.image;

import org.app.util.exception.*;

public class FailedToGetFileOnServerException extends InternalServerErrorException {

    private static final String clientShowMsg = "서버 내 이미지를 가져오던 중 문제가 발생했습니다.";

    public FailedToGetFileOnServerException(String message, Throwable cause) {
        super(message, clientShowMsg, cause);
    }
}
