package org.app.user.service;

import static org.assertj.core.api.Assertions.*;

import com.fasterxml.jackson.core.*;
import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.*;
import org.app.config.domain.*;
import org.app.entity.*;
import org.app.user.domain.exception.*;
import org.app.user.dto.*;
import org.app.util.api.*;
import org.app.util.exception.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import org.support.*;

@Import(UserTemporalProblemServiceTest.DataInitFacade.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class UserTemporalProblemServiceTest extends IntegrationTestSupport {

    static User testUser;

    @Autowired
    UserTemporalProblemService service;

    @Autowired
    ScenarioInfoSerializer serializer;

    @Autowired
    ScenarioInfoDeserializer deserializer;

    @Autowired
    DataInitFacade data;

    @Autowired
    TestTemporalProblemRepository temporalProblemRepo;

    @BeforeEach
    void setUp() {
        testUser = data.createUser();
    }

    @AfterEach
    void tearDown() {
        data.initAll();
    }

    @Test
    @DisplayName("자신의 임시저장 목록을 확인할 수 있다.")
    void getMyTemporalProblems() {
        Long userId = testUser.getId();
        int numOfTotal = 10;
        List<TemporalProblem> temporalProblems;

        {
            temporalProblems = new ArrayList<>(numOfTotal);
            for (int i = 0; i < numOfTotal; i++) {
                String title = String.format("Temporal problem - %d", i);
                ProblemVisibility visibility = i % 2 == 0 ?
                        ProblemVisibility.PUBLIC : ProblemVisibility.PRIVATE;
                temporalProblems.add(
                        data.createTemporalProblem(
                                userId, title, null, null,
                                visibility, null
                        )
                );
            }
        }

        int pageNo = 0;
        int pageSize = numOfTotal / 2;

        SimplePageResponse<SimpleTemporalProblemInfo> response = service.getMyTemporalProblems(
                userId, pageNo, pageSize
        );

        TestUtils.assertSimplePageResponseEquality(
                response, pageNo, pageSize, pageSize, numOfTotal, true
        );

        List<SimpleTemporalProblemInfo> elements = response.pagedElements();
        assertThat(elements).isNotNull().hasSize(pageSize);

        Map<Long, TemporalProblem> temporalProblemMap = temporalProblems.stream()
                .collect(Collectors.toMap(TemporalProblem::getId, Function.identity()));

        for (SimpleTemporalProblemInfo element : elements) {

            assertThat(element).isNotNull();

            Long entityId = element.temporalProblemId();
            assertThat(entityId).isNotNull();
            assertThat(temporalProblemMap).containsKey(entityId);

            TemporalProblem entity = temporalProblemMap.get(entityId);
            assertThat(entity.getId()).isEqualTo(entityId);
            assertThat(entity.getTitle()).isEqualTo(element.title());
            assertThat(entity.getDescription()).isEqualTo(element.description());
            assertThat(entity.getRewardMessage()).isEqualTo(element.rewardMessage());
            assertThat(entity.getVisibility()).isEqualTo(element.visibility());
            assertThat(entity.getCreatedAt())
                    .isCloseTo(element.createdAt(), within(Duration.ofSeconds(5L)));
            assertThat(entity.getModifiedAt()).isEqualTo(element.modifiedAt())
                    .isNull();
        }
    }

    @Test
    @DisplayName("자신의 임시저장 세부 내용을 확인할 수 있다.")
    void getMyTemporalProblem() throws JsonProcessingException {
        Long userId = testUser.getId();
        TemporalProblem entity;
        Long entityId;

        int numOfTotalScenarios = 10;
        int numOfScenariosToGetReward = 3;
        int numOfScenariosToFailPlay = 2;
        ProblemVisibility visibility = ProblemVisibility.PRIVATE;
        Map<Integer, ScenarioInfo> scenarioInfoOrderMap;

        {
            scenarioInfoOrderMap = new HashMap<>();
            ScenarioInfo[] scenarioInfos = new ScenarioInfo[numOfTotalScenarios];
            for (int i = 0; i < scenarioInfos.length; i++) {
                String sC = String.format("SC - %d", i);
                String aC = String.format("AC - %d", i);
                scenarioInfos[i] = new ScenarioInfo(i, sC, aC);
                scenarioInfoOrderMap.put(i, scenarioInfos[i]);
            }

            String title = "TEST TITLE";
            String serializedInfo = serializer.serialize(scenarioInfos);

            entity = data.createTemporalProblem(
                    userId, title, numOfScenariosToGetReward, numOfScenariosToFailPlay,
                    visibility, serializedInfo
            );
            entityId = entity.getId();
        }

        DetailedTemporalProblemInfo response = service.getMyTemporalProblem(userId, entityId);
        assertThat(response).isNotNull();

        assertThat(response.temporalProblemId()).isEqualTo(entityId);
        assertThat(response.title()).isEqualTo(entity.getTitle());
        assertThat(response.description()).isEqualTo(entity.getDescription());
        assertThat(response.rewardMessage()).isEqualTo(entity.getRewardMessage());
        assertThat(response.visibility()).isEqualTo(entity.getVisibility());
        assertThat(response.numOfScenariosToGetReward()).isEqualTo(numOfScenariosToGetReward);
        assertThat(response.numOfScenariosToFailPlay()).isEqualTo(numOfScenariosToFailPlay);
        assertThat(response.numOfTotalScenarios()).isEqualTo(numOfTotalScenarios);

        ScenarioInfo[] scenarioInfos = response.scenarioInfos();
        assertThat(scenarioInfos).isNotNull().hasSize(numOfTotalScenarios);

        for (ScenarioInfo info : scenarioInfos) {

            assertThat(info).isNotNull();

            int order = info.getScenarioOrder();
            assertThat(scenarioInfoOrderMap).containsKey(order);
            assertThat(info).isEqualTo(scenarioInfoOrderMap.get(order));
        }
    }

    @Test
    @DisplayName("새로운 임시저장 내용을 생성할 수 있다.")
    void createTemporalProblem() throws JsonProcessingException {
        Long userId = testUser.getId();

        String title = "TEST-TITLE";
        String description = "TEST DESCRIPTION";
        String rewardMessage = "TEST REWARD MESSAGE";
        Integer nOfSToGetReward = 3;
        Integer nOfSToFailPlay = null;
        ProblemVisibility visibility = ProblemVisibility.PRIVATE;
        ScenarioInfo[] scenarioInfos = new ScenarioInfo[]{
                new ScenarioInfo(0, "hi", "goodbye"),
                new ScenarioInfo(1, "hello", "goodbye"),
                new ScenarioInfo(2, "wtf?", null)
        };

        //noinspection ConstantValue
        SerializedTemporalProblemInfo savingInfo = new SerializedTemporalProblemInfo(
                title, description, rewardMessage, nOfSToGetReward, nOfSToFailPlay, visibility,
                serializer.serialize(scenarioInfos)
        );

        Long response = service.createTemporalProblem(userId, savingInfo);
        assertThat(response).isNotNull();

        Optional<TemporalProblem> opt = temporalProblemRepo.findById(response);
        assertThat(opt).isNotEmpty();

        TemporalProblem find = opt.get();
        assertThat(find.getId()).isEqualTo(response);
        assertThat(find.getUser().getId()).isEqualTo(userId);
        assertThat(find.getTitle()).isEqualTo(title);
        assertThat(find.getDescription()).isEqualTo(description);
        assertThat(find.getRewardMessage()).isEqualTo(rewardMessage);
        assertThat(find.getNumOfScenariosToGetReward()).isEqualTo(nOfSToGetReward);
        //noinspection ConstantValue
        assertThat(find.getNumOfScenariosToFailPlay()).isEqualTo(nOfSToFailPlay);
        assertThat(find.getVisibility()).isEqualTo(visibility);

        String serializedInfo = find.getSerializedScenarioInfo();
        assertThat(serializedInfo).isNotEmpty();

        ScenarioInfo[] deserializedInfos = deserializer.deserialize(serializedInfo);
        assertThat(deserializedInfos).isNotNull().hasSize(scenarioInfos.length);

        Map<Integer, ScenarioInfo> scenarioInfoOrderMap = Arrays.stream(scenarioInfos)
                .collect(Collectors.toMap(ScenarioInfo::getScenarioOrder, Function.identity()));

        for (ScenarioInfo info : deserializedInfos) {

            assertThat(info).isNotNull();

            int order = info.getScenarioOrder();
            assertThat(scenarioInfoOrderMap).containsKey(order);
            assertThat(info).isEqualTo(scenarioInfoOrderMap.get(order));
        }
    }

    @Test
    @DisplayName("임시저장 내용을 수정할 수 있다.")
    void updateTemporalProblem() throws JsonProcessingException {
        Long userId = testUser.getId();
        Long entityId;

        {
            entityId = data.createTemporalProblem(
                    userId, "ORIGIN", 100, 234,
                    ProblemVisibility.PUBLIC, null
            ).getId();
        }

        String title = "changed";
        String description = "changed desc";
        String rewardMessage = "changed msg";
        Integer nOfSToGetReward = 6;
        Integer nOfSToFailPlay = 3;
        ProblemVisibility visibility = ProblemVisibility.PRIVATE;
        ScenarioInfo[] scenarioInfos = new ScenarioInfo[]{
                new ScenarioInfo(0, "hi", "goodbye"),
                new ScenarioInfo(1, "hello", "goodbye"),
                new ScenarioInfo(2, "wtf?", null)
        };

        SerializedTemporalProblemInfo savingInfo = new SerializedTemporalProblemInfo(
                title, description, rewardMessage, nOfSToGetReward, nOfSToFailPlay, visibility,
                serializer.serialize(scenarioInfos)
        );

        Long response = service.updateTemporalProblem(userId, entityId, savingInfo);
        assertThat(response).isNotNull().isEqualTo(entityId);

        Optional<TemporalProblem> opt = temporalProblemRepo.findById(response);
        assertThat(opt).isNotEmpty();

        TemporalProblem find = opt.get();
        assertThat(find.getId()).isEqualTo(response);
        assertThat(find.getUser().getId()).isEqualTo(userId);
        assertThat(find.getTitle()).isEqualTo(title);
        assertThat(find.getDescription()).isEqualTo(description);
        assertThat(find.getRewardMessage()).isEqualTo(rewardMessage);
        assertThat(find.getNumOfScenariosToGetReward()).isEqualTo(nOfSToGetReward);
        assertThat(find.getNumOfScenariosToFailPlay()).isEqualTo(nOfSToFailPlay);
        assertThat(find.getVisibility()).isEqualTo(visibility);
        assertThat(find.getModifiedAt()).isNotNull();

        String serializedInfo = find.getSerializedScenarioInfo();
        assertThat(serializedInfo).isNotEmpty();

        ScenarioInfo[] deserializedInfos = deserializer.deserialize(serializedInfo);
        assertThat(deserializedInfos).isNotNull().hasSize(scenarioInfos.length);

        Map<Integer, ScenarioInfo> scenarioInfoOrderMap = Arrays.stream(scenarioInfos)
                .collect(Collectors.toMap(ScenarioInfo::getScenarioOrder, Function.identity()));

        for (ScenarioInfo info : deserializedInfos) {

            assertThat(info).isNotNull();

            int order = info.getScenarioOrder();
            assertThat(scenarioInfoOrderMap).containsKey(order);
            assertThat(info).isEqualTo(scenarioInfoOrderMap.get(order));
        }
    }

    @Test
    @DisplayName("임시저장 내용을 삭제할 수 있다.")
    void deleteTemporalProblem() {
        Long userId = testUser.getId();
        Long entityId;

        {
            entityId = data.createTemporalProblem(
                    userId, "title", null, null,
                    ProblemVisibility.PRIVATE, null
            ).getId();
        }

        Long response = service.deleteTemporalProblem(userId, entityId);
        assertThat(response).isNotNull().isEqualTo(entityId);

        Optional<TemporalProblem> opt = temporalProblemRepo.findById(response);
        assertThat(opt).isEmpty();
    }

    @Test
    @DisplayName("관련 자원을 찾을 수 없으면 NotFoundException 이 발생한다.")
    void testNotFoundException() {
        Long existingUserId = testUser.getId();
        Long withdrawnUserId;
        Long notExistingId = Long.MAX_VALUE;
        Long existingTemporalProblemId;

        {
            withdrawnUserId = data.createWithdrawnUser().getId();
            existingTemporalProblemId = data.createTemporalProblem(
                    existingUserId, "title", null, null,
                    null, null
            ).getId();
        }

        // 임시저장 목록, 내용보기
        {
            Function<Long, ?> getMyProblemsFunc
                    = uid -> service.getMyTemporalProblems(uid, 0, 10);
            BiFunction<Long, Long, ?> getMyProblemFunc
                    = (uid, tid) -> service.getMyTemporalProblem(uid, tid);

            TestUtils.assertThrow(      // 사용자 없을 때
                    notExistingId, getMyProblemsFunc,
                    UserNotFoundException.class
            );
            TestUtils.assertThrow(      // 사용자 탈퇴했을 때
                    withdrawnUserId, getMyProblemsFunc,
                    UserNotFoundException.class
            );

            TestUtils.assertThrow(      // 사용자 없을 때
                    notExistingId, existingTemporalProblemId,
                    getMyProblemFunc, UserNotFoundException.class
            );
            TestUtils.assertThrow(      // 사용자 탈퇴했을 때
                    withdrawnUserId, existingTemporalProblemId,
                    getMyProblemFunc, UserNotFoundException.class
            );
            TestUtils.assertThrow(      // 관련 내용 없을 때
                    existingUserId, notExistingId,
                    getMyProblemFunc, TemporalProblemNotFoundException.class
            );
        }

        // 임시저장 생성, 수정, 삭제
        {
            //noinspection DataFlowIssue
            Function<Long, ?> createProblemFunc
                    = uid -> service.createTemporalProblem(uid, null);
            BiFunction<Long, Long, ?> updateProblemFunc
                    = (uid, tid) -> service.updateTemporalProblem(uid, tid, null);
            BiFunction<Long, Long, ?> deleteProblemFunc
                    = (uid, tid) -> service.deleteTemporalProblem(uid, tid);

            TestUtils.assertThrow(      // 사용자 없을 때
                    notExistingId, createProblemFunc,
                    UserNotFoundException.class
            );
            TestUtils.assertThrow(      // 사용자 탈퇴했을 때
                    withdrawnUserId, createProblemFunc,
                    UserNotFoundException.class
            );

            TestUtils.assertThrow(      // 사용자 없을 때
                    notExistingId, existingTemporalProblemId,
                    updateProblemFunc, UserNotFoundException.class
            );
            TestUtils.assertThrow(      // 사용자 탈퇴했을 때
                    withdrawnUserId, existingTemporalProblemId,
                    updateProblemFunc, UserNotFoundException.class
            );
            TestUtils.assertThrow(      // 관련 내용 없을 때
                    existingUserId, notExistingId,
                    updateProblemFunc, TemporalProblemNotFoundException.class
            );

            TestUtils.assertThrow(      // 사용자 없을 때
                    notExistingId, existingTemporalProblemId,
                    deleteProblemFunc, UserNotFoundException.class
            );
            TestUtils.assertThrow(      // 사용자 탈퇴했을 때
                    withdrawnUserId, existingTemporalProblemId,
                    deleteProblemFunc, UserNotFoundException.class
            );
            TestUtils.assertThrow(      // 관련 내용 없을 때
                    existingUserId, notExistingId,
                    deleteProblemFunc, TemporalProblemNotFoundException.class
            );
        }
    }

    @Test
    @DisplayName("사용자는 오직 자신의 자원만 조회, 관리할 수 있다.")
    void testForbiddenException() {
        Long userId = testUser.getId();
        Long anotherUserOwnedTemporalProblemId;

        {
            Long anotherUserId = data.createUser().getId();
            anotherUserOwnedTemporalProblemId = data.createTemporalProblem(
                    anotherUserId, "title", null, null,
                    null, null
            ).getId();
        }

        assertThatThrownBy(() -> service.getMyTemporalProblem(
                userId, anotherUserOwnedTemporalProblemId
        ))
                .isInstanceOf(ForbiddenException.class);

        assertThatThrownBy(() -> service.updateTemporalProblem(
                userId, anotherUserOwnedTemporalProblemId, null
        ))
                .isInstanceOf(ForbiddenException.class);

        assertThatThrownBy(() -> service.deleteTemporalProblem(
                userId, anotherUserOwnedTemporalProblemId
        ))
                .isInstanceOf(ForbiddenException.class);
    }

    @Component
    @Transactional
    protected static class DataInitFacade {

        @Autowired
        GeneralDataInitializer initializer;

        User createUser() {
            return initializer.userBuilder()
                    .name("test")
                    .build();
        }

        User createWithdrawnUser() {
            return initializer.userBuilder()
                    .name("withdrawn test user")
                    .withdrawn(true)
                    .withdrawnAt(LocalDateTime.now())
                    .build();
        }

        TemporalProblem createTemporalProblem(
                Long userId, String title,
                Integer nOfSToGetReward, Integer nOfSToFailPlay,
                ProblemVisibility visibility, String serializedScenarioInfo
        ) {
            String description = String.format("DESC - %s", title);
            String rewardMsg = String.format("MSG - %s", title);

            return initializer.temporalProblemBuilder()
                    .userId(userId)
                    .title(title)
                    .description(description)
                    .rewardMessage(rewardMsg)
                    .numOfScenariosToGetReward(nOfSToGetReward)
                    .numOfScenariosToFailPlay(nOfSToFailPlay)
                    .visibility(visibility)
                    .serializedScenarioInfo(serializedScenarioInfo)
                    .build();
        }

        void initAll() {
            initializer.initAll();
        }
    }
}