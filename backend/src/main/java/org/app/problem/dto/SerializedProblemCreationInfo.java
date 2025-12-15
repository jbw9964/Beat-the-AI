package org.app.problem.dto;

import org.app.entity.*;

public record SerializedProblemCreationInfo(
        String title,
        String description,
        String rewardMessage,
        int numOfScenariosToGetReward,
        int numOfScenariosToFailPlay,
        ProblemVisibility visibility,
        String serializedScenarioInfo
) {

}
