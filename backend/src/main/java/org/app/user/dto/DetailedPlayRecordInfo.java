package org.app.user.dto;

import java.time.*;
import lombok.*;
import org.app.entity.*;

public record DetailedPlayRecordInfo(
        Long playRecordId,
        Long problemId,
        String title,
        String description,
        PlayRecordStatus status,
        PlayRecordVisibility visibility,
        int numOfTotalScenarios,
        int numOfSubmittedScenarios,
        int numOfPassedScenarios,
        int numOfScenariosToGetReward,
        int numOfScenariosToFailPlay,
        LocalDateTime createdAt
) {

    public DetailedPlayRecordInfo(
            @NonNull SimplePlayRecordInfo simpleInfo,
            int numOfTotalScenarios, int numOfSubmittedScenarios,
            int numOfPassedScenarios, int numOfScenariosToGetReward,
            int numOfScenariosToFailPlay
    ) {
        this(
                simpleInfo.playRecordId(), simpleInfo.problemId(),
                simpleInfo.title(), simpleInfo.description(),
                simpleInfo.status(), simpleInfo.visibility(),
                numOfTotalScenarios, numOfSubmittedScenarios,
                numOfPassedScenarios, numOfScenariosToGetReward,
                numOfScenariosToFailPlay, simpleInfo.createdAt()
        );
    }
}
