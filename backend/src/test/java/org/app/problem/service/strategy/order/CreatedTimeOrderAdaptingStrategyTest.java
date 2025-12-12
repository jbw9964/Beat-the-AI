package org.app.problem.service.strategy.order;

import static org.mockito.Mockito.*;

import com.querydsl.core.types.*;
import com.querydsl.jpa.impl.*;
import java.time.*;
import java.util.*;
import lombok.extern.slf4j.*;
import org.*;
import org.app.config.jpa.*;
import org.app.entity.*;
import org.app.problem.domain.search.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.transaction.annotation.*;
import org.support.*;

@Slf4j
@Import(CreatedTimeOrderAdaptingStrategyTest.DataInitFacade.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class CreatedTimeOrderAdaptingStrategyTest extends IntegrationTestSupport {

    @Autowired
    CreatedTimeAscOrderAdaptingStrategy ascendingStrategy;

    @Autowired
    CreatedTimeDescOrderAdaptingStrategy descendingStrategy;

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
    @DisplayName("문제 생성 오름차순 정렬 검색이 정상 작동한다.")
    void testAscendingOrder() {
        int testSize = 10;

        OrderSpecifier<?> order = Utils.assertProblemOderAndOrderSpecifierNonNullAndGet(
                ascendingStrategy
        );

        this.prepareData(testSize);

        List<Problem> searchResult = this.executeQuery(order);

        // 1. key 를 빼낸다.
        // 2. key 를 비교할 방법을 명시한다. 그런데 null last 로 명시했다.
        // 3. null last 는 이름 그대로 null 은 마지막 (비교 대상 둘다 null 이면 equal 하게 취급함),
        //    null 이 아닐때는 제공한 comparator 로 비교한다.
        // 4. 그런데 LocaDateTime 에 대해 naturalOrder 는 그냥 오름차순이다. 그래서 잘 작동한다.
        Comparator<Problem> ctAscAndNullLast = Comparator.comparing(
                Problem::getCreatedAt,      // key extract
                Comparator.nullsLast(       // key comparator (if key is null, then last. else below comparator)
                        Comparator.naturalOrder()
                )
        );

        Utils.assertListHasSizeAndSortedAccordingToOrder(
                testSize, searchResult, ctAscAndNullLast
        );
    }

    @Test
    @DisplayName("문제 생성 내림차순 정렬 검색이 정상 작동한다.")
    void testDecendingOrder() {
        int testSize = 10;

        OrderSpecifier<?> order = Utils.assertProblemOderAndOrderSpecifierNonNullAndGet(
                descendingStrategy
        );

        this.prepareData(testSize);

        List<Problem> searchResult = this.executeQuery(order);

        Comparator<Problem> ctDescAndNullLast = Comparator.comparing(
                Problem::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())
        );

        Utils.assertListHasSizeAndSortedAccordingToOrder(
                testSize, searchResult, ctDescAndNullLast
        );
    }

    private void prepareData(int testSize) {
        log.info("Preparing data");

        Long testUserId = data.createUser().getId();

        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < testSize; i++) {
            String title = String.format("Title %d", i);

            LocalDateTime mockedCreatedAt = (i & 0b1) == 1 ?
                    now.minusMonths(i) : now.plusMonths(i);
            when(auditingProvider.provideLocalDateTime()).thenReturn(mockedCreatedAt);

            data.createNewProblem(testUserId, title);
        }

        log.info("Data has been prepared.");
    }

    private List<Problem> executeQuery(OrderSpecifier<?> orderSpecifier) {
        log.info("Executing query");

        List<Problem> result = queryFactory.selectFrom(
                        QProblemPaths.QPROBLEM_ROOT
                )
                .leftJoin(
                        QProblemPaths.QP__QPROBLEM_AGGREGATION_TARGET,
                        QProblemPaths.QPROBLEM_AGGREGATION_ROOT
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

        void createNewProblem(Long userId, String title) {
            initializer.problemBuilder()
                    .userId(userId)
                    .title(title)
                    .visibility(ProblemVisibility.PUBLIC)
                    .build();
        }

        void initAll() {
            initializer.initAll();
        }
    }
}