package org.app.problem.domain.exception;

import org.app.util.exception.*;

public class OrderTypeMismatchException extends InternalServerErrorException {

    public OrderTypeMismatchException(String message) {
        super(message);
    }
}
