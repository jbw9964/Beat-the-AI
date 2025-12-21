package org.app.config.domain.image;

import org.app.util.exception.*;

public class FailedToSaveImageToServerException extends InternalServerErrorException {

    private static final String clientShowMsg = "이미지를 서버에 저장하던 중 문제가 발생했습니다.";

    public FailedToSaveImageToServerException(String message, Throwable cause) {
        super(message, clientShowMsg, cause);
    }
}
