package org.app.user.dto;

import org.app.entity.*;

public record DetailedTemporalProblemInfo(
        Long temporalProblemId,
        String title,
        String description,
        String rewardMessage,
        ProblemVisibility visibility,
        Integer numOfScenariosToGetReward,
        Integer numOfScenariosToFailPlay,
        int numOfTotalScenarios,
        ScenarioInfo[] scenarioInfos
) {


    public DetailedTemporalProblemInfo(
            SimpleTemporalProblemInfo simpleInfo,
            Integer numOfScenariosToGetReward, Integer numOfScenariosToFailPlay,
            int numOfTotalScenarios, ScenarioInfo[] scenarioInfos
    ) {
        this(
                simpleInfo.temporalProblemId(), simpleInfo.title(), simpleInfo.description(),
                simpleInfo.rewardMessage(), simpleInfo.visibility(),
                numOfScenariosToGetReward, numOfScenariosToFailPlay,
                numOfTotalScenarios, scenarioInfos
        );
    }
}
