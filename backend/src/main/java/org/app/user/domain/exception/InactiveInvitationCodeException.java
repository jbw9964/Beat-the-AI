package org.app.user.domain.exception;

import org.app.util.exception.*;

public class InactiveInvitationCodeException extends ConflictException {

    private static final String message = """
            유효하지 않은 초대코드입니다. 문제 또는 초대코드가 삭제되었는지 확인해 주세요.
            """.trim();

    public InactiveInvitationCodeException() {
        super(message);
    }

    public InactiveInvitationCodeException(String message) {
        super(message);
    }
}
