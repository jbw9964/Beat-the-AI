package org.app.user.domain.exception;

import org.app.util.exception.*;

public class ReceivedInvitationNotFoundException extends NotFoundException {

    private static final String message = "수령한 초대코드를 찾을 수 없습니다.";

    public ReceivedInvitationNotFoundException() {
        super(message);
    }

    public ReceivedInvitationNotFoundException(String message) {
        super(message);
    }
}
