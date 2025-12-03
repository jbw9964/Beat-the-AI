package org.app.user.service;

import static org.assertj.core.api.Assertions.*;

import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.*;
import org.app.entity.*;
import org.app.user.domain.exception.*;
import org.app.user.dto.*;
import org.app.user.dto.response.*;
import org.app.util.api.*;
import org.app.util.exception.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import org.support.*;

@Import(UserRecordServiceTest.DataInitFacade.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class UserRecordServiceTest extends IntegrationTestSupport {

    static User testUser;

    @Autowired
    UserRecordService userRecordService;

    @Autowired
    DataInitFacade data;

    @Autowired
    TestPlayRecordRepository playRecordRepo;

    @Autowired
    TestGainedRewardRepository gainedRewardRepo;

    @BeforeEach
    void setUp() {
        testUser = data.createUser();
    }

    @AfterEach
    void tearDown() {
        data.initAll();
    }

    @Test
    @DisplayName("자신의 플레이 기록을 조회할 수 있다.")
    void getMyRecords() {
        Long userId = testUser.getId();
        int numOfTotal = 10;
        List<PlayRecord> playRecords;

        {
            playRecords = new ArrayList<>(numOfTotal);

            for (int i = 0; i < numOfTotal; i++) {
                Long problemId = (long) i;

                PlayRecordStatus status;
                PlayRecordVisibility visibility;
                {
                    int len = PlayRecordStatus.values().length;
                    status = PlayRecordStatus.values()[i % len];
                    len = PlayRecordVisibility.values().length;
                    visibility = PlayRecordVisibility.values()[i % len];
                }
                playRecords.add(data.createPlayRecord(
                        userId, problemId, status, visibility
                ));
            }
        }

        int pageNo = 0;
        int pageSize = numOfTotal / 2;

        SimplePageResponse<SimplePlayRecordInfo> response = userRecordService.getMyRecords(
                userId, pageNo, pageSize
        );

        TestUtils.assertSimplePageResponseEquality(
                response, pageNo, pageSize, pageSize, numOfTotal, true
        );

        List<SimplePlayRecordInfo> elements = response.pagedElements();

        Map<Long, PlayRecord> playRecordMap = playRecords.stream()
                .collect(Collectors.toMap(PlayRecord::getId, Function.identity()));

        for (SimplePlayRecordInfo element : elements) {

            assertThat(element).isNotNull();

            Long entityId = element.playRecordId();
            assertThat(entityId).isNotNull();
            assertThat(playRecordMap).containsKey(entityId);

            PlayRecord entity = playRecordMap.get(entityId);
            assertThat(entity.getId()).isEqualTo(element.playRecordId());
            assertThat(entity.getProblemId()).isEqualTo(element.problemId());
            assertThat(entity.getTitle()).isEqualTo(element.title());
            assertThat(entity.getDescription()).isEqualTo(element.description());
            assertThat(entity.getStatus()).isEqualTo(element.status());
            assertThat(entity.getVisibility()).isEqualTo(element.visibility());
            assertThat(entity.getCreatedAt())
                    .isCloseTo(element.createdAt(), within(Duration.ofSeconds(5L)));
        }
    }

    @Test
    @DisplayName("자신의 플레이 기록 세부 내용을 조회할 수 있다.")
    void getMyRecord() {
        Long userId = testUser.getId();
        PlayRecord playRecord;
        Long playRecordId;

        int numOfEach = 5;
        List<ScenarioRecord> submitted, unsubmitted;

        {
            submitted = new ArrayList<>(numOfEach);
            unsubmitted = new ArrayList<>(numOfEach);
            playRecord = data.createPlayRecord(
                    userId, 5L, PlayRecordStatus.PLAYING, PlayRecordVisibility.PRIVATE
            );
            playRecordId = playRecord.getId();

            int i = 0;
            LocalDateTime now = LocalDateTime.now();

            for (; i < numOfEach; i++) {
                boolean hasPassed = i % 2 == 0;
                LocalDateTime submittedAt = now.plusHours(i);

                submitted.add(data.createSubmittedScenarioRecord(
                        playRecordId, i, submittedAt, hasPassed
                ));
            }

            for (; i < 2 * numOfEach; i++) {
                unsubmitted.add(data.createUnsubmittedScenarioRecord(
                        playRecordId, i
                ));
            }
        }

        GetMyRecordResponse response = userRecordService.getMyRecord(userId, playRecordId);

        assertThat(response).isNotNull();

        int numOfTotal = submitted.size() + unsubmitted.size();
        int numOfSubmitted = submitted.size();
        int numOfPassed = (int) submitted.stream()
                .filter(ScenarioRecord::hasPassed)
                .count();
        int numOfSToGetReward = playRecord.getNumOfScenariosToGetReward();
        int numOfSToFailPlay = playRecord.getNumOfScenariosToFailPlay();

        DetailedPlayRecordInfo playRecordInfo = response.detailedPlayRecordInfo();

        assertThat(playRecordInfo).isNotNull();
        assertThat(playRecordInfo.playRecordId()).isEqualTo(playRecordId);
        assertThat(playRecordInfo.problemId()).isEqualTo(playRecord.getProblemId());
        assertThat(playRecordInfo.title()).isEqualTo(playRecord.getTitle());
        assertThat(playRecordInfo.description()).isEqualTo(playRecord.getDescription());
        assertThat(playRecordInfo.status()).isEqualTo(playRecord.getStatus());
        assertThat(playRecordInfo.visibility()).isEqualTo(playRecord.getVisibility());
        assertThat(playRecordInfo.createdAt())
                .isCloseTo(playRecordInfo.createdAt(), within(Duration.ofSeconds(5L)));

        assertThat(playRecordInfo.numOfTotalScenarios()).isEqualTo(numOfTotal);
        assertThat(playRecordInfo.numOfSubmittedScenarios()).isEqualTo(numOfSubmitted);
        assertThat(playRecordInfo.numOfPassedScenarios()).isEqualTo(numOfPassed);
        assertThat(playRecordInfo.numOfScenariosToGetReward()).isEqualTo(numOfSToGetReward);
        assertThat(playRecordInfo.numOfScenariosToFailPlay()).isEqualTo(numOfSToFailPlay);

        List<ScenarioRecordInfo> scenarioRecordInfos = response.scenarioRecordInfos();
        assertThat(scenarioRecordInfos).isNotNull().hasSize(numOfSubmitted)
                .isSortedAccordingTo(Comparator.comparing(ScenarioRecordInfo::scenarioOrder));

        Map<Long, ScenarioRecord> submittedMap = submitted.stream()
                .collect(Collectors.toMap(ScenarioRecord::getId, Function.identity()));
        Map<Long, ScenarioRecord> unsubmittedMap = unsubmitted.stream()
                .collect(Collectors.toMap(ScenarioRecord::getId, Function.identity()));

        for (ScenarioRecordInfo info : scenarioRecordInfos) {

            assertThat(info).isNotNull();

            Long entityId = info.scenarioRecordId();
            assertThat(entityId).isNotNull();
            assertThat(submittedMap).containsKey(entityId);
            assertThat(unsubmittedMap).doesNotContainKey(entityId);

            ScenarioRecord entity = submittedMap.get(entityId);
            assertThat(entity.getId()).isEqualTo(entityId);
            assertThat(entity.getScenarioOrder()).isEqualTo(info.scenarioOrder());
            assertThat(entity.getScenarioContent()).isEqualTo(info.scenarioContent());
            assertThat(entity.getUserSubmissionContent())
                    .isEqualTo(info.userSubmissionContent());
            assertThat(entity.getAiGeneratedContent())
                    .isEqualTo(info.aiGeneratedContent());
            assertThat(entity.hasSubmitted())
                    .isEqualTo(info.hasSubmitted()).isTrue();
            assertThat(entity.hasPassed()).isEqualTo(info.hasPassed());
            assertThat(entity.getSubmittedAt())
                    .isCloseTo(info.submittedAt(), within(Duration.ofSeconds(5L)));
        }
    }

    @Test
    @DisplayName("기록 공개 여부를 변경할 수 있다.")
    void changeMyRecordVisibility() {
        Long userId = testUser.getId();
        Long problemId = 5L;

        PlayRecordVisibility[] visibilities
                = PlayRecordVisibility.values();

        for (PlayRecordVisibility origin : visibilities) {
            for (PlayRecordVisibility change : visibilities) {

                PlayRecord playRecord = data.createPlayRecord(
                        userId, problemId, PlayRecordStatus.PLAYING, origin
                );
                Long playRecordId = playRecord.getId();

                Long response = userRecordService.changeMyRecordVisibility(
                        userId, playRecordId, change
                );

                assertThat(response).isNotNull().isEqualTo(playRecordId);

                PlayRecord find = playRecordRepo.findById(playRecordId)
                        .orElseThrow(AssertionError::new);

                assertThat(find.getVisibility()).isEqualTo(change);

                LocalDateTime modifiedAt = find.getModifiedAt();
                if (origin.equals(change)) {
                    assertThat(modifiedAt).isNull();
                } else {
                    assertThat(modifiedAt).isNotNull();
                }
            }
        }
    }

    @Test
    @DisplayName("플레이 기록과 연관된 보상을 조회할 수 있다.")
    void getMyRewards() {
        Long userId = testUser.getId();
        Long playRecordId;

        int numOfTotal = 10;
        List<GainedReward> gainedRewards;

        {
            PlayRecord playRecord = data.createPlayRecord(
                    userId, 50L,
                    PlayRecordStatus.CLEARED, PlayRecordVisibility.PRIVATE
            );
            playRecordId = playRecord.getId();

            gainedRewards = new ArrayList<>(numOfTotal);
            RewardStorageType storageType = RewardStorageType.LOCAL_STORAGE;

            for (int i = 0; i < numOfTotal; i++) {
                gainedRewards.add(data.createGainedReward(
                        userId, playRecordId, (long) i, storageType
                ));
            }
        }

        int pageNo = 0;
        int pageSize = numOfTotal / 2;

        SimplePageResponse<GainedRewardInfo> response = userRecordService.getMyRewards(
                userId, playRecordId, pageNo, pageSize
        );

        TestUtils.assertSimplePageResponseEquality(
                response, pageNo, pageSize, pageSize, numOfTotal, true
        );

        List<GainedRewardInfo> elements = response.pagedElements();

        Map<Long, GainedReward> gainedRewardMap = gainedRewards.stream()
                .collect(Collectors.toMap(GainedReward::getId, Function.identity()));

        for (GainedRewardInfo element : elements) {

            assertThat(element).isNotNull();

            Long entityId = element.gainedRewardId();
            assertThat(entityId).isNotNull();
            assertThat(gainedRewardMap).containsKey(entityId);

            GainedReward entity = gainedRewardMap.get(entityId);
            assertThat(entity.getId()).isEqualTo(element.gainedRewardId());
            assertThat(entity.getPlayRecord().getId()).isEqualTo(element.playRecordId());
            assertThat(entity.getDescription()).isEqualTo(element.description());
            assertThat(entity.getCreatedAt())
                    .isCloseTo(element.createdAt(), within(Duration.ofSeconds(5L)));
        }
    }

    @Test
    @DisplayName("플레이 기록과 연관된 보상을 내용을 조회할 수 있다.")
    void getMyReward() {
        Long userId = testUser.getId();
        Long playRecordId;
        GainedReward entity;
        Long gainedRewardId;

        {
            playRecordId = data.createPlayRecord(
                    userId, 35L, PlayRecordStatus.CLEARED, PlayRecordVisibility.PRIVATE
            ).getId();
            entity = data.createGainedReward(
                    userId, playRecordId, 355L, RewardStorageType.LOCAL_STORAGE
            );
            gainedRewardId = entity.getId();
        }

        GainedRewardInfo response = userRecordService.getMyReward(
                userId, playRecordId, gainedRewardId
        );

        assertThat(response).isNotNull();
        assertThat(response.gainedRewardId()).isEqualTo(entity.getId());
        assertThat(response.playRecordId()).isEqualTo(entity.getPlayRecord().getId());
        assertThat(response.description()).isEqualTo(entity.getDescription());
        assertThat(response.createdAt())
                .isCloseTo(entity.getCreatedAt(), within(Duration.ofSeconds(5L)));
    }

    @Test
    @DisplayName("플레이 기록과 연관된 모든 보상을 삭제할 수 있다.")
    void deleteMyRewards() {
        Long userId = testUser.getId();
        Long playRecordId;

        int numOfTotal = 10;
        List<GainedReward> entities;

        {
            playRecordId = data.createPlayRecord(
                    userId, 12L,
                    PlayRecordStatus.PLAYING, PlayRecordVisibility.PRIVATE
            ).getId();

            entities = new ArrayList<>(numOfTotal);
            for (int i = 0; i < numOfTotal; i++) {
                entities.add(data.createGainedReward(
                        userId, playRecordId, (long) i, RewardStorageType.LOCAL_STORAGE
                ));
            }
        }

        DeleteMyRewardResponse response = userRecordService.deleteMyRewards(userId, playRecordId);

        assertThat(response).isNotNull();

        List<Long> deletedRewardIds = response.deletedRewardIds();
        assertThat(deletedRewardIds).isNotNull().hasSize(numOfTotal);

        Set<Long> entityIds = entities.stream()
                .map(GainedReward::getId)
                .collect(Collectors.toSet());
        assertThat(entityIds).containsAll(deletedRewardIds);

        for (Long entityId : entityIds) {
            assertThat(gainedRewardRepo.findById(entityId)).isEmpty();
        }
    }

    @Test
    @DisplayName("관련 자원을 찾을 수 없으면 NotFoundException 이 발생한다.")
    void testNotFoundException() {
        Long existingUserId = testUser.getId();
        Long withdrawnUserId = data.createWithdrawnUser().getId();
        Long notExistingId = Long.MAX_VALUE;
        Long existingPlayRecordId;
        Long existingGainedRewardId;

        {
            existingPlayRecordId = data.createPlayRecord(
                    existingUserId, 99L,
                    PlayRecordStatus.CLEARED, PlayRecordVisibility.PRIVATE
            ).getId();
            existingGainedRewardId = data.createGainedReward(
                    existingUserId, existingPlayRecordId,
                    33L, RewardStorageType.LOCAL_STORAGE
            ).getId();
        }

        // 플레이 목록, 내용 보기
        {
            Function<Long, ?> getMyRecordsFunc
                    = (uid) -> userRecordService.getMyRecords(uid, 0, 10);
            BiFunction<Long, Long, ?> getMyRecordFunc
                    = (uid, pid) -> userRecordService.getMyRecord(uid, pid);

            TestUtils.assertThrow(    // 사용자 없을 때
                    notExistingId, getMyRecordsFunc,
                    UserNotFoundException.class
            );
            TestUtils.assertThrow(    // 사용자 탈퇴했을 때
                    withdrawnUserId, getMyRecordsFunc,
                    UserNotFoundException.class
            );

            TestUtils.assertThrow(    // 사용자 없을 때
                    notExistingId, existingPlayRecordId,
                    getMyRecordFunc, UserNotFoundException.class
            );
            TestUtils.assertThrow(    // 사용자 탈퇴했을 때
                    withdrawnUserId, existingPlayRecordId,
                    getMyRecordFunc, UserNotFoundException.class
            );
            TestUtils.assertThrow(    // 관련 기록 없을 때
                    existingUserId, notExistingId,
                    getMyRecordFunc, PlayRecordNotFoundException.class
            );
        }

        // 플레이 공개 속성 바꾸기
        {
            BiFunction<Long, Long, ?> changeMyRecordVisibilityFunc
                    = (uid, pid) -> userRecordService.changeMyRecordVisibility(
                    uid, pid, PlayRecordVisibility.PRIVATE
            );

            TestUtils.assertThrow(    // 사용자 없을 때
                    notExistingId, existingPlayRecordId,
                    changeMyRecordVisibilityFunc, UserNotFoundException.class
            );
            TestUtils.assertThrow(    // 사용자 탈퇴했을 때
                    withdrawnUserId, existingPlayRecordId,
                    changeMyRecordVisibilityFunc, UserNotFoundException.class
            );
            TestUtils.assertThrow(    // 관련 기록 없을 때
                    existingUserId, notExistingId,
                    changeMyRecordVisibilityFunc, PlayRecordNotFoundException.class
            );
        }

        // 보상 목록, 내용 보기
        {
            BiFunction<Long, Long, ?> getMyRewardsFunc
                    = (uid, pid) -> userRecordService.getMyRewards(
                    uid, pid, 0, 10
            );
            TestUtils.Triplet<Long, Long, Long, ?> getMyRewardFunc
                    = (uid, pid, gid) -> userRecordService.getMyReward(
                    uid, pid, gid
            );

            TestUtils.assertThrow(    // 사용자 없을 때
                    notExistingId, existingPlayRecordId,
                    getMyRewardsFunc, UserNotFoundException.class
            );
            TestUtils.assertThrow(    // 탈퇴했을 때
                    withdrawnUserId, existingPlayRecordId,
                    getMyRewardsFunc, UserNotFoundException.class
            );
            TestUtils.assertThrow(    // 기록 없을 때
                    existingUserId, notExistingId,
                    getMyRewardsFunc, PlayRecordNotFoundException.class
            );

            TestUtils.assertThrow(    // 사용자 없을때
                    notExistingId, existingPlayRecordId, existingGainedRewardId,
                    getMyRewardFunc, UserNotFoundException.class
            );
            TestUtils.assertThrow(    // 탈퇴했을 때
                    withdrawnUserId, existingPlayRecordId, existingGainedRewardId,
                    getMyRewardFunc, UserNotFoundException.class
            );
            TestUtils.assertThrow(    // 기록 없을 때
                    existingUserId, notExistingId, existingGainedRewardId,
                    getMyRewardFunc, PlayRecordNotFoundException.class
            );
            TestUtils.assertThrow(    // 보상 없을 때
                    existingUserId, existingPlayRecordId, notExistingId,
                    getMyRewardFunc, GainedRewardNotFoundException.class
            );
        }

        // 보상 삭제하기
        {
            BiFunction<Long, Long, ?> deleteMyRewardsFunc
                    = (uid, pid) -> userRecordService.deleteMyRewards(uid, pid);

            TestUtils.assertThrow(    // 사용자 없을 때
                    notExistingId, existingPlayRecordId,
                    deleteMyRewardsFunc, UserNotFoundException.class
            );
            TestUtils.assertThrow(    // 탈퇴
                    withdrawnUserId, existingPlayRecordId,
                    deleteMyRewardsFunc, UserNotFoundException.class
            );
            TestUtils.assertThrow(    // 기록 없을 때
                    existingUserId, notExistingId,
                    deleteMyRewardsFunc, PlayRecordNotFoundException.class
            );
        }
    }

    @Test
    @DisplayName("사용자는 오직 자신의 자원만 조회할 수 있다.")
    void testForbiddenException1() {
        Long userId = testUser.getId();
        Long userOwnedPlayRecordId;
        Long anotherUserOwnedPlayRecordId;
        Long anotherUserOwnedGainedRewardId;

        {
            userOwnedPlayRecordId = data.createPlayRecord(
                    userId, 33L,
                    PlayRecordStatus.CLEARED, PlayRecordVisibility.PRIVATE
            ).getId();

            Long anotherUserId = data.createUser().getId();
            anotherUserOwnedPlayRecordId = data.createPlayRecord(
                    anotherUserId, 58L,
                    PlayRecordStatus.PLAYING, PlayRecordVisibility.PUBLIC
            ).getId();
            anotherUserOwnedGainedRewardId = data.createGainedReward(
                    anotherUserId, anotherUserOwnedPlayRecordId,
                    333L, RewardStorageType.LOCAL_STORAGE
            ).getId();
        }

        assertThatThrownBy(() -> userRecordService.getMyRecord(
                userId, anotherUserOwnedPlayRecordId
        ))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> userRecordService.getMyRewards(
                userId, anotherUserOwnedPlayRecordId, 0, 10
        ))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> userRecordService.getMyReward(
                userId, userOwnedPlayRecordId, anotherUserOwnedGainedRewardId
        ))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> userRecordService.getMyReward(
                userId, anotherUserOwnedPlayRecordId, anotherUserOwnedGainedRewardId
        ))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("사용자는 오직 자신의 자원 수정, 삭제할 수 있다.")
    void testForbiddenException2() {
        Long userId = testUser.getId();
        Long anotherUserOwnedPlayRecordId;

        {
            Long anotherUserId = data.createUser().getId();
            anotherUserOwnedPlayRecordId = data.createPlayRecord(
                    anotherUserId, 58L,
                    PlayRecordStatus.PLAYING, PlayRecordVisibility.PUBLIC
            ).getId();
        }

        assertThatThrownBy(() -> userRecordService.changeMyRecordVisibility(
                userId, anotherUserOwnedPlayRecordId, PlayRecordVisibility.PUBLIC
        ))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> userRecordService.deleteMyRewards(
                userId, anotherUserOwnedPlayRecordId
        ))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("사용자는 클리어한 기록의 보상만 조회할 수 있다.")
    void testNonClearedPlayRecordException() {
        Long userId = testUser.getId();
        Long playRecordId;

        {
            playRecordId = data.createPlayRecord(
                    userId, 58L,
                    PlayRecordStatus.PLAYING, PlayRecordVisibility.PUBLIC
            ).getId();
        }

        assertThatThrownBy(() -> userRecordService.getMyRewards(
                userId, playRecordId, 0, 10
        ))
                .isInstanceOf(NonClearedPlayRecordException.class);
        assertThatThrownBy(() -> userRecordService.getMyReward(
                userId, playRecordId, 4321L
        ))
                .isInstanceOf(NonClearedPlayRecordException.class);
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
                    .build();
        }

        PlayRecord createPlayRecord(
                Long userId, Long problemId,
                PlayRecordStatus playRecordStatus, PlayRecordVisibility visibility
        ) {
            String title = UUID.randomUUID().toString();
            int nOfSToGetReward = 10;
            int nOfSToFailPlay = 5;
            return initializer.playRecordBuilder()
                    .userId(userId)
                    .problemId(problemId)
                    .title(title)
                    .status(playRecordStatus)
                    .visibility(visibility)
                    .numOfScenariosToGetReward(nOfSToGetReward)
                    .numOfScenariosToFailPlay(nOfSToFailPlay)
                    .build();
        }

        GainedReward createGainedReward(
                Long userId, Long playRecordId, Long rewardId,
                RewardStorageType storageType
        ) {
            String description = UUID.randomUUID().toString();
            String location = UUID.randomUUID().toString();
            return initializer.gainedRewardBuilder()
                    .userId(userId)
                    .playRecordId(playRecordId)
                    .rewardId(rewardId)
                    .description(description)
                    .location(location)
                    .storageType(storageType)
                    .build();
        }

        ScenarioRecord createUnsubmittedScenarioRecord(
                Long playRecordId, int scenarioOrder
        ) {
            String scenarioContent = UUID.randomUUID().toString();
            return initializer.scenarioRecordBuilder()
                    .playRecordId(playRecordId)
                    .scenarioOrder(scenarioOrder)
                    .scenarioContent(scenarioContent)
                    .build();
        }

        ScenarioRecord createSubmittedScenarioRecord(
                Long playRecordId, int scenarioOrder,
                LocalDateTime submittedAt, boolean hasPassed
        ) {
            String scenarioContent = UUID.randomUUID().toString();
            String userSubmissionContent = UUID.randomUUID().toString();
            String aiGeneratedContent = UUID.randomUUID().toString();
            return initializer.scenarioRecordBuilder()
                    .playRecordId(playRecordId)
                    .scenarioOrder(scenarioOrder)
                    .scenarioContent(scenarioContent)
                    .hasSubmitted(true)
                    .userSubmissionContent(userSubmissionContent)
                    .aiGeneratedContent(aiGeneratedContent)
                    .submittedAt(submittedAt)
                    .hasPassed(hasPassed)
                    .build();
        }

        void initAll() {
            initializer.initAll();
        }
    }
}