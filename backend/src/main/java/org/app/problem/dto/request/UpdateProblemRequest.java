package org.app.problem.dto.request;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import org.app.entity.*;

public record UpdateProblemRequest(
        @NotBlank(message = "문제 제목을 제공해 주세요.")
        String title,
        String description,
        String rewardMessage,

        @NotNull(message = "보상을 수령할 수 있는 시나리오 성공 개수를 제공해 주세요.")
        @Min(value = 0, message = "보상을 수령할 수 있는 시나리오 성공 개수는 0 보다 크거나 같아야 합니다.")
        Integer numOfScenariosToGetReward,

        Integer numOfScenariosToFailPlay,

        @NotNull(message = "문제에 설정할 시나리오 정보를 제공해 주세요.")
        @Valid ScenarioInfo[] scenarioInfos
) {

}
