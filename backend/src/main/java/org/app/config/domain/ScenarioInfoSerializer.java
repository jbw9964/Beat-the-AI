package org.app.config.domain;

import com.fasterxml.jackson.core.*;
import java.util.*;
import org.app.entity.*;
import org.springframework.modulith.*;

@NamedInterface
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public interface ScenarioInfoSerializer {

    String serialize(List<ScenarioInfo> scenarioInfos)
            throws JsonProcessingException, InvalidScenarioInfoException;

    String serialize(ScenarioInfo[] scenarioInfos)
            throws JsonProcessingException, InvalidScenarioInfoException;
}
