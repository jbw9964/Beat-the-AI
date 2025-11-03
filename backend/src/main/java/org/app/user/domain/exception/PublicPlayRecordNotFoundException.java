package org.app.user.domain.exception;

import org.app.util.exception.*;

public class PublicPlayRecordNotFoundException extends NotFoundException {

    private static final String message = "공개 플레이 기록을 찾을 수 없습니다.";

    public PublicPlayRecordNotFoundException() {
        super(message);
    }

    public PublicPlayRecordNotFoundException(String message) {
        super(message);
    }
}
