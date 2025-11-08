package org.app.user.domain.exception;

import org.app.util.exception.*;

public class PlayRecordNotFoundException extends NotFoundException {

    private static final String message = "주어진 플레이 기록을 찾을 수 없습니다.";

    public PlayRecordNotFoundException() {
        super(message);
    }

    public PlayRecordNotFoundException(String message) {
        super(message);
    }
}
