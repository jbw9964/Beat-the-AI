package org.app.user.domain.exception;

import org.app.util.exception.*;

public class GainedRewardNotFoundException extends NotFoundException {

    private static final String message = "주어진 보상 내용을 찾을 수 없습니다.";

    public GainedRewardNotFoundException() {
        super(message);
    }

    public GainedRewardNotFoundException(String message) {
        super(message);
    }
}
