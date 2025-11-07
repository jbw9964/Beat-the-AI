package org.app.user.domain.exception;

import org.app.util.exception.*;

public class AlreadyReceivedInvitationException extends ConflictException {

    private static final String message = "주어진 초대코드를 이미 수령하였습니다.";

    public AlreadyReceivedInvitationException() {
        super(message);
    }

    public AlreadyReceivedInvitationException(String message) {
        super(message);
    }
}
