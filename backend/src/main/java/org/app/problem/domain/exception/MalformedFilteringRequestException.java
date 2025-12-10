package org.app.problem.domain.exception;

import org.app.util.exception.*;

public class MalformedFilteringRequestException extends BadRequestException {

    public MalformedFilteringRequestException(String message) {
        super(message);
    }

}
