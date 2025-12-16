package org.app.problem.dto;

import java.time.*;
import org.app.entity.*;

public record DetailedProblemInfo(
        Long problemId,
        Long userId,
        String userName,
        String title,
        String description,
        int numOfScenariosToGetReward,
        int numOfScenariosToFailPlay,
        int numOfTotalScenarios,
        int numOfRewardSets,
        ProblemVisibility visibility,
        AggregatedInfo aggregatedInfo,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        boolean isMine
) {

    public DetailedProblemInfo(
            SimplePublicProblemInfo simpleInfo,
            String userName, ProblemVisibility visibility, boolean isMine
    ) {
        this(
                simpleInfo.problemId(), simpleInfo.userId(),
                userName, simpleInfo.title(), simpleInfo.description(),
                simpleInfo.numOfScenariosToGetReward(),
                simpleInfo.numOfScenariosToFailPlay(),
                simpleInfo.numOfTotalScenarios(), simpleInfo.numOfRewardSets(),
                visibility, simpleInfo.aggregatedInfo(),
                simpleInfo.createdAt(), simpleInfo.modifiedAt(),
                isMine
        );
    }
}
