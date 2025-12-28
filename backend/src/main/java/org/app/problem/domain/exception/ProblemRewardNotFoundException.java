package org.app.problem.domain.exception;

import org.app.util.exception.*;

public class ProblemRewardNotFoundException extends NotFoundException {

    private static final String message = "주어진 ID 에 해당하는 보상을 찾을 수 없습니다.";

    public ProblemRewardNotFoundException() {
        super(message);
    }
}
