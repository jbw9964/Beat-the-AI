package org.app.problem.service;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import org.*;
import org.app.config.domain.*;
import org.app.entity.*;
import org.app.problem.dto.*;
import org.app.problem.dto.request.*;
import org.app.util.exception.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class ProblemInfoAdaptorTest extends IntegrationTestSupport {

    private static final ScenarioInfo[] validScenarioInfos = new ScenarioInfo[]{
            gen(0, "0"), gen(1, "1"),
            gen(2, "2"), gen(3, "3"),
            gen(4, "4"), gen(5, "5")
    };
    @Autowired
    ProblemInfoAdaptor adaptor;
    @Autowired
    ScenarioInfoDeserializer deserializer;

    private static ScenarioInfo gen(int order, String content) {
        return new ScenarioInfo(order, content);
    }

    @Test
    @DisplayName("유효한 요청에 대해선 Exception 이 발생하지 않는다.")
    void testValidCase() {
        String title = "title";
        String description = "description";
        String rewardMessage = "rewardMessage";
        int nOfSToGetReward = validScenarioInfos.length - 1;
        Integer nOfSToFailPlay;
        ProblemVisibility visibility = ProblemVisibility.PUBLIC;

        for (int i = 0; i < 2; i++) {
            boolean useNull = (i & 0b1) == 0b1;

            nOfSToFailPlay = useNull ? null : validScenarioInfos.length - 1;

            CreateProblemRequest createProblemRequest = new CreateProblemRequest(
                    title, description, rewardMessage,
                    nOfSToGetReward, nOfSToFailPlay,
                    visibility, validScenarioInfos
            );
            UpdateProblemRequest updateProblemRequest = new UpdateProblemRequest(
                    title, description, rewardMessage,
                    nOfSToGetReward, nOfSToFailPlay, validScenarioInfos
            );

            SerializedProblemCreationInfo response1 = adaptor.getSerializedInfoOrThrowEx(
                    createProblemRequest
            );
            SerializedProblemUpdateInfo response2 = adaptor.getSerializedInfoOrThrowEx(
                    updateProblemRequest
            );

            int expectedNOfSToFailPlay = !useNull ? nOfSToFailPlay : validScenarioInfos.length;

            assertThat(response1).isNotNull();
            assertThat(response2).isNotNull();

            assertThat(response1.title()).isEqualTo(title);
            assertThat(response1.description()).isEqualTo(description);
            assertThat(response1.rewardMessage()).isEqualTo(rewardMessage);
            assertThat(response1.numOfScenariosToGetReward()).isEqualTo(nOfSToGetReward);
            assertThat(response1.numOfScenariosToFailPlay()).isEqualTo(expectedNOfSToFailPlay);
            assertThat(response1.visibility()).isEqualTo(visibility);

            assertThat(response2.title()).isEqualTo(title);
            assertThat(response2.description()).isEqualTo(description);
            assertThat(response2.rewardMessage()).isEqualTo(rewardMessage);
            assertThat(response2.numOfScenariosToGetReward()).isEqualTo(nOfSToGetReward);
            assertThat(response2.numOfScenariosToFailPlay()).isEqualTo(expectedNOfSToFailPlay);

            String serializedScenarioInfo1 = response1.serializedScenarioInfo();
            String serializedScenarioInfo2 = response2.serializedScenarioInfo();

            assertThat(serializedScenarioInfo1).isNotBlank();
            assertThat(serializedScenarioInfo2).isNotBlank();

            for (String info : new String[]{
                    serializedScenarioInfo1, serializedScenarioInfo2
            }) {

                ScenarioInfo[] deserialize = deserializer.deserialize(info);

                assertThat(deserialize).isNotNull().hasSize(validScenarioInfos.length);

                for (int j = 0; j < deserialize.length; j++) {
                    ScenarioInfo given = deserialize[j];
                    ScenarioInfo expected = validScenarioInfos[j];

                    assertThat(given).isNotNull().isEqualTo(expected);
                }
            }
        }
    }

    @Test
    @DisplayName("보상을 얻을 수 있는 성공 횟수가 총 시나리오 개수보다 더 크면 "
                 + "BadRequestException 을 일으킨다.")
    void testBadRequestException1() {
        int testSize = 10;

        String title = "title";
        String description = "description";
        String rewardMessage = "rewardMessage";
        int nOfSToFailPlay = validScenarioInfos.length - 1;
        ProblemVisibility visibility = ProblemVisibility.PUBLIC;

        for (
                int trial = validScenarioInfos.length + 1;
                trial <= validScenarioInfos.length + testSize;
                trial++
        ) {

            CreateProblemRequest createProblemRequest = new CreateProblemRequest(
                    title, description, rewardMessage,
                    trial, nOfSToFailPlay,
                    visibility, validScenarioInfos
            );
            UpdateProblemRequest updateProblemRequest = new UpdateProblemRequest(
                    title, description, rewardMessage,
                    trial, nOfSToFailPlay, validScenarioInfos
            );

            assertThatThrownBy(() -> adaptor.getSerializedInfoOrThrowEx(createProblemRequest))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> adaptor.getSerializedInfoOrThrowEx(updateProblemRequest))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    @Test
    @DisplayName("더이상 플레이 불가능한 실패 횟수가 존재하고 1 보다 작거나 "
                 + "총 시나리오 개수보다 많으면 BadRequestException 을 일으킨다.")
    void testBadRequestException2() {
        final int len = validScenarioInfos.length;

        String title = "title";
        String description = "description";
        String rewardMessage = "rewardMessage";
        int nOfSToGetReward = validScenarioInfos.length - 1;
        ProblemVisibility visibility = ProblemVisibility.PUBLIC;

        List<Integer> trials = List.of(
                0, len + 1, len + 2, len + 3
        );

        for (Integer trial : trials) {

            CreateProblemRequest createProblemRequest = new CreateProblemRequest(
                    title, description, rewardMessage,
                    nOfSToGetReward, trial,
                    visibility, validScenarioInfos
            );
            UpdateProblemRequest updateProblemRequest = new UpdateProblemRequest(
                    title, description, rewardMessage,
                    nOfSToGetReward, trial, validScenarioInfos
            );

            assertThatThrownBy(() -> adaptor.getSerializedInfoOrThrowEx(createProblemRequest))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> adaptor.getSerializedInfoOrThrowEx(updateProblemRequest))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    @Test
    @DisplayName("직렬화할 수 없는 시나리오 정보가 제공되면 BadRequestException 이 발생한다.")
    void testBadRequestException3() {

        String title = "title";
        String description = "description";
        String rewardMessage = "rewardMessage";
        int nOfSToGetReward = validScenarioInfos.length - 1;
        int nOfSToFailPlay = validScenarioInfos.length - 1;
        ProblemVisibility visibility = ProblemVisibility.PUBLIC;

        ScenarioInfo[] invalid = new ScenarioInfo[]{
                gen(0, "0"), gen(1, "1"),
                gen(10, "10"), gen(3, "3")
        };

        CreateProblemRequest createProblemRequest = new CreateProblemRequest(
                title, description, rewardMessage,
                nOfSToGetReward, nOfSToFailPlay,
                visibility, invalid
        );
        UpdateProblemRequest updateProblemRequest = new UpdateProblemRequest(
                title, description, rewardMessage,
                nOfSToGetReward, nOfSToFailPlay, invalid
        );

        assertThatThrownBy(() -> adaptor.getSerializedInfoOrThrowEx(createProblemRequest))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> adaptor.getSerializedInfoOrThrowEx(updateProblemRequest))
                .isInstanceOf(BadRequestException.class);
    }
}