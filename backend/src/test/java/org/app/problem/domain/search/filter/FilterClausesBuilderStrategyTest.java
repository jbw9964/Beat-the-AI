package org.app.problem.domain.search.filter;

import static org.assertj.core.api.Assertions.*;

import com.querydsl.core.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.*;
import org.app.problem.domain.exception.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;

class FilterClausesBuilderStrategyTest extends IntegrationTestSupport {

    @Autowired
    List<FilterClausesBuilderStrategy> strategyList;

    private static List<ProblemFilterType> getProblemFilterTypesExcept(
            ProblemFilterType except
    ) {
        return Arrays.stream(ProblemFilterType.values())
                .filter(type -> !type.equals(except))
                .toList();
    }

    private static ProblemFilter<?> genProblemFilter(
            ProblemFilterType filterType
    ) {
        return new ProblemFilter<>() {
            @Override
            public Object getFrom() {
                return null;
            }

            @Override
            public Object getTo() {
                return null;
            }

            @Override
            public Object getEqualTo() {
                return null;
            }

            @Override
            public ProblemFilterType getFilterType() {
                return filterType;
            }
        };
    }

    @Test
    @DisplayName("모든 필터링 타입에 대한 전략이 구비되어있다.")
    void test() {
        Map<ProblemFilterType, FilterClausesBuilderStrategy> strategyMap = strategyList.stream()
                .collect(Collectors.toMap(
                        FilterClausesBuilderStrategy::handleableFilterType,
                        Function.identity()
                ));

        for (ProblemFilterType type : ProblemFilterType.values()) {
            assertThat(strategyMap).containsKey(type);

            FilterClausesBuilderStrategy strategy = strategyMap.get(type);
            assertThat(strategy).isNotNull();
            assertThat(strategy.handleableFilterType()).isEqualTo(type);
        }
    }

    @Test
    @DisplayName("전략과 ProblemFilter 의 filter type 이 일치하지 않으면 "
                 + "FilterTypeMismatchException 이 발생한다.")
    void testFilterTypeMismatchException() {

        assertThat(strategyList).isNotEmpty();

        BooleanBuilder booleanBuilder = new BooleanBuilder();
        for (FilterClausesBuilderStrategy strategy : strategyList) {

            ProblemFilterType handleableFilterType = strategy.handleableFilterType();
            List<ProblemFilterType> otherTypes = getProblemFilterTypesExcept(
                    handleableFilterType
            );

            for (ProblemFilterType otherType : otherTypes) {
                ProblemFilter<?> invalidFilter = genProblemFilter(otherType);

                assertThatThrownBy(() -> strategy.addFilterClauses(
                        booleanBuilder, invalidFilter
                ))
                        .isInstanceOf(FilterTypeMismatchException.class);
            }
        }
    }

    @Test
    @DisplayName("ProblemFilter 값 타입이 전략과 일치하지 않으면 "
                 + "FailedToCastFilterValueException 이 발생한다.")
    void testFailedToCastFilterValueException() {

        assertThat(strategyList).isNotEmpty();

        BooleanBuilder booleanBuilder = new BooleanBuilder();
        SomeRecord unexpectedSomeRecordTypeValue = new SomeRecord();

        for (FilterClausesBuilderStrategy strategy : strategyList) {
            ProblemFilterType handleableFilterType = strategy.handleableFilterType();
            ProblemFilter<SomeRecord> problemFilter
                    = unexpectedSomeRecordTypeValue.toProblemFilter(handleableFilterType);

            assertThatThrownBy(() -> strategy.addFilterClauses(
                    booleanBuilder, problemFilter
            ))
                    .isInstanceOf(FailedToCastFilterValueException.class);
        }
    }

    private record SomeRecord() {

        ProblemFilter<SomeRecord> toProblemFilter(ProblemFilterType filterType) {
            SomeRecord self = this;

            return new ProblemFilter<>() {
                @Override
                public SomeRecord getFrom() {
                    return self;
                }

                @Override
                public SomeRecord getTo() {
                    return self;
                }

                @Override
                public SomeRecord getEqualTo() {
                    return self;
                }

                @Override
                public ProblemFilterType getFilterType() {
                    return filterType;
                }
            };
        }
    }
}