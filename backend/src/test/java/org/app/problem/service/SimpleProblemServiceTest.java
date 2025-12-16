package org.app.problem.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import lombok.extern.slf4j.*;
import org.*;
import org.app.entity.*;
import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.filter.*;
import org.app.problem.dto.*;
import org.app.util.*;
import org.app.util.api.*;
import org.app.util.exception.*;
import org.assertj.core.data.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.transaction.annotation.*;
import org.support.*;

@Slf4j
@Import(SimpleProblemServiceTest.DataInitFacade.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class SimpleProblemServiceTest extends IntegrationTestSupport {

    static User testUser;

    @Autowired
    SimpleProblemService service;

    @Autowired
    DataInitFacade data;

    @MockitoSpyBean
    DateTimeProvider dateTimeProvider;

    @Autowired
    TestProblemRepository problemRepo;

    @Autowired
    TestProblemAggregationRepository problemAggregationRepo;

    @BeforeEach
    void setUp() {
        testUser = data.createUser();
    }

    @AfterEach
    void tearDown() {
        data.initAll();
    }

    @Test
    @DisplayName("공개된 문제 목록을 조회할 수 있다.")
    void getPublicProblems() {
        Long userId = testUser.getId();
        int numOfPublicNonSoftDeleted = 10;
        List<Problem> publicNonSoftDeletedEntities;

        int numOfPrivates = 5;
        int numOfSoftDeleted = 15;

        {
            publicNonSoftDeletedEntities = new ArrayList<>(
                    numOfPublicNonSoftDeleted
            );

            for (int i = 0; i < numOfPublicNonSoftDeleted; i++) {
                String title = String.format("PUB title-%d", i);

                AggregatedProblemPlayInfo playInfo = new AggregatedProblemPlayInfo(i);
                AggregatedProblemRatingInfo ratingInfo = new AggregatedProblemRatingInfo(
                        i, i * i
                );

                publicNonSoftDeletedEntities.add(data.createPublicProblem(
                        userId, title, title, i, i, i, i,
                        playInfo, ratingInfo
                ));
            }

            for (int i = 0; i < numOfPrivates; i++) {
                String title = String.format("PRIV title-%d", i);
                data.createPrivateProblem(userId, title);
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDate today = LocalDate.now();

            for (int i = 0; i < numOfSoftDeleted; i++) {
                String title = String.format("SOFT DELETED title-%d", i);
                ProblemVisibility visibility = (i & 0b1) == 0b1 ?
                        ProblemVisibility.PUBLIC : ProblemVisibility.PRIVATE;

                data.createSoftDeletedProblem(
                        userId, title, visibility, now, today
                );
            }
        }

        int pageNo = 0;

        SimplePageResponse<SimplePublicProblemInfo> response = service.getPublicProblems(
                pageNo, numOfPublicNonSoftDeleted
        );

        assertThat(response).isNotNull();

        TestUtils.assertSimplePageResponseEquality(
                response, pageNo, numOfPublicNonSoftDeleted,
                numOfPublicNonSoftDeleted, numOfPublicNonSoftDeleted, false
        );

        List<SimplePublicProblemInfo> elements = response.pagedElements();
        assertThat(elements).isNotNull().hasSize(numOfPublicNonSoftDeleted);

        Map<Long, Problem> problemMap = publicNonSoftDeletedEntities.stream()
                .collect(Collectors.toMap(Problem::getId, Function.identity()));

        for (SimplePublicProblemInfo element : elements) {

            assertThat(element).isNotNull();

            Long entityId = element.problemId();
            assertThat(entityId).isNotNull();
            assertThat(problemMap).containsKey(entityId);

            Problem entity = problemMap.get(entityId);
            assertThat(element.problemId()).isEqualTo(entityId);
            assertThat(element.title()).isEqualTo(entity.getTitle());
            assertThat(element.userId()).isEqualTo(userId);

            assertThat(element.numOfTotalScenarios()).isEqualTo(entity.getNumOfTotalScenarios());
            assertThat(element.numOfRewardSets()).isEqualTo(entity.getNumOfRewardSets());
            assertThat(element.createdAt())
                    .isCloseTo(entity.getCreatedAt(), within(Duration.ofSeconds(5L)));

            AggregatedInfo aggregatedInfo = element.aggregatedInfo();
            assertThat(aggregatedInfo).isNotNull();

            ProblemAggregation agg = entity.getProblemAggregation();
            AggregatedProblemPlayInfo aggPli = agg.getPlayInfo();
            AggregatedProblemRatingInfo aggRi = agg.getRatingInfo();

            PlayInfo playInfo = aggregatedInfo.playInfo();
            assertThat(playInfo).isNotNull();
            assertThat(playInfo.numOfTotalPlays())
                    .isEqualTo(aggPli.getNumOfTotalPlays());

            RatingInfo ratingInfo = aggregatedInfo.ratingInfo();
            assertThat(ratingInfo).isNotNull();
            assertThat(ratingInfo.numOfTotalRatings())
                    .isEqualTo(aggRi.getNumOfTotalRatings());
            assertThat(ratingInfo.ratingScoreAverage())
                    .isCloseTo(aggRi.getRatingAverage(), Offset.offset(0.1d));
        }
    }

    @Test
    @DisplayName("공개된 문제에 한해 세부 검색이 지원된다.")
    void searchPublicProblems() {
        Long userId = testUser.getId();
        int numOfPublicNonSoftDeletedAboveRatingAvg5 = 20;
        List<Problem> publicNonSoftDeletedAboveRatingAvg5Entities;
        ProblemFilter<Double> aboveRatingAvg5Filter;

        int numOfPublicNonSoftDeleted = 5;
        int numOfPrivates = 10;
        int numOfSoftDeleted = 15;

        {
            publicNonSoftDeletedAboveRatingAvg5Entities = new ArrayList<>(
                    numOfPublicNonSoftDeletedAboveRatingAvg5
            );

            for (int i = 1; i <= numOfPublicNonSoftDeletedAboveRatingAvg5; i++) {
                String title = String.format("PUB AVG5 ABOVE title-%d", i);
                AggregatedProblemRatingInfo ratingInfo = new AggregatedProblemRatingInfo(
                        i, 10L * i
                );

                publicNonSoftDeletedAboveRatingAvg5Entities.add(data.createPublicProblem(
                        userId, title, title,
                        0, 0, 0, 0,
                        null, ratingInfo
                ));
            }

            for (int i = 0; i < numOfPublicNonSoftDeleted; i++) {
                String title = String.format("PUB title-%d", i);
                data.createPublicProblem(
                        userId, title, title,
                        0, 0, 0, 0,
                        null, null
                );
            }

            for (int i = 0; i < numOfPrivates; i++) {
                String title = String.format("PRIV title-%d", i);
                data.createPrivateProblem(userId, title);
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDate today = LocalDate.now();

            for (int i = 0; i < numOfSoftDeleted; i++) {
                String title = String.format("SOFT DELETED title-%d", i);
                ProblemVisibility visibility = (i & 0b1) == 0b1 ?
                        ProblemVisibility.PUBLIC : ProblemVisibility.PRIVATE;

                data.createSoftDeletedProblem(
                        userId, title, visibility, now, today
                );
            }

            aboveRatingAvg5Filter = new ProblemFilter<>() {
                @Override
                public Double getFrom() {
                    return 5.d;
                }

                @Override
                public Double getTo() {
                    return null;
                }

                @Override
                public Double getEqualTo() {
                    return null;
                }

                @Override
                public ProblemFilterType getFilterType() {
                    return ProblemFilterType.RATING_AVG;
                }
            };
        }

        int pageNo = 0;

        SimplePageResponse<SimplePublicProblemInfo> response = service.searchPublicProblems(
                List.of(aboveRatingAvg5Filter),
                Collections.emptyList(),
                pageNo, numOfPublicNonSoftDeletedAboveRatingAvg5
        );

        TestUtils.assertSimplePageResponseEquality(
                response, pageNo, numOfPublicNonSoftDeletedAboveRatingAvg5,
                numOfPublicNonSoftDeletedAboveRatingAvg5,
                numOfPublicNonSoftDeletedAboveRatingAvg5, false
        );

        List<SimplePublicProblemInfo> elements = response.pagedElements();
        assertThat(elements).isNotNull().hasSize(numOfPublicNonSoftDeletedAboveRatingAvg5);

        Map<Long, Problem> problemMap = publicNonSoftDeletedAboveRatingAvg5Entities.stream()
                .collect(Collectors.toMap(Problem::getId, Function.identity()));

        for (SimplePublicProblemInfo element : elements) {

            assertThat(element).isNotNull();

            Long entityId = element.problemId();
            assertThat(entityId).isNotNull();
            assertThat(problemMap).containsKey(entityId);

            Problem entity = problemMap.get(entityId);
            assertThat(element.problemId()).isEqualTo(entityId);
            assertThat(element.title()).isEqualTo(entity.getTitle());
            assertThat(element.userId()).isEqualTo(userId);
            assertThat(element.createdAt())
                    .isCloseTo(entity.getCreatedAt(), within(Duration.ofSeconds(5L)));

            AggregatedInfo aggregatedInfo = element.aggregatedInfo();
            assertThat(aggregatedInfo).isNotNull();

            AggregatedProblemRatingInfo aggRi = entity.getProblemAggregation().getRatingInfo();
            RatingInfo ratingInfo = aggregatedInfo.ratingInfo();

            assertThat(ratingInfo).isNotNull();
            assertThat(ratingInfo.numOfTotalRatings())
                    .isEqualTo(aggRi.getNumOfTotalRatings());
            assertThat(ratingInfo.ratingScoreAverage())
                    .isGreaterThan(5.d)
                    .isCloseTo(aggRi.getRatingAverage(), Offset.offset(0.1d));
        }
    }

    @Test
    @DisplayName("Public 한 문제는 누구든 조회할 수 있다.")
    void getProblem1() {
        Long userId;
        String userName = "User name";
        String title = "Title";
        String description = "Description";
        int nToGetReward = 10;
        int nToFailPlay = 5;
        int nOfRewards = 3;
        int nOfScenarios = 6;
        long nOfPlays = 9;
        long nOfRatings = 12;
        long ratingScoreAvg = 15;

        Long problemId;

        {
            userId = data.createUser(userName).getId();

            AggregatedProblemPlayInfo playInfo = new AggregatedProblemPlayInfo(
                    nOfPlays
            );
            AggregatedProblemRatingInfo ratingInfo = new AggregatedProblemRatingInfo(
                    nOfRatings, ratingScoreAvg * nOfRatings
            );

            problemId = data.createPublicProblem(
                    userId, title, description, nToGetReward, nToFailPlay,
                    nOfScenarios, nOfRewards, playInfo, ratingInfo
            ).getId();
        }

        for (int i = 0; i < 2; i++) {
            boolean useUserId = (i & 0b1) == 0b1;

            Long authenticatedUserId = useUserId ? userId : null;

            DetailedProblemInfo response = service.getProblem(problemId, authenticatedUserId);
            assertThat(response).isNotNull();

            assertThat(response.problemId()).isEqualTo(problemId);
            assertThat(response.userId()).isEqualTo(userId);
            assertThat(response.title()).isEqualTo(title);
            assertThat(response.description()).isEqualTo(description);
            assertThat(response.userName()).isEqualTo(userName);

            assertThat(response.numOfScenariosToGetReward()).isEqualTo(nToGetReward);
            assertThat(response.numOfScenariosToFailPlay()).isEqualTo(nToFailPlay);
            assertThat(response.visibility()).isEqualTo(ProblemVisibility.PUBLIC);
            assertThat(response.isMine()).isEqualTo(
                    userId.equals(authenticatedUserId)
            );

            assertThat(response.numOfTotalScenarios()).isEqualTo(nOfScenarios);
            assertThat(response.numOfRewardSets()).isEqualTo(nOfRewards);

            AggregatedInfo aggregatedInfo = response.aggregatedInfo();
            assertThat(aggregatedInfo).isNotNull();

            PlayInfo playInfo = aggregatedInfo.playInfo();
            assertThat(playInfo).isNotNull();
            assertThat(playInfo.numOfTotalPlays()).isEqualTo(nOfPlays);

            RatingInfo ratingInfo = aggregatedInfo.ratingInfo();
            assertThat(ratingInfo).isNotNull();
            assertThat(ratingInfo.numOfTotalRatings()).isEqualTo(nOfRatings);
            assertThat(ratingInfo.ratingScoreAverage())
                    .isCloseTo(ratingScoreAvg, Offset.offset(0.1d));
        }
    }

    @Test
    @DisplayName("문제 작성자와 초대 수령한 사용자는 private 문제를 조회할 수 있다.")
    void getProblem2() {
        Long invitedUserId;
        Long problemOwnerId;
        String userName = "User name";
        String title = "Title";
        String description = "Description";
        int nToGetReward = 10;
        int nToFailPlay = 5;

        Long privateProblemId;

        {
            invitedUserId = data.createUser().getId();
            problemOwnerId = data.createUser(userName).getId();

            privateProblemId = data.createPrivateProblem(
                    problemOwnerId, title, description, nToGetReward, nToFailPlay
            ).getId();

            String validCode = "This is valid";
            String invalidCode = "This is an invalid code";

            data.createProblemInvitatino(privateProblemId, validCode);

            data.createRecievedInvitation(
                    invitedUserId, privateProblemId, validCode, title
            );
            data.createRecievedInvitation(
                    invitedUserId, privateProblemId, invalidCode, title
            );
        }

        for (int i = 0; i < 2; i++) {
            boolean useOwnerId = (i & 0b1) == 0b1;

            Long authenticatedUserId = useOwnerId ? problemOwnerId : invitedUserId;

            DetailedProblemInfo response = service.getProblem(
                    privateProblemId, authenticatedUserId
            );
            assertThat(response).isNotNull();

            assertThat(response.problemId()).isEqualTo(privateProblemId);
            assertThat(response.userId()).isEqualTo(problemOwnerId);
            assertThat(response.title()).isEqualTo(title);
            assertThat(response.description()).isEqualTo(description);
            assertThat(response.userName()).isEqualTo(userName);

            assertThat(response.numOfScenariosToGetReward()).isEqualTo(nToGetReward);
            assertThat(response.numOfScenariosToFailPlay()).isEqualTo(nToFailPlay);
            assertThat(response.visibility()).isEqualTo(ProblemVisibility.PRIVATE);
            assertThat(response.isMine()).isEqualTo(
                    problemOwnerId.equals(authenticatedUserId)
            );

            assertThat(response.aggregatedInfo()).isNull();
        }
    }

    @Test
    @DisplayName("로그인하지 않았거나 초대 수령하지 않은 유저는 "
                 + "private 문제 조회시 ForbiddenException 을 받는다.")
    void getProblem3() {
        Long nonInvitedUserId;
        Long expiredInvitationHavingUserId;
        String userName = "User name";
        String title = "Title";
        String description = "Description";
        int nToGetReward = 10;
        int nToFailPlay = 5;

        Long privateProblemId;

        {
            expiredInvitationHavingUserId = data.createUser().getId();
            nonInvitedUserId = data.createUser().getId();
            Long problemOwnerId = data.createUser(userName).getId();

            privateProblemId = data.createPrivateProblem(
                    problemOwnerId, title, description, nToGetReward, nToFailPlay
            ).getId();

            String validCode = "This is valid";
            String invalidCode = "This is an invalid code";

            data.createProblemInvitatino(privateProblemId, validCode);
            data.createRecievedInvitation(
                    expiredInvitationHavingUserId, privateProblemId, invalidCode, title
            );
        }

        assertThatThrownBy(() -> service.getProblem(
                privateProblemId, null
        ))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> service.getProblem(
                privateProblemId, nonInvitedUserId
        ))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> service.getProblem(
                privateProblemId, expiredInvitationHavingUserId
        ))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("Public 한 문제를 생성하면 집계 테이블이 생성된다.")
    void createProblem1() {
        Long userId = testUser.getId();

        String title = "Title";
        String description = "Description";
        String rewardMessage = "Reward Message";
        int numOfScenariosToGetReward = 3;
        int numOfScenariosToFailPlay = 6;
        int numOfTotalScenarios = 3;
        ProblemVisibility visibility = ProblemVisibility.PUBLIC;
        String serializedScenarioInfo = "temp";

        SerializedProblemCreationInfo info = new SerializedProblemCreationInfo(
                title, description, rewardMessage,
                numOfScenariosToGetReward, numOfScenariosToFailPlay,
                visibility, numOfTotalScenarios, serializedScenarioInfo
        );

        Long response = service.createProblem(userId, info);
        assertThat(response).isNotNull();

        Problem problemFind = problemRepo.findById(response)
                .orElseThrow(AssertionError::new);

        assertThat(problemFind.getId()).isEqualTo(response);
        assertThat(problemFind.getTitle()).isEqualTo(title);
        assertThat(problemFind.getDescription()).isEqualTo(description);
        assertThat(problemFind.getRewardMessage()).isEqualTo(rewardMessage);

        assertThat(problemFind.getNumOfScenariosToGetReward()).isEqualTo(numOfScenariosToGetReward);
        assertThat(problemFind.getNumOfScenariosToFailPlay()).isEqualTo(numOfScenariosToFailPlay);
        assertThat(problemFind.getVisibility()).isEqualTo(visibility);

        assertThat(problemFind.getNumOfTotalScenarios()).isEqualTo(numOfTotalScenarios);
        assertThat(problemFind.getNumOfRewardSets()).isZero();
        assertThat(problemFind.getSerializedScenarioInfo()).isEqualTo(serializedScenarioInfo);

        assertThat(problemFind.doesRemovalScheduled()).isFalse();

        ScheduledRemoval scheduledRemoval = problemFind.getScheduledRemoval();
        assertThat(scheduledRemoval).isNotNull();
        assertThat(scheduledRemoval.doesRemovalScheduled()).isFalse();

        ProblemAggregation problemAggFind = problemAggregationRepo.findById(response)
                .orElseThrow(AssertionError::new);
        assertThat(problemAggFind.getProblemId()).isEqualTo(response);

        AggregatedProblemPlayInfo playInfo = problemAggFind.getPlayInfo();
        assertThat(playInfo).isNotNull();
        assertThat(playInfo.getNumOfTotalPlays()).isZero();

        AggregatedProblemRatingInfo ratingInfo = problemAggFind.getRatingInfo();
        assertThat(ratingInfo).isNotNull();
        assertThat(ratingInfo.getNumOfTotalRatings()).isZero();
        assertThat(ratingInfo.getSumOfTotalRatingScore()).isZero();
        assertThat(ratingInfo.getRatingAverage())
                .isGreaterThanOrEqualTo(0.d)
                .isCloseTo(0.d, Offset.offset(0.1d));
    }

    @Test
    @DisplayName("Private 한 문제를 생성하면 집계 테이블이 생성되지 않는다.")
    void createProblem2() {
        Long userId = testUser.getId();

        String title = "Title";
        String description = "Description";
        String rewardMessage = "Reward Message";
        int numOfScenariosToGetReward = 3;
        int numOfScenariosToFailPlay = 6;
        int numOfTotalScenarios = 3;
        ProblemVisibility visibility = ProblemVisibility.PRIVATE;
        String serializedScenarioInfo = "temp";

        SerializedProblemCreationInfo info = new SerializedProblemCreationInfo(
                title, description, rewardMessage,
                numOfScenariosToGetReward, numOfScenariosToFailPlay,
                visibility, numOfTotalScenarios, serializedScenarioInfo
        );

        Long response = service.createProblem(userId, info);
        assertThat(response).isNotNull();

        Problem problemFind = problemRepo.findById(response)
                .orElseThrow(AssertionError::new);

        assertThat(problemFind.getId()).isEqualTo(response);
        assertThat(problemFind.getTitle()).isEqualTo(title);
        assertThat(problemFind.getDescription()).isEqualTo(description);
        assertThat(problemFind.getRewardMessage()).isEqualTo(rewardMessage);

        assertThat(problemFind.getNumOfScenariosToGetReward()).isEqualTo(numOfScenariosToGetReward);
        assertThat(problemFind.getNumOfScenariosToFailPlay()).isEqualTo(numOfScenariosToFailPlay);
        assertThat(problemFind.getVisibility()).isEqualTo(visibility);

        assertThat(problemFind.getNumOfTotalScenarios()).isEqualTo(numOfTotalScenarios);
        assertThat(problemFind.getNumOfRewardSets()).isZero();
        assertThat(problemFind.getSerializedScenarioInfo()).isEqualTo(serializedScenarioInfo);

        assertThat(problemFind.doesRemovalScheduled()).isFalse();

        ScheduledRemoval scheduledRemoval = problemFind.getScheduledRemoval();
        assertThat(scheduledRemoval).isNotNull();
        assertThat(scheduledRemoval.doesRemovalScheduled()).isFalse();

        Optional<ProblemAggregation> empty = problemAggregationRepo.findById(response);
        assertThat(empty).isEmpty();
    }

    @Test
    @DisplayName("Public, Private 속성의 문제를 수정할 수 있다.")
    void updateProblem() {
        Long userId = testUser.getId();

        String title = "Changed : Title";
        String description = "Changed : Description";
        String rewardMessage = "Changed : Reward Message";
        int numOfScenariosToGetReward = 3;
        int numOfScenariosToFailPlay = 6;
        int numOfTotalScenarios = 3;
        String serializedScenarioInfo = "Changed : temp";

        Long publicProblemId, privateProblemId;

        {
            String origin = "origin";
            int iOrigin = 1234;

            AggregatedProblemPlayInfo playInfo = new AggregatedProblemPlayInfo();
            AggregatedProblemRatingInfo ratingInfo = new AggregatedProblemRatingInfo();

            publicProblemId = data.createPublicProblem(
                    userId, origin, origin,
                    iOrigin, iOrigin, iOrigin, iOrigin,
                    playInfo, ratingInfo
            ).getId();

            privateProblemId = data.createPrivateProblem(userId, origin)
                    .getId();
        }

        assertThat(publicProblemId).isNotEqualTo(privateProblemId);

        SerializedProblemUpdateInfo updateInfo = new SerializedProblemUpdateInfo(
                title, description, rewardMessage,
                numOfScenariosToGetReward, numOfScenariosToFailPlay,
                numOfTotalScenarios, serializedScenarioInfo
        );

        Long response1 = service.updateProblem(publicProblemId, userId, updateInfo);
        Long response2 = service.updateProblem(privateProblemId, userId, updateInfo);

        assertThat(response1).isNotNull().isEqualTo(publicProblemId);
        assertThat(response2).isNotNull().isEqualTo(privateProblemId);

        for (Long problemId : List.of(publicProblemId, privateProblemId)) {

            Problem find = problemRepo.findById(problemId)
                    .orElseThrow(AssertionError::new);

            assertThat(find.getTitle()).isEqualTo(title);
            assertThat(find.getDescription()).isEqualTo(description);
            assertThat(find.getRewardMessage()).isEqualTo(rewardMessage);
            assertThat(find.getNumOfScenariosToGetReward()).isEqualTo(numOfScenariosToGetReward);
            assertThat(find.getNumOfScenariosToFailPlay()).isEqualTo(numOfScenariosToFailPlay);
            assertThat(find.getNumOfTotalScenarios()).isEqualTo(numOfTotalScenarios);
            assertThat(find.getSerializedScenarioInfo()).isEqualTo(serializedScenarioInfo);

            Optional<ProblemAggregation> aggOptional = problemAggregationRepo.findById(problemId);

            if (problemId.equals(privateProblemId)) {
                assertThat(find.getVisibility()).isEqualTo(ProblemVisibility.PRIVATE);
                assertThat(aggOptional).isEmpty();
            } else if (problemId.equals(publicProblemId)) {
                assertThat(find.getVisibility()).isEqualTo(ProblemVisibility.PUBLIC);
                assertThat(aggOptional).isPresent();
            }
        }
    }

    @Test
    @DisplayName("문제 공개 속성이 public 으로 바뀌면 집계 테이블이 생성된다.")
    void updateProblemVisibility1() {
        Long userId = testUser.getId();
        Long publicProblemId, privateProblemId;

        {
            String title = "title";

            publicProblemId = data.createPublicProblem(
                    userId, title, null,
                    0, 0, 0, 0,
                    null, null
            ).getId();

            privateProblemId = data.createPrivateProblem(userId, title).getId();
        }

        assertThat(publicProblemId).isNotEqualTo(privateProblemId);

        Long response1 = service.updateProblemVisibility(
                publicProblemId, userId, ProblemVisibility.PUBLIC
        );
        Long response2 = service.updateProblemVisibility(
                privateProblemId, userId, ProblemVisibility.PUBLIC
        );

        assertThat(response1).isNotNull().isEqualTo(publicProblemId);
        assertThat(response2).isNotNull().isEqualTo(privateProblemId);

        List<Long> responses = List.of(response1, response2);
        for (Long response : responses) {

            Problem find = problemRepo.findById(response)
                    .orElseThrow(AssertionError::new);

            assertThat(find.getVisibility()).isEqualTo(ProblemVisibility.PUBLIC);

            Optional<ProblemAggregation> opt = problemAggregationRepo.findById(response);
            assertThat(opt).isPresent();
        }
    }

    @Test
    @DisplayName("문제 공개 속성이 private 으로 바뀌면 집계 테이블이 삭제된다.")
    void updateProblemVisibility2() {
        Long userId = testUser.getId();
        Long publicProblemId, privateProblemId;

        {
            String title = "title";
            publicProblemId = data.createPublicProblem(
                    userId, title, null,
                    0, 0, 0, 0,
                    null, null
            ).getId();
            privateProblemId = data.createPrivateProblem(userId, title).getId();
        }

        assertThat(publicProblemId).isNotEqualTo(privateProblemId);

        Long response1 = service.updateProblemVisibility(
                publicProblemId, userId, ProblemVisibility.PRIVATE
        );
        Long response2 = service.updateProblemVisibility(
                privateProblemId, userId, ProblemVisibility.PRIVATE
        );

        assertThat(response1).isNotNull().isEqualTo(publicProblemId);
        assertThat(response2).isNotNull().isEqualTo(privateProblemId);

        List<Long> responses = List.of(response1, response2);
        for (Long response : responses) {

            Problem find = problemRepo.findById(response)
                    .orElseThrow(AssertionError::new);

            assertThat(find.getVisibility()).isEqualTo(ProblemVisibility.PRIVATE);

            Optional<ProblemAggregation> opt = problemAggregationRepo.findById(response);
            assertThat(opt).isEmpty();
        }
    }

    @Test
    @DisplayName("문제 삭제 요청시 삭제 스켸쥴이 잡히고 집계 테이블은 삭제된다.")
    void deleteProblem() {
        Long userId = testUser.getId();
        Long publicProblemId, privateProblemId;

        {
            String title = "title";
            publicProblemId = data.createPublicProblem(
                    userId, title, null,
                    0, 0, 0, 0,
                    null, null
            ).getId();
            privateProblemId = data.createPrivateProblem(userId, title).getId();
        }

        assertThat(publicProblemId).isNotEqualTo(privateProblemId);

        LocalDateTime requestedTime = dateTimeProvider.localDateTimeNow()
                .plusHours(1L);
        when(dateTimeProvider.localDateTimeNow())
                .thenReturn(requestedTime)
                .thenReturn(requestedTime);

        Long response1 = service.deleteProblem(publicProblemId, userId);
        Long response2 = service.deleteProblem(privateProblemId, userId);

        assertThat(response1).isNotNull().isEqualTo(publicProblemId);
        assertThat(response2).isNotNull().isEqualTo(privateProblemId);

        List<Long> responses = List.of(response1, response2);
        for (Long response : responses) {

            Problem find = problemRepo.findById(response)
                    .orElseThrow(AssertionError::new);

            assertThat(find.doesRemovalScheduled()).isTrue();

            ScheduledRemoval scheduledRemoval = find.getScheduledRemoval();
            assertThat(scheduledRemoval).isNotNull();
            assertThat(scheduledRemoval.doesRemovalScheduled()).isTrue();
            assertThat(scheduledRemoval.requestedAt())
                    .isCloseTo(requestedTime, within(Duration.ofSeconds(5L)));
            assertThat(scheduledRemoval.scheduedAt()).isNotNull();

            Optional<ProblemAggregation> opt = problemAggregationRepo.findById(response);
            assertThat(opt).isEmpty();
        }

    }

    @Test
    @DisplayName("관련 자원을 찾을 수 없으면 NotFoundException 이 발생한다.")
    void testNotFoundException() {
        Long existingUserId, withdrawnUserId;
        Long existingPublicProblemId, existingPrivateProblemId, softDeletedProblemId;

        Long nonExsitingUserId, nonExsitingProblemId;

        {
            existingUserId = data.createUser().getId();
            withdrawnUserId = data.createWithdrawnUser().getId();

            Long userId = testUser.getId();

            existingPublicProblemId = data.createPublicProblem(
                    userId, "title", null,
                    0, 0, 0, 0,
                    null, null
            ).getId();

            existingPrivateProblemId = data.createPrivateProblem(userId, "title")
                    .getId();

            softDeletedProblemId = data.createSoftDeletedProblem(
                    userId, "title", ProblemVisibility.PUBLIC,
                    LocalDateTime.now(), LocalDate.now()
            ).getId();

            nonExsitingUserId = nonExsitingProblemId = Long.MAX_VALUE;
        }

        // 문제 정보 조회하기
        {
            BiFunction<Long, Long, ?> getProblemFunc
                    = (pid, uid) -> service.getProblem(pid, uid);

            TestUtils.assertThrow(      // 문제 없을 때
                    nonExsitingProblemId, existingUserId,
                    getProblemFunc, ProblemNotFoundException.class
            );

            TestUtils.assertThrow(      // 문제 삭제 예정일 때
                    softDeletedProblemId, existingUserId,
                    getProblemFunc, ProblemNotFoundException.class
            );

            TestUtils.assertThrow(      // 문제 private 인데 없는 사용자일 때
                    existingPrivateProblemId, nonExsitingUserId,
                    getProblemFunc, UserNotFoundException.class
            );

            TestUtils.assertThrow(      // 문제 private 인데 없는 탈퇴한 사용자일 때
                    existingPrivateProblemId, withdrawnUserId,
                    getProblemFunc, UserNotFoundException.class
            );
        }

        // 문제 생성하기
        {
            //noinspection DataFlowIssue
            Function<Long, ?> createProblemFunc
                    = uid -> service.createProblem(uid, null);

            TestUtils.assertThrow(      // 사용자 없을 때
                    nonExsitingUserId, createProblemFunc,
                    UserNotFoundException.class
            );

            TestUtils.assertThrow(      // 탈퇴한 사용자일 때
                    withdrawnUserId, createProblemFunc,
                    UserNotFoundException.class
            );
        }

        // 문제 수정하기
        {
            BiFunction<Long, Long, ?> updateProblemFunc
                    = (pid, uid) -> service.updateProblem(pid, uid, null);

            // 사용자 없을 때
            TestUtils.assertThrow(
                    existingPublicProblemId, nonExsitingUserId,
                    updateProblemFunc, UserNotFoundException.class
            );

            // 탈퇴한 사용자일 때
            TestUtils.assertThrow(
                    existingPublicProblemId, withdrawnUserId,
                    updateProblemFunc, UserNotFoundException.class
            );

            // 문제 없을 때
            TestUtils.assertThrow(
                    nonExsitingProblemId, existingUserId,
                    updateProblemFunc, ProblemNotFoundException.class
            );

            // 문제 삭제 예정일 때
            TestUtils.assertThrow(
                    softDeletedProblemId, existingUserId,
                    updateProblemFunc, ProblemNotFoundException.class
            );

        }

        // 문제 공개 속성 바꾸기
        {
            BiFunction<Long, Long, ?> updateVisivilityFunc
                    = (pid, uid) -> service.updateProblemVisibility(
                    pid, uid, null
            );

            // 사용자 없을 때
            TestUtils.assertThrow(
                    existingPublicProblemId, nonExsitingUserId,
                    updateVisivilityFunc, UserNotFoundException.class
            );

            // 탈퇴한 사용자일 때
            TestUtils.assertThrow(
                    existingPublicProblemId, withdrawnUserId,
                    updateVisivilityFunc, UserNotFoundException.class
            );

            // 문제 없을 때
            TestUtils.assertThrow(
                    nonExsitingProblemId, existingUserId,
                    updateVisivilityFunc, ProblemNotFoundException.class
            );

            // 문제 삭제 예정일 때
            TestUtils.assertThrow(
                    softDeletedProblemId, existingUserId,
                    updateVisivilityFunc, ProblemNotFoundException.class
            );
        }

        // 문제 삭제하기
        {
            BiFunction<Long, Long, ?> deleteProblemFunc
                    = (pid, uid) -> service.deleteProblem(pid, uid);

            // 사용자 없을 때
            TestUtils.assertThrow(
                    existingPublicProblemId, nonExsitingUserId,
                    deleteProblemFunc, UserNotFoundException.class
            );

            // 탈퇴한 사용자일 때
            TestUtils.assertThrow(
                    existingPublicProblemId, withdrawnUserId,
                    deleteProblemFunc, UserNotFoundException.class
            );

            // 문제 없을 때
            TestUtils.assertThrow(
                    nonExsitingProblemId, existingUserId,
                    deleteProblemFunc, ProblemNotFoundException.class
            );

            // 문제 삭제 예정일 때
            TestUtils.assertThrow(
                    softDeletedProblemId, existingUserId,
                    deleteProblemFunc, ProblemNotFoundException.class
            );
        }
    }

    @Test
    @DisplayName("문제 정보 수정, 공개 속성 변경, 삭제 요청은 작성자만 가능하다.")
    void testForbiddenException() {
        // 초대되지 않은 유저 forbidden exception 은 이전에 테스트 했다.

        Long userId;
        Long anotherUserOwnedPublicProblemId;
        Long anotherUserOwnedPrivateProblemId;

        {
            userId = data.createUser().getId();

            Long anotherUserId = data.createUser().getId();

            anotherUserOwnedPublicProblemId = data.createPublicProblem(
                    anotherUserId, "title", null,
                    0, 0, 0, 0,
                    null, null
            ).getId();

            anotherUserOwnedPrivateProblemId = data.createPrivateProblem(
                    anotherUserId, "title"
            ).getId();
        }

        // 문제 정보 수정하기
        {
            BiFunction<Long, Long, ?> updateProblemFunc
                    = (pid, uid) -> service.updateProblem(
                    pid, uid, null
            );

            TestUtils.assertThrow(
                    anotherUserOwnedPublicProblemId, userId,
                    updateProblemFunc, ForbiddenException.class
            );
            TestUtils.assertThrow(
                    anotherUserOwnedPrivateProblemId, userId,
                    updateProblemFunc, ForbiddenException.class
            );
        }

        // 문제 공개 속성 바꾸기
        {
            BiFunction<Long, Long, ?> updateVisibilityFunc
                    = (pid, uid) -> service.updateProblemVisibility(
                    pid, uid, null
            );

            TestUtils.assertThrow(
                    anotherUserOwnedPublicProblemId, userId,
                    updateVisibilityFunc, ForbiddenException.class
            );
            TestUtils.assertThrow(
                    anotherUserOwnedPrivateProblemId, userId,
                    updateVisibilityFunc, ForbiddenException.class
            );
        }

        // 문제 삭제하기
        {
            BiFunction<Long, Long, ?> deleteProblemFunc
                    = (pid, uid) -> service.deleteProblem(
                    pid, uid
            );

            TestUtils.assertThrow(
                    anotherUserOwnedPublicProblemId, userId,
                    deleteProblemFunc, ForbiddenException.class
            );
            TestUtils.assertThrow(
                    anotherUserOwnedPrivateProblemId, userId,
                    deleteProblemFunc, ForbiddenException.class
            );
        }

    }


    @Component
    @Transactional
    protected static class DataInitFacade {

        @Autowired
        GeneralDataInitializer initializer;

        User createUser() {
            return initializer.userBuilder()
                    .name("Test user")
                    .build();
        }

        User createWithdrawnUser() {
            return initializer.userBuilder()
                    .name("Test withdrawn user")
                    .withdrawn(true)
                    .withdrawnAt(LocalDateTime.now())
                    .build();
        }

        User createUser(String userName) {
            return initializer.userBuilder()
                    .name(userName)
                    .build();
        }

        Problem createPublicProblem(
                Long userId, String title,
                String descriptoin, int nToGetReward, int nToFailPlay,
                int nOfTotalScenarios, int nOfRewardSets,
                AggregatedProblemPlayInfo playInfo,
                AggregatedProblemRatingInfo ratingInfo
        ) {
            Problem problem = initializer.problemBuilder()
                    .userId(userId)
                    .title(title)
                    .description(descriptoin)
                    .numOfScenariosToGetReward(nToGetReward)
                    .numOfScenariosToFailPlay(nToFailPlay)
                    .visibility(ProblemVisibility.PUBLIC)
                    .numOfTotalScenarios(nOfTotalScenarios)
                    .serializedScenarioInfo("hi")
                    .numOfRewardSets(nOfRewardSets)
                    .build();

            initializer.problemAggregationBuilder()
                    .problemId(problem.getId())
                    .playInfo(playInfo)
                    .ratingInfo(ratingInfo)
                    .build();

            return problem;
        }

        Problem createPrivateProblem(Long userId, String title) {
            return initializer.problemBuilder()
                    .userId(userId)
                    .title(title)
                    .visibility(ProblemVisibility.PRIVATE)
                    .serializedScenarioInfo("hi")
                    .build();
        }

        Problem createPrivateProblem(
                Long userId, String title,
                String descriptoin, int nToGetReward, int nToFailPlay
        ) {
            return initializer.problemBuilder()
                    .userId(userId)
                    .title(title)
                    .description(descriptoin)
                    .numOfScenariosToGetReward(nToGetReward)
                    .numOfScenariosToFailPlay(nToFailPlay)
                    .visibility(ProblemVisibility.PRIVATE)
                    .serializedScenarioInfo("hi")
                    .build();
        }

        Problem createSoftDeletedProblem(
                Long userId, String title, ProblemVisibility visibility,
                LocalDateTime softDeletedAt, LocalDate reservedRemovalDate
        ) {
            Problem problem = initializer.problemBuilder()
                    .userId(userId)
                    .title(title)
                    .visibility(visibility)
                    .serializedScenarioInfo("hi")
                    .build();

            problem.reserveRemoval(softDeletedAt, reservedRemovalDate);

            return problem;
        }

        void createProblemInvitatino(Long problemId, String code) {
            initializer.invitationBuilder()
                    .problemId(problemId)
                    .code(code)
                    .build();
        }

        void createRecievedInvitation(
                Long userId, Long problemId,
                String code, String problemTitle
        ) {
            initializer.receivedInvitationBuilder()
                    .userId(userId)
                    .problemId(problemId)
                    .title(problemTitle)
                    .code(code)
                    .build();
        }

        void initAll() {
            initializer.initAll();
        }
    }
}