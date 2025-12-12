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
@Import(RatingAvgOrderStrategyTest.DataInitFacade.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class RatingAvgOrderStrategyTest extends IntegrationTestSupport {

    @Autowired
    RatingAvgAscOrderStrategy ascendingStrategy;

    @Autowired
    RatingAvgDescOrderStrategy descendingStrategy;

    @Autowired
    DataInitFacade data;

    @Autowired
    JPAQueryFactory queryFactory;

    @AfterEach
    void tearDown() {
        data.initAll();
    }

    @Test
    @DisplayName("문제 평가 평균 점수 오름차순 정렬 검색이 정상 작동한다.")
    void testAscendingOrder() {
        int testSize = 30;

        OrderSpecifier<?> order = Utils.assertProblemOderAndOrderSpecifierNonNullAndGet(
                ascendingStrategy
        );

        this.prepareData(testSize);

        List<Problem> searchResult = this.executeQuery(order);

        Comparator<Problem> raAscAndNullLast = Comparator.comparing(
                p -> p.getProblemAggregation().getRatingInfo().getRatingAverage(),
                Comparator.nullsLast(Comparator.naturalOrder())
        );

        Utils.assertListHasSizeAndSortedAccordingToOrder(
                testSize, searchResult, raAscAndNullLast
        );
    }

    @Test
    @DisplayName("문제 평가 평균 점수 내림차순 정렬 검색이 정상 작동한다.")
    void testDecendingOrder() {
        int testSize = 30;

        OrderSpecifier<?> order = Utils.assertProblemOderAndOrderSpecifierNonNullAndGet(
                descendingStrategy
        );

        this.prepareData(testSize);

        List<Problem> searchResult = this.executeQuery(order);

        Comparator<Problem> raDescAndNullLast = Comparator.comparing(
                p -> p.getProblemAggregation().getRatingInfo().getRatingAverage(),
                Comparator.nullsLast(Comparator.reverseOrder())
        );

        Utils.assertListHasSizeAndSortedAccordingToOrder(
                testSize, searchResult, raDescAndNullLast
        );
    }

    private void prepareData(int testSize) {
        log.info("Preparing data");

        Long testUserId = data.createUser().getId();

        for (int i = 0; i < testSize; i++) {
            String title = String.format("Title %d", i);
            AggregatedProblemRatingInfo ratingInfo = new AggregatedProblemRatingInfo(
                    i, (long) i * i
            );
            data.createNewProblemWithPlayInfo(testUserId, title, ratingInfo);
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
                AggregatedProblemRatingInfo ratingInfo
        ) {
            Long problemId = initializer.problemBuilder()
                    .userId(userId)
                    .title(title)
                    .visibility(ProblemVisibility.PUBLIC)
                    .build()
                    .getId();

            initializer.problemAggregationBuilder()
                    .problemId(problemId)
                    .ratingInfo(ratingInfo)
                    .build();
        }

        void initAll() {
            initializer.initAll();
        }
    }
}