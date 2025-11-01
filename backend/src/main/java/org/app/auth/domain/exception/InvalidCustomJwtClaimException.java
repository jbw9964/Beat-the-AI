package org.app.auth.domain.exception;

public class InvalidCustomJwtClaimException extends RuntimeException {

    public InvalidCustomJwtClaimException(String message) {
        super(message);
    }
}
