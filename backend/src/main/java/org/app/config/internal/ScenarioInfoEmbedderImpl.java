package org.app.config.internal;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.app.entity.*;

@Slf4j
@RequiredArgsConstructor
@SuppressWarnings({"ClassCanBeRecord", "BooleanMethodIsAlwaysInverted"})
class ScenarioInfoEmbedderImpl implements ScenarioInfoEmbedder {

    private static final Class<ScenarioInfo[]> CLAZZ = ScenarioInfo[].class;
    private final ObjectMapper objMapper;

    @Override
    public ScenarioInfo[] deserialize(String serialized) {

        if (!valid(serialized)) {
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

        if (!valid(deserialized)) {
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

    @Override
    public String serialize(List<ScenarioInfo> scenarioInfos) throws JsonProcessingException {

        if (!valid(scenarioInfos)) {
            throw new InvalidScenarioInfoException();
        }

        return objMapper.writeValueAsString(scenarioInfos);
    }

    @Override
    public String serialize(ScenarioInfo[] scenarioInfos) throws JsonProcessingException {

        if (!valid(scenarioInfos)) {
            throw new InvalidScenarioInfoException();
        }

        return objMapper.writeValueAsString(scenarioInfos);
    }

    private boolean valid(String serialized) {
        return serialized != null && !serialized.isEmpty();
    }

    private boolean valid(List<ScenarioInfo> scenarioInfos) {

        if (scenarioInfos == null || scenarioInfos.isEmpty()) {
            return false;
        }

        int expected = 0;
        for (ScenarioInfo scenarioInfo : scenarioInfos) {
            if (
                    scenarioInfo == null ||
                    scenarioInfo.getScenarioOrder() != expected++
            ) {
                return false;
            }
        }

        return true;
    }

    private boolean valid(ScenarioInfo[] scenarioInfos) {

        if (scenarioInfos == null || scenarioInfos.length == 0) {
            return false;
        }

        return valid(List.of(scenarioInfos));
    }
}
