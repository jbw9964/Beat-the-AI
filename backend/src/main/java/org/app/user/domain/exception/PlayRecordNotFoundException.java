package org.app.user.domain.exception;

import org.app.util.exception.*;

public class PlayRecordNotFoundException extends NotFoundException {

    private static final String message = "";

    public PlayRecordNotFoundException() {
        super(message);
    }

    public PlayRecordNotFoundException(String message) {
        super(message);
    }
}
