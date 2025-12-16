package org.app.problem.repository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import lombok.extern.slf4j.*;
import org.*;
import org.app.config.jpa.*;
import org.app.entity.*;
import org.app.problem.domain.search.filter.*;
import org.app.problem.domain.search.order.*;
import org.app.problem.dto.request.*;
import org.app.problem.service.strategy.order.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.transaction.annotation.*;
import org.support.*;

@Slf4j
@Import(DynamicProblemSearchRepositoryTest.DataInitFacade.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class DynamicProblemSearchRepositoryTest extends IntegrationTestSupport {

    private static final LocalDate
            CT_FROM = LocalDate.of(2025, 6, 4),
            CT_TO = LocalDate.of(2026, 6, 4);
    private static final long
            LONG_FROM = 100L,
            LONG_TO = 200L;
    private static final int
            INT_FROM = 10,
            INT_TO = 20;
    private static final double
            DOUBLE_FROM = 5.d,
            DOUBLE_TO = 8.d;
    @Autowired
    List<AbstractOrderRequestAdaptor> orderRequestAdaptors;
    @Autowired
    DynamicProblemSearchRepositoryImpl dynamicSearchRepo;
    @Autowired
    DataInitFacade data;
    @MockitoSpyBean
    EntityAuditingProvider auditingProvider;

    private static <T> ProblemFilter<T> gen(
            ProblemFilterType filterType, T from, T to, T equalTo
    ) {
        return new ProblemFilter<>() {
            @Override
            public T getFrom() {
                return from;
            }

            @Override
            public T getTo() {
                return to;
            }

            @Override
            public T getEqualTo() {
                return equalTo;
            }

            @Override
            public ProblemFilterType getFilterType() {
                return filterType;
            }
        };
    }

    private static List<ProblemFilter<?>> createProblemFilters(Long userId) {
        List<ProblemFilter<?>> filters = new ArrayList<>();

        filters.add(gen(
                ProblemFilterType.CREATED_DATE,
                CT_FROM, CT_TO, null
        ));
        filters.add(gen(
                ProblemFilterType.CREATED_USER, null, null, userId
        ));
        filters.add(gen(
                ProblemFilterType.NUM_OF_REWARDS,
                INT_FROM, INT_TO, null
        ));
        filters.add(gen(
                ProblemFilterType.NUM_OF_PLAYS,
                LONG_FROM, LONG_TO, null
        ));
        filters.add(gen(
                ProblemFilterType.NUM_OF_PROBLEM_SCENARIOS,
                INT_FROM, INT_TO, null
        ));
        filters.add(gen(
                ProblemFilterType.NUM_OF_RATINGS,
                LONG_FROM, LONG_TO, null
        ));
        filters.add(gen(
                ProblemFilterType.RATING_AVG,
                DOUBLE_FROM, DOUBLE_TO, null
        ));

        return filters;
    }

    private static double getAvg(Problem p) {
        return p.getProblemAggregation().getRatingInfo().getRatingAverage();
    }

    private static int getRewardCnt(Problem p) {
        return p.getNumOfRewardSets();
    }

    private static long getPlayCnt(Problem p) {
        return p.getProblemAggregation().getPlayInfo().getNumOfTotalPlays();
    }

    private static int getScCount(Problem p) {
        return p.getNumOfTotalScenarios();
    }

    private static Comparator<Problem> expectedOrdering() {
        Comparator<Problem> ratingAvgDesc = Comparator.comparing(
                DynamicProblemSearchRepositoryTest::getAvg,
                Comparator.nullsLast(Comparator.reverseOrder())
        );

        Comparator<Problem> rewardAsc = Comparator.comparing(
                DynamicProblemSearchRepositoryTest::getRewardCnt,
                Comparator.nullsLast(Comparator.naturalOrder())
        );

        Comparator<Problem> playCntDesc = Comparator.comparing(
                DynamicProblemSearchRepositoryTest::getPlayCnt,
                Comparator.nullsLast(Comparator.reverseOrder())
        );

        Comparator<Problem> createdAsc = Comparator.comparing(
                Problem::getCreatedAt
        );

        Comparator<Problem> scCntDesc = Comparator.comparing(
                DynamicProblemSearchRepositoryTest::getScCount,
                Comparator.nullsLast(Comparator.reverseOrder())
        );

        return ratingAvgDesc
                .thenComparing(rewardAsc)
                .thenComparing(playCntDesc)
                .thenComparing(createdAsc)
                .thenComparing(scCntDesc);
    }

    @AfterEach
    void tearDown() {
        data.initAll();
    }

    @Test
    @DisplayName("복수의 검색, 정렬 조건을 제시해 문제를 검색할 수 있다.")
    void searchNonSoftDeletedPublicProblemWithFilters() {
        /*
        데이터 설명:
        1.  데이터엔 사용자 3 명이 존재한다. 2 명은 모든 검색 조건에 포함되지 않을 데이터를 가지고 있고,
            단 한명만 모든 검색 조건에 포함되는 데이터를 갖고 있다.

        2.  더미 사용자 2 명은 각자 문제 5 개를 갖고있다.
            갖고있는 문제 절반은 PRIVATE, 나머지는 PUBLIC 속성이다. 이외 문제 정보는 임의로 구성된다.

        3.  진짜 사용자는 문제 (10 + a) 개를 갖고 있다.
            모든 문제는 PUBLIC 이며, 추가로 문제 10 개는 아래 검색 조건에 모두 부합한다.
            나머지 a 의 경우 각 검색 조건에 부합하지 않는 데이터를 칭한다.
            각 검색 조건별 부합하지 않는 데이터는 최소 1 개 이상 존재한다.

        4. 검색 조건은 다음과 같다.
            a. 사용자 3 이 생성한 문제이다.
            b. 2025-06-04 -- 2026-06-04 범위에 생성된 문제이다.
            c. 설정된 보상 개수가 10 -- 20 개 범위에 속해있다.
            d. 문제 플레이 횟수가 100 -- 200 회 범위에 속해있다.
            e. 문제에 설정된 시나리오 개수가 10 -- 20 개 범위에 속해있다.
            f. 문제 평가 총 개수가 100 -- 200 회 범위에 속해있다.
            g. 문제 평가 평균 점수가 5 -- 8 점 범위에 속해있다.

            각 조건은 static 변수의 값들과 동일하다.
            (CT_FROM, LONG_FROM 등)

        정렬 조건 설명:
        존재하는 데이터와 별개로 다음 정렬 조건을 제시한다.
        1. 제 1 우선순위 정렬      :   평균 점수 내림차순
        2. 제 2 우선순위 정렬      :   설정된 보상 개수 오름차순
        3. 제 3 우선순위 정렬      :   문제 플레이 횟수 내림차순
        4. 제 4 우선순위 정렬      :   생성일 오름차순
        5. 우선순위 제공 X 정렬     :   문제 시나리오 개수 내림차순
         */

        int dummyDataSize = 5;
        List<Problem> dummyProblems1 = this.prepareDummyData(dummyDataSize);
        List<Problem> dummyProblems2 = this.prepareDummyData(dummyDataSize);

        int coreDataSize = 10;
        Long coreUserId = data.createUser().getId();
        List<Problem> coreProblems = this.prepareCoreData(coreDataSize, coreUserId);

        int randomIter = 5;
        List<Problem> coreDummyProblems = this.prepareCoreDummyData(coreUserId, randomIter);

        List<ProblemFilter<?>> problemFilters = createProblemFilters(coreUserId);
        List<ProblemOrder> problemOrders = this.createProblemOrders();
        Pageable pageable = PageRequest.of(0, coreDataSize);

        Page<Problem> response = dynamicSearchRepo.searchNonSoftDeletedPublicProblemWithFilters(
                problemFilters, problemOrders, pageable
        );

        assertThat(response).isNotNull();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.getTotalElements()).isEqualTo(coreDataSize);

        List<Problem> responseContent = response.getContent();

        Utils.assertAContainsAllB(responseContent, coreProblems, Problem::getId);
        Utils.assertADoesNotContainAnyOfOthers(
                responseContent, Problem::getId,
                dummyProblems1, dummyProblems2, coreDummyProblems
        );

        Comparator<Problem> expectedOrdering = expectedOrdering();

        assertThat(responseContent).isSortedAccordingTo(expectedOrdering);

        // 한번에 테스트 성공했네 지렸다 ㄹㅇㅋㅋ
    }

    private List<Problem> prepareDummyData(
            int dummySize
    ) {
        log.info("Preparing dummy data");

        List<Problem> problems = new ArrayList<>(dummySize);

        Long dummyUserId = data.createUser().getId();

        for (int i = 0; i < dummySize; i++) {
            String title = String.format(
                    "[u=%d] Dummy title: [%d]",
                    dummyUserId, i
            );
            boolean isPublic = (i & 0b1) == 0b1;

            LocalDateTime randomCreatedTime = Utils.getRandom(
                    CT_FROM, CT_TO
            );
            long randomRatingAvgScore = Utils.getRandom(
                    (long) DOUBLE_FROM, (long) DOUBLE_TO
            );
            long randomRatingNum = Utils.getRandom(LONG_FROM, LONG_TO);
            long randomTotalPlayNum = Utils.getRandom(LONG_FROM, LONG_TO);
            int randomScenarioSetNum = Utils.getRandom(INT_FROM, INT_TO);
            int randomRewardSetNum = Utils.getRandom(INT_FROM, INT_TO);

            AggregatedProblemRatingInfo ratingInfo = new AggregatedProblemRatingInfo(
                    randomRatingNum, randomRatingAvgScore * randomRatingNum
            );
            AggregatedProblemPlayInfo playInfo = new AggregatedProblemPlayInfo(
                    randomTotalPlayNum
            );

            when(auditingProvider.provideLocalDateTime())
                    .thenReturn(randomCreatedTime);

            Problem dummpyProblem = data.createNewProblemWithAggregation(
                    dummyUserId, title, isPublic,
                    randomScenarioSetNum, randomRewardSetNum,
                    ratingInfo, playInfo
            );

            problems.add(dummpyProblem);
        }

        if (problems.size() != dummySize) {
            throw new AssertionError();
        }

        log.info("Dummpy data has been prepared");

        return problems;
    }

    private List<Problem> prepareCoreData(
            int dataSize, Long userId
    ) {
        log.info("Preparing core data");

        List<Problem> problems = new ArrayList<>(dataSize);

        for (int i = 0; i < dataSize; i++) {
            String title = String.format("Title-%d", i);

            LocalDateTime randomCreatedTime = Utils.getRandom(
                    CT_FROM, CT_TO
            );
            long randomRatingAvgScore = Utils.getRandom(
                    (long) DOUBLE_FROM, (long) DOUBLE_TO
            );
            long randomRatingNum = Utils.getRandom(LONG_FROM, LONG_TO);
            long randomTotalPlayNum = Utils.getRandom(LONG_FROM, LONG_TO);

            int randomScenarioSetNum = Utils.getRandom(INT_FROM, INT_TO);
            int randomRewardSetNum = Utils.getRandom(INT_FROM, INT_TO);

            AggregatedProblemRatingInfo ratingInfo = new AggregatedProblemRatingInfo(
                    randomRatingNum, randomRatingAvgScore * randomRatingNum
            );
            AggregatedProblemPlayInfo playInfo = new AggregatedProblemPlayInfo(
                    randomTotalPlayNum
            );

            when(auditingProvider.provideLocalDateTime())
                    .thenReturn(randomCreatedTime);

            Problem dummpyProblem = data.createNewProblemWithAggregation(
                    userId, title, true,
                    randomScenarioSetNum, randomRewardSetNum,
                    ratingInfo, playInfo
            );

            problems.add(dummpyProblem);
        }

        if (problems.size() != dataSize) {
            throw new AssertionError();
        }

        log.info("Core data has been prepared");

        return problems;
    }

    private List<Problem> prepareCoreDummyData(
            Long userId, int randomGenIter
    ) {
        log.info("Preparing core-dummy data");

        List<Problem> problems = new ArrayList<>();

        for (int iter = 0; iter < randomGenIter; iter++) {

            String title1 = String.format("Title-[1]-%d", iter);
            String title2 = String.format("Title-[2]-%d", iter);

            LocalDateTime randomBeforeCtFrom = Utils.getRandomMaxExclusive(
                    LocalDate.MIN, CT_FROM
            );
            LocalDateTime randomAfterCtTo = Utils.getRandomMinExclusive(
                    CT_TO, LocalDate.MAX
            );

            long randomLowerDoubleFrom = Utils.getRandomMaxExclusive(
                    0, (long) DOUBLE_FROM
            );
            long randomUpperDoubleTo = Utils.getRandomMinExclusive(
                    (long) DOUBLE_TO, Long.MAX_VALUE - 1
            );

            long randomLowerLongFrom = Utils.getRandomMaxExclusive(
                    Long.MIN_VALUE, LONG_FROM
            );
            long randomUpperLongTo = Utils.getRandomMinExclusive(
                    LONG_TO, Long.MAX_VALUE - 1
            );

            int randomLowerIntFrom = Utils.getRandomMaxExclusive(
                    Integer.MIN_VALUE, INT_FROM
            );
            int randomUpperIntFrom = Utils.getRandomMinExclusive(
                    INT_TO, Integer.MAX_VALUE - 1
            );

            AggregatedProblemRatingInfo ratingInfo1 = new AggregatedProblemRatingInfo(
                    randomLowerLongFrom, randomLowerDoubleFrom * randomLowerLongFrom
            );
            AggregatedProblemRatingInfo ratingInfo2 = new AggregatedProblemRatingInfo(
                    randomUpperLongTo, randomUpperDoubleTo * randomUpperLongTo
            );

            AggregatedProblemPlayInfo playInfo1 = new AggregatedProblemPlayInfo(
                    randomLowerLongFrom
            );
            AggregatedProblemPlayInfo playInfo2 = new AggregatedProblemPlayInfo(
                    randomUpperLongTo
            );

            when(auditingProvider.provideLocalDateTime())
                    .thenReturn(randomBeforeCtFrom)
                    .thenReturn(randomAfterCtTo);

            Problem p1 = data.createNewProblemWithAggregation(
                    userId, title1, true,
                    randomLowerIntFrom, randomLowerIntFrom,
                    ratingInfo1, playInfo1
            );
            Problem p2 = data.createNewProblemWithAggregation(
                    userId, title2, true,
                    randomUpperIntFrom, randomUpperIntFrom,
                    ratingInfo2, playInfo2
            );

            problems.add(p1);
            problems.add(p2);

        }

        if (problems.isEmpty()) {
            throw new AssertionError();
        }

        log.info("Core-dummy data has been prepared");

        return problems;
    }

    private List<ProblemOrder> createProblemOrders() {
        Map<ProblemOrderType, AbstractOrderRequestAdaptor> adaptorMap = orderRequestAdaptors.stream()
                .collect(Collectors.toMap(
                        OrderRequestAdaptingStrategy::handleableOrderType,
                        Function.identity()
                ));

        List<ProblemOrderType> orderTypes = List.of(
                ProblemOrderType.RATING_AVG_DESC,
                ProblemOrderType.NUM_OF_REWARDS_ASC,
                ProblemOrderType.NUM_OF_PLAYS_DESC,
                ProblemOrderType.CREATED_TIME_ASC,
                ProblemOrderType.NUM_OF_PROBLEM_SCENARIOS_DESC
        );

        return orderTypes.stream().map(t -> {
                    OrderingRequest req = new OrderingRequest(t, null);
                    return adaptorMap.get(t).toOrder(req);
                })
                .toList();
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

        Problem createNewProblemWithAggregation(
                Long userId, String title, boolean isPublic,
                int numOftotalScenarios, int numOfRewardSets,
                AggregatedProblemRatingInfo ratingInfo,
                AggregatedProblemPlayInfo playInfo
        ) {
            Problem problem = initializer.problemBuilder()
                    .userId(userId)
                    .title(title)
                    .visibility(isPublic ?
                            ProblemVisibility.PUBLIC :
                            ProblemVisibility.PRIVATE)
                    .numOfTotalScenarios(numOftotalScenarios)
                    .serializedScenarioInfo("hi")
                    .numOfRewardSets(numOfRewardSets)
                    .build();

            initializer.problemAggregationBuilder()
                    .problemId(problem.getId())
                    .ratingInfo(ratingInfo)
                    .playInfo(playInfo)
                    .build();

            return problem;
        }

        void initAll() {
            initializer.initAll();
        }
    }
}
