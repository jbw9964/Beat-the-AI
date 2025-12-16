package org.app.problem.domain.exception;

import org.app.util.exception.*;

public class ProblemNotFoundException extends NotFoundException {

    public static final String message = "주어진 ID 에 해당하는 문제를 찾을 수 없습니다.";

    public ProblemNotFoundException() {
        super(message);
    }
}
