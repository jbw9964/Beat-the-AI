package org.app.problem.service.strategy.filter;

import static org.assertj.core.api.Assertions.*;

import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.filter.*;
import org.app.problem.dto.request.*;
import org.junit.jupiter.api.*;

class NumOfRatingsFilterRequestAdaptingStrategyTest {

    static final NumOfRatingsFilterRequestAdaptingStrategy strategy
            = new NumOfRatingsFilterRequestAdaptingStrategy();

    @Test
    @DisplayName("문제 평가 횟수 필터링 요청은 from, to 속성만 받아들인다.")
    void testToFilter() {
        ProblemFilterType filterType = strategy.handleableFilterType();

        Long from = 1L, to = 2L, equalTo = 3L;
        FilteringRequest request = Utils.genRequest(
                filterType, from, to, equalTo, String::valueOf
        );

        ProblemFilter<Long> response = strategy.toFilter(request);

        assertThat(response).isNotNull();
        assertThat(response.getFilterType()).isEqualTo(filterType);
        assertThat(response.getFrom()).isEqualTo(from);
        assertThat(response.getTo()).isEqualTo(to);
        assertThat(response.getEqualTo()).isNull();
    }

    @Test
    @DisplayName("문제 평가 횟수 필터링 요청 값은 1 보다 크거나 같아야 한다.")
    void testIllegalFilterValueException() {
        ProblemFilterType filterType = strategy.handleableFilterType();

        Long invalid = 0L;
        FilteringRequest request = Utils.genRequest(
                filterType, invalid, invalid, null, String::valueOf
        );

        assertThatThrownBy(() -> strategy.toFilter(request))
                .isInstanceOf(IllegalFilterValueException.class);
    }
}