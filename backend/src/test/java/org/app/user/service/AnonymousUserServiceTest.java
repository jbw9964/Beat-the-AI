package org.app.user.service;

import static org.assertj.core.api.Assertions.*;

import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import lombok.extern.slf4j.*;
import org.*;
import org.app.entity.*;
import org.app.user.domain.exception.*;
import org.app.user.dto.*;
import org.app.user.dto.response.*;
import org.app.user.repository.*;
import org.app.util.*;
import org.app.util.api.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

@Slf4j
@Import(AnonymousUserServiceTest.DataInitializer.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class AnonymousUserServiceTest extends IntegrationTestSupport {

    static final Random RANDOM = new Random();
    static User testUser;
    @Autowired
    AnonymousUserService anonymousUserService;

    @Autowired
    DataInitializer initializer;

    @Autowired
    DateTimeProvider dateTimeProvider;

    private static Stream<Arguments> nonPublicPlayRecordVisibility() {
        return Stream.of(
                Arrays.stream(PlayRecordVisibility.values())
                        .filter(prv -> !prv.equals(PlayRecordVisibility.PUBLIC))
                        .map(Arguments::of)
                        .toArray(Arguments[]::new)
        );
    }

    @BeforeEach
    void setUp() {
        testUser = initializer.createNewUser(
                "test", "testEMAIL", "testTHUMBNAIL"
        );
    }

    @AfterEach
    void tearDown() {
        initializer.initAll();
    }

    @Test
    @DisplayName("임의의 사용자 정보를 조회할 수 있다.")
    void getUser() {
        GetUserResponse resp = anonymousUserService.getUser(testUser.getId(), null);

        assertThat(resp).isNotNull();
        assertThat(resp.userId()).isEqualTo(testUser.getId());
        assertThat(resp.username()).isEqualTo(testUser.getName());
        assertThat(resp.email()).isEqualTo(testUser.getEmail());
        assertThat(resp.thumbnailUrl()).isEqualTo(testUser.getThumbnailUrl());
        assertThat(resp.isMine()).isFalse();
    }

    @Test
    @DisplayName("Public 한 사용자 플레이 기록을 조회할 수 있다.")
    void getPublicRecords() {
        Long userId = testUser.getId();

        List<PlayRecord> publicRecords = new ArrayList<>();
        List<PlayRecord> privateRecords = new ArrayList<>();
        {
            int i = 0;
            for (; i <= 9; i++) {
                int mod = i % 3;
                PlayRecordStatus status =
                        mod == 0 ? PlayRecordStatus.PLAYING :
                                mod == 1 ? PlayRecordStatus.CLEARED : PlayRecordStatus.FAILED;
                publicRecords.add(genPR(userId, (long) i, status, PlayRecordVisibility.PUBLIC));
            }

            for (; i <= 18; i++) {
                int mod = i % 3;
                PlayRecordStatus status =
                        mod == 0 ? PlayRecordStatus.PLAYING :
                                mod == 1 ? PlayRecordStatus.CLEARED : PlayRecordStatus.FAILED;
                PlayRecordVisibility visibility =
                        mod == 0 ? PlayRecordVisibility.PRIVATE : PlayRecordVisibility.CREATOR_ONLY;
                privateRecords.add(genPR(userId, (long) i, status, visibility));
            }
        }

        int pageNo = 0;
        int pageSize = publicRecords.size() / 2;

        GetPublicRecordsResponse response = anonymousUserService.getPublicRecords(
                userId, pageNo, pageSize, null
        );

        assertThat(response).isNotNull();
        assertThat(response.isMine()).isFalse();

        SimplePageResponse<SimplePlayRecordInfo> pageResponse = response.pageResponse();
        assertThat(pageResponse).isNotNull();
        assertThat(pageResponse.pageNoRequest()).isEqualTo(pageNo);
        assertThat(pageResponse.pageSizeRequest()).isEqualTo(pageSize);
        assertThat(pageResponse.numOfPagedElements()).isEqualTo(pageSize);
        assertThat(pageResponse.numOfTotalElements()).isEqualTo(publicRecords.size());
        assertThat(pageResponse.hasNext()).isTrue();

        List<SimplePlayRecordInfo> pagedElements = pageResponse.pagedElements();
        assertThat(pagedElements).isNotNull().hasSize(pageSize);

        Map<Long, PlayRecord> publicRecordMap = publicRecords.stream()
                .collect(Collectors.toMap(PlayRecord::getId, Function.identity()));
        Map<Long, PlayRecord> privateRecordMap = privateRecords.stream()
                .collect(Collectors.toMap(PlayRecord::getId, Function.identity()));

        for (SimplePlayRecordInfo element : pagedElements) {

            assertThat(element).isNotNull();

            Long playRecordId = element.playRecordId();
            assertThat(publicRecordMap).containsKey(playRecordId);
            assertThat(privateRecordMap).doesNotContainKey(playRecordId);

            PlayRecord publicRecord = publicRecordMap.get(playRecordId);
            assertThat(publicRecord.getId()).isEqualTo(element.playRecordId());
            assertThat(publicRecord.getProblemId()).isEqualTo(element.problemId());
            assertThat(publicRecord.getTitle()).isEqualTo(element.title());
            assertThat(publicRecord.getDescription()).isEqualTo(element.description());
            assertThat(publicRecord.getStatus()).isEqualTo(element.status());
            assertThat(publicRecord.getVisibility()).isEqualTo(element.visibility());
            assertThat(publicRecord.getCreatedAt()).isCloseTo(
                    element.createdAt(), within(Duration.ofSeconds(5L))
            );
        }
    }

    @Test
    @DisplayName("Public 한 사용자 플레이 내용을 볼 수 있다.")
    void getPublicRecord() {
        Long userId = testUser.getId();
        PlayRecord playRecord = initializer.createNewPlayRecord(
                userId, 55L,
                "testTITLE", "testDESCRIPTION", "testREWARD",
                5, 5,
                PlayRecordStatus.PLAYING, PlayRecordVisibility.PUBLIC
        );

        Long playRecordId = playRecord.getId();
        List<ScenarioRecord> submittedScenarios = new ArrayList<>();
        List<ScenarioRecord> unsubmittedScenarios = new ArrayList<>();
        {
            int i = 0;
            for (; i <= 5; i++) {
                boolean hasPassed = i % 2 == 0;
                submittedScenarios.add(genSubmittedSR(playRecordId, i, hasPassed));
            }
            for (; i <= 10; i++) {
                unsubmittedScenarios.add(genUnsubmittedSR(playRecordId, i));
            }
        }

        GetPublicRecordResponse response = anonymousUserService.getPublicRecord(
                userId, playRecordId, null
        );

        assertThat(response).isNotNull();
        assertThat(response.isMine()).isFalse();

        int nOfTotal = submittedScenarios.size() + unsubmittedScenarios.size();
        int nOfSubmitted = submittedScenarios.size();
        int nOfPassed = (int) submittedScenarios.stream()
                .filter(ScenarioRecord::hasPassed)
                .count();
        int nOfSToGetReward = playRecord.getNumOfScenariosToGetReward();
        int nOfSToFailPlay = playRecord.getNumOfScenariosToFailPlay();

        DetailedPlayRecordInfo detailedPRInfo = response.detailedPlayRecordInfo();
        assertThat(detailedPRInfo).isNotNull();
        assertThat(detailedPRInfo.playRecordId()).isEqualTo(playRecordId);
        assertThat(detailedPRInfo.problemId()).isEqualTo(playRecord.getProblemId());
        assertThat(detailedPRInfo.title()).isEqualTo(playRecord.getTitle());
        assertThat(detailedPRInfo.description()).isEqualTo(playRecord.getDescription());
        assertThat(detailedPRInfo.status()).isEqualTo(playRecord.getStatus());
        assertThat(detailedPRInfo.visibility()).isEqualTo(playRecord.getVisibility());

        assertThat(detailedPRInfo.numOfTotalScenarios()).isEqualTo(nOfTotal);
        assertThat(detailedPRInfo.numOfSubmittedScenarios()).isEqualTo(nOfSubmitted);
        assertThat(detailedPRInfo.numOfPassedScenarios()).isEqualTo(nOfPassed);
        assertThat(detailedPRInfo.numOfScenariosToGetReward()).isEqualTo(nOfSToGetReward);
        assertThat(detailedPRInfo.numOfScenariosToFailPlay()).isEqualTo(nOfSToFailPlay);

        int nOfPagedElements = submittedScenarios.size();

        SimplePageResponse<ScenarioRecordInfo> pagedSRInfo = response.scenarioPageResponse();
        assertThat(pagedSRInfo).isNotNull();
        assertThat(pagedSRInfo.numOfPagedElements()).isEqualTo(nOfPagedElements);

        List<ScenarioRecordInfo> pagedElements = pagedSRInfo.pagedElements();
        assertThat(pagedElements).isNotNull().hasSize(nOfPagedElements)
                .isSortedAccordingTo(Comparator.comparing(ScenarioRecordInfo::scenarioOrder));

        Map<Long, ScenarioRecord> submittedSRMap = submittedScenarios.stream()
                .collect(Collectors.toMap(ScenarioRecord::getId, Function.identity()));
        Map<Long, ScenarioRecord> unsubmittedSRMap = unsubmittedScenarios.stream()
                .collect(Collectors.toMap(ScenarioRecord::getId, Function.identity()));

        for (ScenarioRecordInfo element : pagedElements) {

            assertThat(element).isNotNull();

            Long scenarioRecordId = element.scenarioRecordId();
            assertThat(submittedSRMap).containsKey(scenarioRecordId);
            assertThat(unsubmittedSRMap).doesNotContainKey(scenarioRecordId);

            ScenarioRecord scenarioRecord = submittedSRMap.get(scenarioRecordId);
            assertThat(scenarioRecord.getId()).isEqualTo(element.scenarioRecordId());
            assertThat(scenarioRecord.getScenarioOrder()).isEqualTo(element.scenarioOrder());
            assertThat(scenarioRecord.getScenarioContent()).isEqualTo(element.scenarioContent());
            assertThat(scenarioRecord.getUserSubmissionContent()).isEqualTo(
                    element.userSubmissionContent()
            );
            assertThat(scenarioRecord.getAiGeneratedContent()).isEqualTo(
                    element.aiGeneratedContent()
            );
            assertThat(scenarioRecord.hasSubmitted()).isEqualTo(element.hasSubmitted()).isTrue();
            assertThat(scenarioRecord.hasPassed()).isEqualTo(element.hasPassed());
            assertThat(scenarioRecord.getSubmittedAt()).isCloseTo(
                    element.submittedAt(), within(Duration.ofSeconds(5L))
            );
        }

    }

    @Test
    @DisplayName("사용자를 찾을 수 없으면 NotFoundException 이 발생한다.")
    void testUserNotFound() {
        Long notExistingUserId = Long.MAX_VALUE;

        assertThatThrownBy(() -> anonymousUserService.getUser(notExistingUserId, null))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> anonymousUserService.getPublicRecords(
                notExistingUserId, 0, 5, null
        ))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> anonymousUserService.getPublicRecord(
                notExistingUserId, 5L, null
        ))
                .isInstanceOf(UserNotFoundException.class);
    }

    @ParameterizedTest
    @MethodSource("nonPublicPlayRecordVisibility")
    @DisplayName("Public 하지 않은 플레이 내용을 조회하면 NotFoundException 이 발생한다.")
    void testPublicPlayRecordNotFound(PlayRecordVisibility visibility) {
        Long userId = testUser.getId();
        PlayRecord nonPublicPR = genPR(
                userId, 5L, PlayRecordStatus.PLAYING, visibility
        );
        Long playRecordId = nonPublicPR.getId();

        assertThatThrownBy(() -> anonymousUserService.getPublicRecord(
                userId, playRecordId, null
        ))
                .isInstanceOf(PublicPlayRecordNotFoundException.class);
    }

    private PlayRecord genPR(
            Long userId, Long problemId,
            PlayRecordStatus status, PlayRecordVisibility visibility
    ) {
        String tempStr = String.valueOf(problemId);
        int tempInt = RANDOM.nextInt(1, 15);
        return initializer.createNewPlayRecord(
                userId, problemId, tempStr, tempStr, tempStr,
                tempInt, tempInt, status, visibility
        );
    }

    private ScenarioRecord genUnsubmittedSR(
            Long playRecordId, int scenarioOrder
    ) {
        String scenarioContent = String.format(
                "Scenario %s-%s", playRecordId, scenarioOrder
        );
        return initializer.createNewUnSubmittedScenarioRecord(
                playRecordId, scenarioOrder, scenarioContent
        );
    }

    private ScenarioRecord genSubmittedSR(
            Long playRecordId, int scenarioOrder, boolean hasPassed
    ) {
        String scenarioContent = String.format(
                "Scenario %s-%s", playRecordId, scenarioOrder
        );
        String userSubmissionContent = String.format(
                "User submission %s-%s", playRecordId, scenarioOrder
        );
        String aiGeneratedContent = String.format(
                "AI generated %s-%s", playRecordId, scenarioOrder
        );
        LocalDateTime submittedAt = dateTimeProvider.localDateTimeNow();

        return initializer.createNewSubmittedScenarioRecord(
                playRecordId, scenarioOrder,
                scenarioContent, userSubmissionContent, aiGeneratedContent,
                hasPassed, submittedAt
        );
    }

    @Component
    @SuppressWarnings("SameParameterValue")
    protected static class DataInitializer {

        @Autowired
        UserRepository userRepo;

        @Autowired
        UserPlayRecordRepository userPlayRecordRepo;

        @Autowired
        UserScenarioRecordRepository userScenarioRecordRepo;

        @Transactional
        User createNewUser(String name, String email, String thumbnailUrl) {
            User user = new User(name);
            user.changeEmail(email);
            user.changeThumbnailUrl(thumbnailUrl);
            return userRepo.save(user);
        }

        @Transactional
        PlayRecord createNewPlayRecord(
                Long userId, Long problemId, String title, String description,
                String rewardMessage, int numOfScenariosToGetReward, int numOfScenariosToFailPlay,
                PlayRecordStatus status, PlayRecordVisibility visibility
        ) {
            User find = userRepo.findById(userId).orElseThrow(AssertionError::new);
            PlayRecord playRecord = new PlayRecord(
                    find, problemId, title, description,
                    rewardMessage, numOfScenariosToGetReward, numOfScenariosToFailPlay,
                    status, visibility
            );

            return userPlayRecordRepo.save(playRecord);
        }

        @Transactional
        ScenarioRecord createNewUnSubmittedScenarioRecord(
                Long playRecordId, int scenarioOrder, String scenarioContent
        ) {
            PlayRecord find = userPlayRecordRepo.findById(playRecordId)
                    .orElseThrow(AssertionError::new);
            ScenarioRecord scenarioRecord
                    = new ScenarioRecord(find, scenarioOrder, scenarioContent);
            return userScenarioRecordRepo.save(scenarioRecord);
        }

        @Transactional
        ScenarioRecord createNewSubmittedScenarioRecord(
                Long playRecordId, int scenarioOrder, String scenarioContent,
                String userSubmissionContent, String aiGeneratedContent,
                boolean hasPassed, LocalDateTime submittedAt
        ) {
            ScenarioRecord scenarioRecord = this.createNewUnSubmittedScenarioRecord(
                    playRecordId, scenarioOrder, scenarioContent
            );

            scenarioRecord.updateSubmission(
                    userSubmissionContent, aiGeneratedContent, hasPassed, submittedAt
            );

            return scenarioRecord;
        }

        @Transactional
        void initAll() {
            userScenarioRecordRepo.deleteAll();
            userPlayRecordRepo.deleteAll();
            userRepo.deleteAll();
        }
    }
}