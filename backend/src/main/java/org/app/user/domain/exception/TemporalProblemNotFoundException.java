package org.app.user.domain.exception;

import org.app.util.exception.*;

public class TemporalProblemNotFoundException extends NotFoundException {

    private static final String message = "주어진 ID 와 연관된 임시저장 내용을 찾을 수 없습니다.";

    public TemporalProblemNotFoundException() {
        super(message);
    }

    public TemporalProblemNotFoundException(String message) {
        super(message);
    }
}
