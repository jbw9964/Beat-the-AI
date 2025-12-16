package org.app.problem.repository.strategy;

import static org.mockito.Mockito.*;

import com.querydsl.core.*;
import com.querydsl.jpa.impl.*;
import java.time.*;
import java.util.*;
import lombok.extern.slf4j.*;
import org.*;
import org.app.config.jpa.*;
import org.app.entity.*;
import org.app.problem.domain.search.*;
import org.app.problem.domain.search.filter.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.transaction.annotation.*;
import org.support.*;

@Slf4j
@Import(CreatedDateFilterClausesBuilderStrategyTest.DataInitFacade.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class CreatedDateFilterClausesBuilderStrategyTest extends IntegrationTestSupport {

    private static final LocalDate TODAY = LocalDate.now();

    @Autowired
    CreatedDateFilterClausesBuilderStrategy strategy;

    @Autowired
    DataInitFacade data;

    @Autowired
    JPAQueryFactory queryFactory;

    @MockitoSpyBean
    EntityAuditingProvider auditingProvider;

    @AfterEach
    void tearDown() {
        data.initAll();
    }

    @Test
    @DisplayName("두 날짜 사이 생성된 문제를 검색할 수 있다.")
    void testBoundedCase() {
        LocalDate afterM1 = TODAY.plusMonths(1);
        LocalDate afterM2 = TODAY.plusMonths(2);
        LocalDate afterM3 = TODAY.plusMonths(3);
        LocalDate afterM4 = TODAY.plusMonths(4);

        int range1Size = 10;
        int range2Size = 15;

        List<Problem> m1m2ps = this.prepareData(range1Size, afterM1, afterM2);
        List<Problem> m3m4ps = this.prepareData(range2Size, afterM3, afterM4);

        ProblemFilter<?> betweenM1M2Filter = Utils.genProblemFilter(
                strategy, afterM1, afterM2, null
        );
        ProblemFilter<LocalDate> betweenM3M4Filter = Utils.genProblemFilter(
                strategy, afterM3, afterM4, null
        );

        BooleanBuilder betweenM1M2 = new BooleanBuilder();
        BooleanBuilder betweenM3M4 = new BooleanBuilder();
        strategy.addFilterClauses(betweenM1M2, betweenM1M2Filter);
        strategy.addFilterClauses(betweenM3M4, betweenM3M4Filter);

        List<Problem> searchResult1 = this.executeQuery(betweenM1M2);
        List<Problem> searchResult2 = this.executeQuery(betweenM3M4);

        Utils.assertAContainsAllB(searchResult1, m1m2ps, Problem::getId);
        Utils.assertADoesNotContainsAnyB(searchResult1, m3m4ps, Problem::getId);
        Utils.assertAllElementsSatisfies(
                searchResult1, p ->
                        !p.getCreatedAt().isBefore(afterM1.atTime(LocalTime.MIN)) &&
                        !p.getCreatedAt().isAfter(afterM2.atTime(LocalTime.MAX))
        );

        Utils.assertAContainsAllB(searchResult2, m3m4ps, Problem::getId);
        Utils.assertADoesNotContainsAnyB(searchResult2, m1m2ps, Problem::getId);
        Utils.assertAllElementsSatisfies(
                searchResult2, p ->
                        !p.getCreatedAt().isBefore(afterM3.atTime(LocalTime.MIN)) &&
                        !p.getCreatedAt().isAfter(afterM4.atTime(LocalTime.MAX))
        );
    }

    @Test
    @DisplayName("어느 날짜 기준 생성된 문제를 검색할 수 있다.")
    void testUnboundedCase() {
        LocalDate thresholdDate = TODAY;

        LocalDate afterM1 = thresholdDate.plusMonths(1);
        LocalDate afterM2 = thresholdDate.plusMonths(2);
        LocalDate beforeM2 = thresholdDate.minusMonths(2);
        LocalDate beforeM1 = thresholdDate.minusMonths(1);

        int range1Size = 10;
        int range2Size = 15;

        List<Problem> aps = this.prepareData(range1Size, afterM1, afterM2);
        List<Problem> bps = this.prepareData(range2Size, beforeM2, beforeM1);

        ProblemFilter<?> afterTFilter = Utils.genProblemFilter(
                strategy, thresholdDate, null, null
        );
        ProblemFilter<LocalDate> beforeTFilter = Utils.genProblemFilter(
                strategy, null, thresholdDate, null
        );

        BooleanBuilder afterT = new BooleanBuilder();
        BooleanBuilder beforeT = new BooleanBuilder();
        strategy.addFilterClauses(afterT, afterTFilter);
        strategy.addFilterClauses(beforeT, beforeTFilter);

        List<Problem> searchResult1 = this.executeQuery(afterT);
        List<Problem> searchResult2 = this.executeQuery(beforeT);

        Utils.assertAContainsAllB(searchResult1, aps, Problem::getId);
        Utils.assertADoesNotContainsAnyB(searchResult1, bps, Problem::getId);
        Utils.assertAllElementsSatisfies(searchResult1,
                p -> !p.getCreatedAt().isBefore(thresholdDate.atTime(LocalTime.MIN)));

        Utils.assertAContainsAllB(searchResult2, bps, Problem::getId);
        Utils.assertADoesNotContainsAnyB(searchResult2, aps, Problem::getId);
        Utils.assertAllElementsSatisfies(searchResult2,
                p -> !p.getCreatedAt().isAfter(thresholdDate.atTime(LocalTime.MAX)));
    }

    private List<Problem> prepareData(
            int testSize, LocalDate minInclusive, LocalDate maxInclusive
    ) {
        log.info("Preparing data");

        List<LocalDate> randoms = Utils.createRandomLocalDatesInclusive(
                testSize, minInclusive, maxInclusive
        );

        List<Problem> problems = new ArrayList<>(testSize);

        Long testUserId = data.createUser().getId();

        for (int i = 0; i < randoms.size(); i++) {
            String title = String.format("Title %d", i);

            LocalDate randomDate = randoms.get(i);
            LocalDateTime mockedCreatedAt;

            if (randomDate.equals(minInclusive)) {
                mockedCreatedAt = randomDate.atTime(LocalTime.MIN)
                        .plusSeconds(5L);
            } else if (randomDate.equals(maxInclusive)) {
                mockedCreatedAt = randomDate.atTime(LocalTime.MAX)
                        .minusSeconds(5L);
            } else {
                mockedCreatedAt = (i & 0b1) == 0b1 ?
                        randomDate.atTime(LocalTime.MIN) :
                        randomDate.atTime(LocalTime.MAX);
            }

            when(auditingProvider.provideLocalDateTime())
                    .thenReturn(mockedCreatedAt);

            problems.add(data.createNewProblem(testUserId, title));
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

        Problem createNewProblem(Long userId, String title) {
            return initializer.problemBuilder()
                    .userId(userId)
                    .title(title)
                    .visibility(ProblemVisibility.PUBLIC)
                    .serializedScenarioInfo("hi")
                    .build();
        }

        void initAll() {
            initializer.initAll();
        }
    }
}