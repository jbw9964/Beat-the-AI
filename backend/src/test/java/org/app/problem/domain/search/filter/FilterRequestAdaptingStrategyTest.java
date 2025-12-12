package org.app.problem.domain.search.filter;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.*;
import org.app.problem.domain.exception.*;
import org.app.problem.dto.request.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;

class FilterRequestAdaptingStrategyTest extends IntegrationTestSupport {

    @Autowired
    List<FilterRequestAdaptingStrategy<?>> strategyList;

    private static List<ProblemFilterType> getProblemFilterTypesExcept(
            ProblemFilterType except
    ) {
        return Arrays.stream(ProblemFilterType.values())
                .filter(type -> !type.equals(except))
                .toList();
    }

    @Test
    @DisplayName("모든 필터링 타입에 대한 전략이 구비되어있다.")
    void test() {
        Map<ProblemFilterType, FilterRequestAdaptingStrategy<?>> strategyMap = strategyList.stream()
                .collect(Collectors.toMap(
                        FilterRequestAdaptingStrategy::handleableFilterType,
                        Function.identity()
                ));

        for (ProblemFilterType type : ProblemFilterType.values()) {
            assertThat(strategyMap).containsKey(type);

            FilterRequestAdaptingStrategy<?> strategy = strategyMap.get(type);
            assertThat(strategy).isNotNull();
            assertThat(strategy.handleableFilterType()).isEqualTo(type);
        }
    }

    @Test
    @DisplayName("전략과 요청의 filter type 이 일치하지 않으면 FilterTypeMismatchException 이 발생한다.")
    void testFilterTypeMismatchException() {
        for (FilterRequestAdaptingStrategy<?> strategy : strategyList) {

            ProblemFilterType handleableFilterType = strategy.handleableFilterType();
            List<ProblemFilterType> otherTypes = getProblemFilterTypesExcept(
                    handleableFilterType
            );

            for (ProblemFilterType otherType : otherTypes) {
                FilteringRequest request = new FilteringRequest(
                        otherType, null, null, null
                );

                assertThatThrownBy(() -> strategy.toFilter(request))
                        .isInstanceOf(FilterTypeMismatchException.class);
            }
        }
    }

    @Test
    @DisplayName("Parse 할 수 없는 요청은 MalformedFilterRequestException 을 일으킨다.")
    void testMalformedFilteringRequestException() {
        String malformedFilterValue = "This should raise MalformedFilterRequestException";

        for (FilterRequestAdaptingStrategy<?> strategy : strategyList) {
            ProblemFilterType handleableFilterType = strategy.handleableFilterType();
            FilteringRequest request = new FilteringRequest(
                    handleableFilterType,
                    malformedFilterValue, malformedFilterValue, malformedFilterValue
            );

            assertThatThrownBy(() -> strategy.toFilter(request))
                    .isInstanceOf(MalformedFilterRequestException.class);
        }
    }
}