package org.app.problem.service.strategy.filter;

import static org.assertj.core.api.Assertions.*;

import java.time.*;
import java.time.format.*;
import org.app.problem.domain.search.filter.*;
import org.app.problem.dto.request.*;
import org.junit.jupiter.api.*;

class CreatedDateFilterRequestAdaptingStrategyTest {

    private static final LocalDate TODAY = LocalDate.now();
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            AbstractFilterRequestAdaptor.DATE_TIME_FORMATTER;

    private static final CreatedDateFilterRequestAdaptingStrategy strategy
            = new CreatedDateFilterRequestAdaptingStrategy();

    @Test
    @DisplayName("생성일 필터링 요청은 from, to 속성만 받아들인다.")
    void testToFilter() {
        ProblemFilterType filterType = strategy.handleableFilterType();

        LocalDate from = TODAY.plusDays(1L);
        LocalDate to = TODAY.plusDays(2L);
        LocalDate equalTo = TODAY.plusDays(3L);

        FilteringRequest request = Utils.genRequest(
                filterType, from, to, equalTo,
                DATE_TIME_FORMATTER::format
        );

        ProblemFilter<LocalDate> response = strategy.toFilter(request);

        assertThat(response).isNotNull();
        assertThat(response.getFilterType()).isEqualTo(filterType);
        assertThat(response.getFrom()).isNotNull().isEqualTo(from);
        assertThat(response.getTo()).isNotNull().isEqualTo(to);
        assertThat(response.getEqualTo()).isNull();
    }
}