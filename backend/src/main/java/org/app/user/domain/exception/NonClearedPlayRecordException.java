package org.app.user.domain.exception;

import org.app.util.exception.*;

public class NonClearedPlayRecordException extends ForbiddenException {

    public static final String message = "해당 플레이 기록이 성공 상태가 아닙니다.";

    public NonClearedPlayRecordException() {
        super(message);
    }

    public NonClearedPlayRecordException(String message) {
        super(message);
    }
}
