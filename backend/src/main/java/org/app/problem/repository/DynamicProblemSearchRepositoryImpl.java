package org.app.problem.repository;

import com.querydsl.core.*;
import com.querydsl.core.types.*;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import lombok.extern.slf4j.*;
import org.app.entity.*;
import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.*;
import org.app.problem.domain.search.filter.*;
import org.app.problem.domain.search.order.*;
import org.app.util.exception.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;

// 원래 클래스 이름 FilterClausesAdaptor 로 만들려 했는데 그러니까
// searchByFilter 메서드에 대한 jqpl 만들려 해서 bean creation error 발생함.
// 그래서 아쉽게도 Impl 이름으로 바꿈...
@Slf4j
@Component
public class DynamicProblemSearchRepositoryImpl implements DynamicProblemSearchRepository {

    private static final QProblem QP = QProblemPaths.QPROBLEM_ROOT;
    private static final QProblemAggregation QPA_TARGET
            = QProblemPaths.QP__QPROBLEM_AGGREGATION_TARGET;
    private static final QProblemAggregation QPA_ALIAS
            = QProblemPaths.QPROBLEM_AGGREGATION_ROOT;

    private static final String
            MAIN_QUERY_COMMENT = "`Dynamic problem search main query`",
            COUNT_QUERY_COMMENT = "`Dynamic problem search count query`";

    private final JPAQueryFactory query;
    private final Map<ProblemFilterType, FilterClausesBuilderStrategy>
            strategyMap;

    public DynamicProblemSearchRepositoryImpl(
            JPAQueryFactory jpaQueryFactory,
            List<FilterClausesBuilderStrategy> strategies
    ) {
        this.query = jpaQueryFactory;
        this.strategyMap = strategies.stream().collect(Collectors.toMap(
                FilterClausesBuilderStrategy::handleableFilterType,
                Function.identity()
        ));
    }

    @Override
    public Page<Problem> searchNonSoftDeletedPublicProblemWithFilters(
            List<ProblemFilter<?>> filters,
            List<ProblemOrder> orders,
            Pageable pageable
    )
            throws FilterTypeMismatchException, FailedToCastFilterValueException {

        JPAQuery<Problem> mainQuery = query.selectFrom(QP)
                .leftJoin(QPA_TARGET, QPA_ALIAS).fetchJoin();
        JPAQuery<Long> countQuery = query.select(QP.count())
                .from(QP);

        Predicate publicProblemClauses = QP.visibility.eq(ProblemVisibility.PUBLIC);
        Predicate nonRemovalScheduledClauses = QP.schedueldRemoval.doesRemovalScheduled.not();
        BooleanBuilder filteringClauses = this.buildFilterClauses(filters);
        OrderSpecifier<?>[] orderClauses = this.buildOrderClauses(orders);

        log.info("Builded filter clauses: {}", filteringClauses);

        List<Problem> result = mainQuery
                .where(
                        publicProblemClauses,
                        nonRemovalScheduledClauses,
                        filteringClauses
                )
                .orderBy(
                        orderClauses
                )
                .offset(
                        pageable.getOffset()
                )
                .limit(
                        pageable.getPageSize()
                )
                .setHint(JPA_COMMENT_KEY, MAIN_QUERY_COMMENT)
                .fetch();

        Long count = countQuery
                .where(
                        publicProblemClauses,
                        nonRemovalScheduledClauses,
                        filteringClauses
                )
                .setHint(JPA_COMMENT_KEY, COUNT_QUERY_COMMENT)
                .fetchOne();

        return new PageImpl<>(
                result, pageable,
                Objects.requireNonNull(
                        count,
                        "Expected to get non-null count, but somehow it is."
                )
        );
    }

    private BooleanBuilder buildFilterClauses(List<ProblemFilter<?>> filters) {
        BooleanBuilder filteringClauses = new BooleanBuilder();

        for (ProblemFilter<?> filter : filters) {
            ProblemFilterType filterType = filter.getFilterType();

            if (!strategyMap.containsKey(filterType)) {
                throw new NotImplementedException(
                        String.format(
                                "FilterClausesBuilder for type=%s has not been implemented",
                                filterType
                        ),
                        String.format(
                                "%s 에 대한 필터링 검색은 아직 구현되지 않았습니다.",
                                filterType
                        )
                );
            }

            strategyMap.get(filterType).addFilterClauses(filteringClauses, filter);
        }

        return filteringClauses;
    }

    private OrderSpecifier<?>[] buildOrderClauses(List<ProblemOrder> orders) {
        return orders.stream()
                .map(ProblemOrder::orderSpecifier)
                .toArray(OrderSpecifier[]::new);
    }
}
