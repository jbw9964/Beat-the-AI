package org.app.problem.service;

import com.fasterxml.jackson.core.*;
import java.util.*;
import lombok.*;
import org.app.config.domain.*;
import org.app.entity.*;
import org.app.problem.dto.*;
import org.app.problem.dto.request.*;
import org.app.util.exception.*;
import org.springframework.stereotype.*;

@Component
@RequiredArgsConstructor
public class ProblemInfoAdaptor {

    private final ScenarioInfoValidator validator;
    private final ScenarioInfoSerializer serializer;

    private void validateRequestOrThrowBadReqEx(
            ScenarioInfo[] scenarioInfos, int toGetReward, Integer toFailPlay
    )
            throws BadRequestException {

        int numOfTotalScenarios = scenarioInfos.length;

        if (numOfTotalScenarios < toGetReward) {
            throw new BadRequestException(String.format(
                    "보상을 수령할 수 있는 시나리오 성공 개수는 전체 시나리오 개수보다 작거나 같아야 합니다: "
                    + "(전체 시나리오 개수=%d, 주어진 성공 개수=%d)",
                    numOfTotalScenarios, toGetReward
            ));
        }

        if (
                toFailPlay != null &&
                (toFailPlay < 1 || numOfTotalScenarios < toFailPlay)
        ) {
            throw new BadRequestException(String.format(
                    "플레이 불가능한 실패 개수는 제공되지 않거나 [1, 전체 시나리오 개수] 사이의 정수여야 합니다: "
                    + "(전체 시나리오 개수=%d, 주어진 실패 허용 개수=%d) ",
                    numOfTotalScenarios, toFailPlay
            ));
        }
    }

    private String getSerializedScenarioInfoOrThrowEx(
            ScenarioInfo[] scenarioInfos
    )
            throws BadRequestException, InternalServerErrorException {

        ScenarioInfo[] orderedScenarioInfos = null;
        boolean serializable = false;

        if (
                scenarioInfos != null &&
                scenarioInfos.length > 0
        ) {
            orderedScenarioInfos = Arrays.stream(scenarioInfos)
                    .sorted()
                    .toArray(ScenarioInfo[]::new);

            serializable = validator.serializable(orderedScenarioInfos);
        }

        if (!serializable) {
            throw new BadRequestException(
                    "제공한 시나리오 정보는 직렬화 할 수 없습니다. "
                    + "정보간 순서, null 체크 여부를 확인해 주세요."
            );
        }

        try {
            return serializer.serialize(orderedScenarioInfos);
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException(
                    "Failed to serialize scneario info even",
                    "시나리오 정보를 직렬화 중 문제가 발생했습니다.",
                    e
            );
        }
    }

    public SerializedProblemCreationInfo getSerializedInfoOrThrowEx(
            CreateProblemRequest request
    )
            throws BadRequestException, InternalServerErrorException {

        ScenarioInfo[] scenarioInfos = request.scenarioInfos();
        int toGetReward = request.numOfScenariosToGetReward();
        Integer toFailPlay = request.numOfScenariosToFailPlay();

        this.validateRequestOrThrowBadReqEx(
                scenarioInfos, toGetReward, toFailPlay
        );

        String serializedScenarioInfo = this.getSerializedScenarioInfoOrThrowEx(
                scenarioInfos
        );

        return new SerializedProblemCreationInfo(
                request.title(), request.description(),
                request.rewardMessage(), toGetReward,
                toFailPlay != null ? toFailPlay : scenarioInfos.length,
                request.visibility(), serializedScenarioInfo
        );
    }

    public SerializedProblemUpdateInfo getSerializedInfoOrThrowEx(
            UpdateProblemRequest request
    )
            throws BadRequestException, InternalServerErrorException {

        ScenarioInfo[] scenarioInfos = request.scenarioInfos();
        int toGetReward = request.numOfScenariosToGetReward();
        Integer toFailPlay = request.numOfScenariosToFailPlay();

        this.validateRequestOrThrowBadReqEx(
                scenarioInfos, toGetReward, toFailPlay
        );

        String serializedScenarioInfo = this.getSerializedScenarioInfoOrThrowEx(
                scenarioInfos
        );

        return new SerializedProblemUpdateInfo(
                request.title(), request.description(),
                request.rewardMessage(), toGetReward,
                toFailPlay != null ? toFailPlay : scenarioInfos.length,
                serializedScenarioInfo
        );
    }
}
