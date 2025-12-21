package org.app.config.domain.image;

import org.app.util.exception.*;

public class FailedToFindImageOnServerException extends InternalServerErrorException {

    private static final String clientShowMsg
            = "이미지 정보는 존재했지만 서버 저장소에서 찾을 수 없었습니다.";

    public FailedToFindImageOnServerException(String message, Throwable cause) {
        super(message, clientShowMsg, cause);
    }
}
