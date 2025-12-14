package org.app.problem.domain.exception;

import org.app.util.exception.*;

public class MalformedFilterRequestException extends BadRequestException {

    public MalformedFilterRequestException(String message) {
        super(message);
    }

}
