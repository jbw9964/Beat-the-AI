package org.app.user.service;

import java.util.*;
import lombok.*;
import org.app.config.domain.scenario.*;
import org.app.entity.*;
import org.app.user.dto.*;
import org.app.user.dto.request.*;
import org.springframework.stereotype.*;

@Component
@RequiredArgsConstructor
public class TemporalProblemInfoAdaptor {

    private final ScenarioInfoValidator validator;
    private final ScenarioInfoSerializer serializer;

    public boolean areScenarioInfosSerializable(ScenarioInfo[] scenarioInfos) {

        if (scenarioInfos == null) {
            return true;
        }

        ScenarioInfo[] orderedInfos = Arrays.stream(scenarioInfos).sorted()
                .toArray(ScenarioInfo[]::new);

        return validator.serializable(orderedInfos);
    }

    public SerializedTemporalProblemInfo getSerializedInfo(CreateTemporalProblemRequest request) {
        String serializedInfo = this.getSerializedScenarioInfo(request.scenarioInfos());
        return new SerializedTemporalProblemInfo(
                request.title(), request.description(), request.rewardMessage(),
                request.numOfScenariosToGetReward(), request.numOfScenariosToFailPlay(),
                request.visibility(), serializedInfo
        );
    }

    private String getSerializedScenarioInfo(ScenarioInfo[] scenarioInfos) {
        if (scenarioInfos == null || scenarioInfos.length == 0) {
            return null;
        }

        try {
            return serializer.serialize(
                    Arrays.stream(scenarioInfos).sorted()
                            .toArray(ScenarioInfo[]::new)
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize info due to: " + e.getCause(), e);
        }
    }
}
