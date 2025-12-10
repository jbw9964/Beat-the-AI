package org.app.problem.domain.exception;

import org.app.util.exception.*;

public class IllegalFilterValueException extends BadRequestException {

    public IllegalFilterValueException(String message) {
        super(message);
    }
}
