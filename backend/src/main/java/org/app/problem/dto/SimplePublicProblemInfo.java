package org.app.problem.dto;

import java.time.*;

public record SimplePublicProblemInfo(
        Long problemId,
        Long userId,
        String title,
        String description,
        int numOfScenariosToGetReward,
        int numOfScenariosToFailPlay,
        int numOfTotalScenarios,
        int numOfRewardSets,
        AggregatedInfo aggregatedInfo,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {

}
