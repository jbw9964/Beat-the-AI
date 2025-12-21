package org.app.config.domain.image;

import org.app.util.exception.*;

public class FailedToCreateBlurImageException extends InternalServerErrorException {

    private static final String clientShowMsg = "블러 이미지를 생성하던 중 문제가 발생했습니다.";

    public FailedToCreateBlurImageException(String message, Throwable cause) {
        super(message, clientShowMsg, cause);
    }
}
