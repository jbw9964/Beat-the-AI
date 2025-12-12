package org.app.problem.service.strategy.order;

import com.querydsl.core.types.*;
import com.querydsl.jpa.impl.*;
import java.util.*;
import lombok.extern.slf4j.*;
import org.*;
import org.app.entity.*;
import org.app.problem.domain.search.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import org.support.*;

@Slf4j
@Import(NumOfRewardsOrderStrategyTest.DataInitFacade.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class NumOfRewardsOrderStrategyTest extends IntegrationTestSupport {

    @Autowired
    NumOfRewardsAscOrderStrategy ascendingStrategy;

    @Autowired
    NumOfRewardsDescOrderStrategy descendingStrategy;

    @Autowired
    DataInitFacade data;

    @Autowired
    JPAQueryFactory queryFactory;

    @AfterEach
    void tearDown() {
        data.initAll();
    }

    @Test
    @DisplayName("문제 보상 개수 오름차순 정렬 검색이 정상 작동한다.")
    void testAscendingOrder() {
        int testSize = 30;

        OrderSpecifier<?> order = Utils.assertProblemOderAndOrderSpecifierNonNullAndGet(
                ascendingStrategy
        );

        this.prepareData(testSize);

        List<Problem> searchResult = this.executeQuery(order);

        Comparator<Problem> norAscAndNullLast = Comparator.comparing(
                p -> p.getProblemAggregation().getProblemInfo().getNumOfRewardSet(),
                Comparator.nullsLast(Comparator.naturalOrder())
        );

        Utils.assertListHasSizeAndSortedAccordingToOrder(
                testSize, searchResult, norAscAndNullLast
        );
    }

    @Test
    @DisplayName("문제 보상 개수 내림차순 정렬 검색이 정상 작동한다.")
    void testDecendingOrder() {
        int testSize = 30;

        OrderSpecifier<?> order = Utils.assertProblemOderAndOrderSpecifierNonNullAndGet(
                descendingStrategy
        );

        this.prepareData(testSize);

        List<Problem> searchResult = this.executeQuery(order);

        Comparator<Problem> norDescAndNullLast = Comparator.comparing(
                p -> p.getProblemAggregation().getProblemInfo().getNumOfRewardSet(),
                Comparator.nullsLast(Comparator.reverseOrder())
        );

        Utils.assertListHasSizeAndSortedAccordingToOrder(
                testSize, searchResult, norDescAndNullLast
        );
    }

    private void prepareData(int testSize) {
        log.info("Preparing data");

        Long testUserId = data.createUser().getId();

        for (int i = 0; i < testSize; i++) {
            String title = String.format("Title %d", i);
            AggregatedProblemInfo problemInfo = new AggregatedProblemInfo(i, 0);
            data.createNewProblemWithPlayInfo(testUserId, title, problemInfo);
        }

        log.info("Data has been prepared.");
    }

    private List<Problem> executeQuery(OrderSpecifier<?> orderSpecifier) {
        log.info("Executing query");

        List<Problem> result = queryFactory.selectFrom(
                        QProblemExpressions.QPROBLEM_ROOT
                )
                .leftJoin(
                        QProblemExpressions.QP__QPROBLEM_AGGREGATION_TARGET,
                        QProblemExpressions.QPROBLEM_AGGREGATION_ROOT
                )
                .fetchJoin()
                .orderBy(orderSpecifier)
                .fetch();

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

        void createNewProblemWithPlayInfo(
                Long userId, String title,
                AggregatedProblemInfo problemInfo
        ) {
            Long problemId = initializer.problemBuilder()
                    .userId(userId)
                    .title(title)
                    .visibility(ProblemVisibility.PUBLIC)
                    .build()
                    .getId();

            initializer.problemAggregationBuilder()
                    .problemId(problemId)
                    .problemInfo(problemInfo)
                    .build();
        }

        void initAll() {
            initializer.initAll();
        }
    }
}