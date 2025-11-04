package org.app.user.domain.exception;

import org.app.util.exception.*;

public class RatingNotFoundException extends NotFoundException {

    private static final String message = "문제 평가 기록을 찾을 수 없습니다.";

    public RatingNotFoundException() {
        super(message);
    }

    public RatingNotFoundException(String message) {
        super(message);
    }
}
