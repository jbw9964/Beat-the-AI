package org.app.problem.service.strategy.filter;

import static org.assertj.core.api.Assertions.*;

import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.filter.*;
import org.app.problem.dto.request.*;
import org.junit.jupiter.api.*;

class RatingAvgFilterRequestAdaptingStrategyTest {

    static final RatingAvgFilterRequestAdaptingStrategy strategy
            = new RatingAvgFilterRequestAdaptingStrategy();

    @Test
    @DisplayName("문제 설정 보상 개수 필터링 요청은 from, to 속성만 받아들인다.")
    void testToFilter() {
        ProblemFilterType filterType = strategy.handleableFilterType();

        Double from = 0.d, to = 1.d, equalTo = 3.d;
        FilteringRequest request = Utils.genRequest(
                filterType, from, to, equalTo, String::valueOf
        );

        ProblemFilter<Double> response = strategy.toFilter(request);

        assertThat(response).isNotNull();
        assertThat(response.getFilterType()).isEqualTo(filterType);
        assertThat(response.getFrom()).isEqualTo(from);
        assertThat(response.getTo()).isEqualTo(to);
        assertThat(response.getEqualTo()).isNull();
    }

    @Test
    @DisplayName("문제 설정 보상 개수 필터링 요청 값은 0 보다 크거나 같아야 한다.")
    void testIllegalFilterValueException() {
        ProblemFilterType filterType = strategy.handleableFilterType();

        Double invalid = -0.1d;
        FilteringRequest request = Utils.genRequest(
                filterType, invalid, invalid, null, String::valueOf
        );

        assertThatThrownBy(() -> strategy.toFilter(request))
                .isInstanceOf(IllegalFilterValueException.class);
    }
}