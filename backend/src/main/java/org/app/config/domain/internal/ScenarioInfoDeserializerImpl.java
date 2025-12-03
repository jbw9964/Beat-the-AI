package org.app.config.domain.internal;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.app.config.domain.*;
import org.app.entity.*;

@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("ClassCanBeRecord")
public class ScenarioInfoDeserializerImpl implements ScenarioInfoDeserializer {

    private static final Class<ScenarioInfo[]> CLAZZ = ScenarioInfo[].class;
    private final ObjectMapper objMapper;
    private final ScenarioInfoValidator validator;

    @Override
    public ScenarioInfo[] deserialize(String serialized)
            throws InvalidSerializedScenarioInfoException, InvalidScenarioInfoException {

        if (!validator.deserializable(serialized)) {
            RuntimeException cause = new IllegalArgumentException();
            throw new InvalidSerializedScenarioInfoException(
                    "Invalid serialized scenario info: " + serialized, cause
            );
        }

        ScenarioInfo[] deserialized;
        try {
            deserialized = objMapper.readValue(serialized, CLAZZ);
        } catch (JsonProcessingException e) {
            throw new InvalidSerializedScenarioInfoException(
                    "Failed to deserialize ScenarioInfo", e
            );
        }

        if (!validator.serializable(deserialized)) {
            log.warn(
                    "Somehow deserialized ScenarioInfo isn't valid. Given: {}",
                    serialized
            );
            throw new InvalidScenarioInfoException(
                    "Succeeded deserialization, but deserialized ScenarioInfo isn't valid"
            );
        }

        return deserialized;
    }
}
