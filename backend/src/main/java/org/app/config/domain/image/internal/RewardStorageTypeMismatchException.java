package org.app.config.domain.image.internal;

import org.app.util.exception.*;

public class RewardStorageTypeMismatchException extends InternalServerErrorException {

    private static final String clientShowMsg = "보상 이미지를 처리하던 중 문제가 발생했습니다.";

    public RewardStorageTypeMismatchException(String message) {
        super(message, clientShowMsg);
    }
}
