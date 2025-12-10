package org.app.problem.domain.exception;

import org.app.util.exception.*;

public class FilterTypeMissMatchException
        extends InternalServerErrorException {

    public FilterTypeMissMatchException(String message) {
        super(message);
    }
}
