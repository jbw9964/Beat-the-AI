package org.app.problem.service.strategy.filter;

import static org.assertj.core.api.Assertions.*;

import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.filter.*;
import org.app.problem.dto.request.*;
import org.junit.jupiter.api.*;

class CreatedUserFilterRequestAdaptingStrategyTest {

    private static final CreatedUserFilterRequestAdaptingStrategy strategy
            = new CreatedUserFilterRequestAdaptingStrategy();

    @Test
    @DisplayName("문제 생성 사용자 필터링 요청은 equalTo 속성만 받아들인다.")
    void testToFilter() {
        ProblemFilterType filterType = strategy.handleableFilterType();

        Long from = 0L, to = 1L, equalTo = 2L;
        FilteringRequest request = Utils.genRequest(
                filterType, from, to, equalTo, String::valueOf
        );

        ProblemFilter<Long> response = strategy.toFilter(request);

        assertThat(response).isNotNull();
        assertThat(response.getFilterType()).isEqualTo(filterType);
        assertThat(response.getFrom()).isNull();
        assertThat(response.getTo()).isNull();
        assertThat(response.getEqualTo()).isEqualTo(equalTo);
    }

    @Test
    @DisplayName("문제 생성 사용자 필터링 요청 값은 0 보다 크거나 같아야 한다.")
    void testIllegalFilterValueException() {
        ProblemFilterType filterType = strategy.handleableFilterType();

        Long invalid = -1L;
        FilteringRequest request = Utils.genRequest(
                filterType, null, null, invalid, String::valueOf
        );

        assertThatThrownBy(() -> strategy.toFilter(request))
                .isInstanceOf(IllegalFilterValueException.class);
    }
}