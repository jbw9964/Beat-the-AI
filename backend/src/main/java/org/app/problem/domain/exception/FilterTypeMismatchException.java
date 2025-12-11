package org.app.problem.domain.exception;

import org.app.util.exception.*;

public class FilterTypeMismatchException
        extends InternalServerErrorException {

    public FilterTypeMismatchException(String message) {
        super(message);
    }
}
