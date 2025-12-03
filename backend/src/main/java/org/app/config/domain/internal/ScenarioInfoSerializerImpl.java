package org.app.config.domain.internal;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import java.util.*;
import lombok.*;
import org.app.config.domain.*;
import org.app.entity.*;

@RequiredArgsConstructor
@SuppressWarnings("ClassCanBeRecord")
public class ScenarioInfoSerializerImpl implements ScenarioInfoSerializer {

    private final ObjectMapper objMapper;
    private final ScenarioInfoValidator validator;

    @Override
    public String serialize(List<ScenarioInfo> scenarioInfos)
            throws JsonProcessingException, InvalidScenarioInfoException {

        if (!validator.serializable(scenarioInfos)) {
            throw new InvalidScenarioInfoException();
        }

        return objMapper.writeValueAsString(scenarioInfos);
    }

    @Override
    public String serialize(ScenarioInfo[] scenarioInfos)
            throws JsonProcessingException, InvalidScenarioInfoException {

        if (!validator.serializable(scenarioInfos)) {
            throw new InvalidScenarioInfoException();
        }

        return objMapper.writeValueAsString(scenarioInfos);
    }
}
