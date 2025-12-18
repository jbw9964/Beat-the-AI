package org.app.config.domain.scenario;

public class InvalidScenarioInfoException extends RuntimeException {

    public InvalidScenarioInfoException() {
    }

    public InvalidScenarioInfoException(String message) {
        super(message);
    }
}
