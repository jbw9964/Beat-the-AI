package org.app.config.domain;

public class InvalidSerializedScenarioInfoException extends RuntimeException {

    public InvalidSerializedScenarioInfoException() {

    }

    public InvalidSerializedScenarioInfoException(String message) {
        super(message);
    }

    public InvalidSerializedScenarioInfoException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidSerializedScenarioInfoException(Throwable cause) {
        super(cause);
    }
}
