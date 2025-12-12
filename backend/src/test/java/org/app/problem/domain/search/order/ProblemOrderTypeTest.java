package org.app.problem.domain.search.order;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import org.app.problem.domain.exception.*;
import org.app.problem.dto.request.*;
import org.junit.jupiter.api.*;

class ProblemOrderTypeTest {

    private static OrderingRequest genReq(ProblemOrderType type) {
        return new OrderingRequest(type, null);
    }

    @Test
    @DisplayName("정렬 요청 중 상반되거나 중복된 요청이 존재하면 ImproperOrderTypesException 이 발생한다.")
    void testImproperOrderTypesException() {

        ProblemOrderType[] values = ProblemOrderType.values();

        List<OrderingRequest> validRequests
                = Arrays.stream(values)
                .map(ProblemOrderTypeTest::genReq)
                .toList();

        for (ProblemOrderType addition : values) {

            List<OrderingRequest> improperRequests = new ArrayList<>(validRequests);
            improperRequests.add(genReq(addition));

            assertThatThrownBy(() -> ProblemOrderType.assertNoDuplicateOderExists(
                    improperRequests
            ))
                    .isInstanceOf(ImproperOrderTypesException.class);
        }
    }
}