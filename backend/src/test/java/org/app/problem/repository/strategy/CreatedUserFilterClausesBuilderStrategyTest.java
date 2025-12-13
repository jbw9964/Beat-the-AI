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
@Import(CreatedUserFilterClausesBuilderStrategyTest.DataInitFacade.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class CreatedUserFilterClausesBuilderStrategyTest extends IntegrationTestSupport {

    @Autowired
    CreatedUserFilterClausesBuilderStrategy strategy;

    @Autowired
    DataInitFacade data;

    @Autowired
    JPAQueryFactory queryFactory;

    @AfterEach
    void tearDown() {
        data.initAll();
    }

    @Test
    @DisplayName("특정 유저가 생성한 문제를 검색할 수 있다.")
    void test() {
        User testUser1 = data.createUser();
        User testUser2 = data.createUser();

        int testSize1 = 10;
        int testSize2 = 20;

        List<Problem> p1s = this.prepareData(testSize1, testUser1.getId());
        List<Problem> p2s = this.prepareData(testSize2, testUser2.getId());

        ProblemFilter<?> filter1 = Utils.genProblemFilter(
                strategy, null, null, testUser1.getId()
        );
        ProblemFilter<?> filter2 = Utils.genProblemFilter(
                strategy, null, null, testUser2.getId()
        );

        BooleanBuilder builder1 = new BooleanBuilder();
        BooleanBuilder builder2 = new BooleanBuilder();
        strategy.addFilterClauses(builder1, filter1);
        strategy.addFilterClauses(builder2, filter2);

        List<Problem> searchResult1 = this.executeQuery(builder1);
        List<Problem> searchResult2 = this.executeQuery(builder2);

        Utils.assertAContainsAllB(searchResult1, p1s, Problem::getId);
        Utils.assertADoesNotContainsAnyB(searchResult1, p2s, Problem::getId);
        Utils.assertAllElementsSatisfies(
                searchResult1, p -> p.getUser().getId().equals(testUser1.getId())
        );

        Utils.assertAContainsAllB(searchResult2, p2s, Problem::getId);
        Utils.assertADoesNotContainsAnyB(searchResult2, p1s, Problem::getId);
        Utils.assertAllElementsSatisfies(
                searchResult2, p -> p.getUser().getId().equals(testUser2.getId())
        );
    }

    private List<Problem> prepareData(int testSize, Long userId) {

        log.info("Preparing data");

        List<Problem> problems = new ArrayList<>(testSize);

        for (int i = 0; i < testSize; i++) {
            String title = String.format("Title %d", i);
            problems.add(data.createNewProblem(userId, title));
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
                    .build();
        }

        void initAll() {
            initializer.initAll();
        }
    }
}