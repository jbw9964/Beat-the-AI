package org.app.problem.domain.exception;

import org.app.util.exception.*;

public class UnexpectedNotFoundException extends InternalServerErrorException {

    public UnexpectedNotFoundException(String message) {
        super(message, null);
    }
}
