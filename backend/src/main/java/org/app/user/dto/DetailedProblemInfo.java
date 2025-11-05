package org.app.user.dto;

import java.time.*;
import java.util.*;
import lombok.*;
import org.app.entity.*;

public record DetailedProblemInfo(
        Long problemId,
        String title,
        String description,
        String rewardMessage,
        int numOfTotalScenarios,
        int numOfScenariosToGetReward,
        int numOfScenariosToFailPlay,
        ProblemVisibility visibility,
        List<ScenarioInfo> scenarioInfos,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {

    public DetailedProblemInfo(
            @NonNull SimpleProblemInfo simpleProblemInfo,
            @NonNull List<ScenarioInfo> scenarioInfos
    ) {
        this(
                simpleProblemInfo.problemId(), simpleProblemInfo.title(),
                simpleProblemInfo.description(), simpleProblemInfo.rewardMessage(),

                scenarioInfos.size(),

                simpleProblemInfo.numOfScenariosToGetReward(),
                simpleProblemInfo.numOfScenariosToFailPlay(),
                simpleProblemInfo.visibility(),

                scenarioInfos,

                simpleProblemInfo.createdAt(), simpleProblemInfo.modifiedAt()
        );
    }
}
