package org.app.problem.dto;

public record SerializedProblemUpdateInfo(
        String title,
        String description,
        String rewardMessage,
        int numOfScenariosToGetReward,
        int numOfScenariosToFailPlay,
        int numOfTotalScenarios,
        String serializedScenarioInfo
) {

}
