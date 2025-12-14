package org.app.problem.repository.strategy;

import com.querydsl.core.*;
import com.querydsl.jpa.impl.*;
import java.util.*;
import lombok.extern.slf4j.*;
import org.*;
import org.app.entity.*;
import org.app.problem.domain.search.*;
import org.app.problem.domain.search.filter.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import org.support.*;

@Slf4j
@Import(NumOfRatingsFilterClausesBuilderStrategyTest.DataInitFacade.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class NumOfRatingsFilterClausesBuilderStrategyTest extends IntegrationTestSupport {

    @Autowired
    NumOfRatingsFilterClausesBuilderStrategy strategy;

    @Autowired
    DataInitFacade data;

    @Autowired
    JPAQueryFactory queryFactory;

    @AfterEach
    void tearDown() {
        data.initAll();
    }


    @Test
    @DisplayName("두 평가 개수 사이 존재하는 문제를 검색할 수 있다.")
    void testBoundedCase() {
        long l10 = 10L, l20 = 20L;
        long l30 = 30L, l40 = 40L;

        int range1Size = 5;
        int range2Size = 20;

        List<Problem> p10to20s = this.prepareData(range1Size, l10, l20);
        List<Problem> p30to40s = this.prepareData(range2Size, l30, l40);

        ProblemFilter<?> between1020Filter = Utils.genProblemFilter(
                strategy, l10, l20, null
        );
        ProblemFilter<?> between3040Filter = Utils.genProblemFilter(
                strategy, l30, l40, null
        );

        BooleanBuilder between1020 = new BooleanBuilder();
        BooleanBuilder between3040 = new BooleanBuilder();
        strategy.addFilterClauses(between1020, between1020Filter);
        strategy.addFilterClauses(between3040, between3040Filter);

        List<Problem> searchResult1 = this.executeQuery(between1020);
        List<Problem> searchResult2 = this.executeQuery(between3040);

        Utils.assertAContainsAllB(searchResult1, p10to20s, Problem::getId);
        Utils.assertADoesNotContainsAnyB(searchResult1, p30to40s, Problem::getId);
        Utils.assertAllElementsSatisfies(
                searchResult1, p -> {
                    long n = p.getProblemAggregation()
                            .getRatingInfo()
                            .getNumOfTotalRatings();

                    return l10 <= n && n <= l20;
                }
        );

        Utils.assertAContainsAllB(searchResult2, p30to40s, Problem::getId);
        Utils.assertADoesNotContainsAnyB(searchResult2, p10to20s, Problem::getId);
        Utils.assertAllElementsSatisfies(
                searchResult1, p -> {
                    long n = p.getProblemAggregation()
                            .getRatingInfo()
                            .getNumOfTotalRatings();

                    return l30 <= n && n <= l40;
                }
        );
    }

    @Test
    @DisplayName("어느 평가 개수 기준 존재하는 문제를 검색할 수 있다.")
    void testUnboundedCase() {
        long threshold = 50;

        int lowerSize = 20;
        int upperSize = 15;

        List<Problem> pls = this.prepareData(lowerSize, 0L, threshold - 1);
        List<Problem> pus = this.prepareData(upperSize, threshold + 1, 100L);

        ProblemFilter<?> lowerFilter = Utils.genProblemFilter(
                strategy, null, threshold, null
        );
        ProblemFilter<?> upperFilter = Utils.genProblemFilter(
                strategy, threshold, null, null
        );

        BooleanBuilder lower = new BooleanBuilder();
        BooleanBuilder upper = new BooleanBuilder();
        strategy.addFilterClauses(lower, lowerFilter);
        strategy.addFilterClauses(upper, upperFilter);

        List<Problem> searchResult1 = this.executeQuery(lower);
        List<Problem> searchResult2 = this.executeQuery(upper);

        Utils.assertAContainsAllB(searchResult1, pls, Problem::getId);
        Utils.assertADoesNotContainsAnyB(searchResult1, pus, Problem::getId);
        Utils.assertAllElementsSatisfies(
                searchResult1, p -> p.getProblemAggregation()
                                            .getRatingInfo()
                                            .getNumOfTotalRatings() <= threshold
        );

        Utils.assertAContainsAllB(searchResult2, pus, Problem::getId);
        Utils.assertADoesNotContainsAnyB(searchResult2, pls, Problem::getId);
        Utils.assertAllElementsSatisfies(
                searchResult2, p -> p.getProblemAggregation()
                                            .getRatingInfo()
                                            .getNumOfTotalRatings() >= threshold
        );
    }

    private List<Problem> prepareData(
            int testSize, long minInclusive, long maxInclusive
    ) {
        log.info("Preparing data");

        List<Long> randoms = Utils.createRandomLongsInclusive(
                testSize, minInclusive, maxInclusive
        );

        List<Problem> problems = new ArrayList<>(testSize);

        Long testUserId = data.createUser().getId();

        for (int i = 0; i < testSize; i++) {
            String title = String.format("Title %d", i);
            Long rand = randoms.get(i);
            AggregatedProblemRatingInfo ratingInfo = new AggregatedProblemRatingInfo(
                    rand, 0
            );
            problems.add(data.createNewProblemWithPlayInfo(
                    testUserId, title, ratingInfo
            ));
        }

        log.info("Data has been prepared.");

        return problems;
    }

    private List<Problem> executeQuery(BooleanBuilder booleanBuilder) {
        log.info("Executing query");

        List<Problem> result = queryFactory.selectFrom(
                        QProblemPaths.QPROBLEM_ROOT
                )
                .leftJoin(
                        QProblemPaths.QP__QPROBLEM_AGGREGATION_TARGET,
                        QProblemPaths.QPROBLEM_AGGREGATION_ROOT
                )
                .fetchJoin()
                .where(booleanBuilder)
                .fetch();

        log.info("Used boolean builder: {}", booleanBuilder);

        log.info("Query has been executed");

        return result;
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

        Problem createNewProblemWithPlayInfo(
                Long userId, String title,
                AggregatedProblemRatingInfo ratingInfo
        ) {
            Problem problem = initializer.problemBuilder()
                    .userId(userId)
                    .title(title)
                    .visibility(ProblemVisibility.PUBLIC)
                    .build();

            initializer.problemAggregationBuilder()
                    .problemId(problem.getId())
                    .ratingInfo(ratingInfo)
                    .build();

            return problem;
        }

        void initAll() {
            initializer.initAll();
        }
    }
}