package org.app.problem.dto.request;

import jakarta.validation.constraints.*;
import org.app.problem.domain.search.order.*;

public record OrderingRequest(
        @NotNull(message = "문제 정렬 타입을 제공해 주세요.")
        ProblemOrderType orderType,

        @Min(value = 0, message = "정렬 우선순위는 0 보다 크거나 같아야 합니다.")
        Integer priority
) implements Comparable<OrderingRequest> {

    @Override
    public Integer priority() {
        return priority != null ?
                priority : Integer.MAX_VALUE;
    }

    @Override
    public int compareTo(OrderingRequest o) {
        return Integer.compare(this.priority(), o.priority());
    }
}
