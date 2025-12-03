package org.app.user.dto.request;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import org.app.entity.*;

public record CreateTemporalProblemRequest(
        @NotBlank(message = "임시저장 제목을 제공해 주세요.")
        String title,
        String description,
        String rewardMessage,
        Integer numOfScenariosToGetReward,
        Integer numOfScenariosToFailPlay,
        ProblemVisibility visibility,
        @Valid ScenarioInfo[] scenarioInfos
) {

}
