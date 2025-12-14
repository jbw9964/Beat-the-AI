package org.app.problem.domain.exception;

import org.app.util.exception.*;

public class ImproperOrderTypesException extends BadRequestException {

    public ImproperOrderTypesException(String message) {
        super(message);
    }
}
