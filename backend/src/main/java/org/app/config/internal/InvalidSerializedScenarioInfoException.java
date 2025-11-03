package org.app.config.internal;

public class InvalidSerializedScenarioInfoException extends RuntimeException {

    public InvalidSerializedScenarioInfoException() {

    }

    public InvalidSerializedScenarioInfoException(String message) {
        super(message);
    }

    public InvalidSerializedScenarioInfoException(Throwable cause) {
        super(cause);
    }
}
