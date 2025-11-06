package org.app.config.domain;

public class InvalidScenarioInfoException extends RuntimeException {

    public InvalidScenarioInfoException() {
    }

    public InvalidScenarioInfoException(String message) {
        super(message);
    }
}
