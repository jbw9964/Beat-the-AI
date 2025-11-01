package org.app.auth.domain.exception;

import org.app.util.exception.*;

public class DuplicateLoginIDException extends BadRequestException {

    private static final String message = "중복된 로그인 ID 입니다.";

    public DuplicateLoginIDException() {
        super(message);
    }
}
