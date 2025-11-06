package org.app.config.domain;

import org.app.entity.*;
import org.springframework.modulith.*;

@NamedInterface
public interface ScenarioInfoDeserializer {

    ScenarioInfo[] deserialize(String serialized)
            throws InvalidSerializedScenarioInfoException, InvalidScenarioInfoException;
}
