package org.app.config.domain.scenario.internal;

import java.util.*;
import org.app.config.domain.scenario.*;
import org.app.entity.*;

public class ScenarioInfoValidatorImpl implements ScenarioInfoValidator {

    @Override
    public boolean serializable(List<ScenarioInfo> scenarioInfos) {
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

    @Override
    public boolean serializable(ScenarioInfo[] scenarioInfos) {
        if (scenarioInfos == null || scenarioInfos.length == 0) {
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

    @Override
    public boolean deserializable(String serializedScenarioInfo) {
        return serializedScenarioInfo != null && !serializedScenarioInfo.isEmpty();
    }
}
