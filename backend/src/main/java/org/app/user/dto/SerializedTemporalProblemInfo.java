package org.app.user.dto;

import org.app.entity.*;

public record SerializedTemporalProblemInfo(
        String title,
        String description,
        String rewardMessage,
        Integer numOfScenariosToGetReward,
        Integer numOfScenariosToFailPlay,
        ProblemVisibility visibility,
        String serializedScenarioInfos
) {

}
