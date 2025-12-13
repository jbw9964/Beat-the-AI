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
@Import(NumOfProblemScenariosFilterClausesBuilderStrategyTest.DataInitFacade.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class NumOfProblemScenariosFilterClausesBuilderStrategyTest
        extends IntegrationTestSupport {

    @Autowired
    NumOfProblemScenariosFilterClausesBuilderStrategy strategy;

    @Autowired
    DataInitFacade data;

    @Autowired
    JPAQueryFactory queryFactory;

    @AfterEach
    void tearDown() {
        data.initAll();
    }


    @Test
    @DisplayName("두 시나리오 개수 사이 생성된 문제를 검색할 수 있다.")
    void testBoundedCase() {
        int i10 = 10, i20 = 20;
        int i30 = 30, i40 = 40;

        int range1Size = 15;
        int range2Size = 20;

        List<Problem> p10to20s = this.prepareData(range1Size, i10, i20);
        List<Problem> p30to40s = this.prepareData(range2Size, i30, i40);

        ProblemFilter<?> between1020Filter = Utils.genProblemFilter(
                strategy, i10, i20, null
        );
        ProblemFilter<?> between3040Filter = Utils.genProblemFilter(
                strategy, i30, i40, null
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
                    int n = p.getProblemAggregation()
                            .getProblemInfo()
                            .getNumOfScenarioSet();

                    return i10 <= n && n <= i20;
                }
        );

        Utils.assertAContainsAllB(searchResult2, p30to40s, Problem::getId);
        Utils.assertADoesNotContainsAnyB(searchResult2, p10to20s, Problem::getId);
        Utils.assertAllElementsSatisfies(
                searchResult2, p -> {
                    int n = p.getProblemAggregation()
                            .getProblemInfo()
                            .getNumOfScenarioSet();

                    return i30 <= n && n <= i40;
                }
        );
    }

    @Test
    @DisplayName("어느 시나리오 개수 기준 생성된 문제를 검색할 수 있다.")
    void testUnboundedCase() {
        int threshold = 5;

        int lowerSize = 3;
        int upperSize = 15;

        List<Problem> pls = this.prepareData(lowerSize, 0, threshold - 1);
        List<Problem> pus = this.prepareData(upperSize, threshold + 1, 15);

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
                                            .getProblemInfo()
                                            .getNumOfScenarioSet() <= threshold
        );

        Utils.assertAContainsAllB(searchResult2, pus, Problem::getId);
        Utils.assertADoesNotContainsAnyB(searchResult2, pls, Problem::getId);
        Utils.assertAllElementsSatisfies(
                searchResult2, p -> p.getProblemAggregation()
                                            .getProblemInfo()
                                            .getNumOfScenarioSet() >= threshold
        );
    }

    private List<Problem> prepareData(
            int testSize, int minInclusive, int maxInclusive
    ) {
        log.info("Preparing data");

        List<Integer> randoms = Utils.creatRandomIntsInclusive(
                testSize, minInclusive, maxInclusive
        );

        List<Problem> problems = new ArrayList<>(testSize);

        Long testUserId = data.createUser().getId();

        for (int i = 0; i < testSize; i++) {
            String title = String.format("Title %d", i);
            Integer rand = randoms.get(i);
            AggregatedProblemInfo playInfo = new AggregatedProblemInfo(0, rand);
            problems.add(data.createNewProblemWithPlayInfo(
                    testUserId, title, playInfo
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
                AggregatedProblemInfo problemInfo
        ) {
            Problem problem = initializer.problemBuilder()
                    .userId(userId)
                    .title(title)
                    .visibility(ProblemVisibility.PUBLIC)
                    .build();

            initializer.problemAggregationBuilder()
                    .problemId(problem.getId())
                    .problemInfo(problemInfo)
                    .build();

            return problem;
        }

        void initAll() {
            initializer.initAll();
        }
    }

}