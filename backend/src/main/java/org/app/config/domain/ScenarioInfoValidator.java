package org.app.config.domain;

import java.util.*;
import org.app.entity.*;
import org.springframework.modulith.*;

@NamedInterface
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public interface ScenarioInfoValidator {

    boolean serializable(List<ScenarioInfo> scenarioInfos);

    boolean serializable(ScenarioInfo[] scenarioInfos);

    boolean deserializable(String serializedScenarioInfo);
}
