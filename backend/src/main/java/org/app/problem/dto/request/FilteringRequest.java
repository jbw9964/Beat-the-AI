package org.app.problem.dto.request;

import jakarta.validation.constraints.*;
import org.app.problem.domain.search.filter.*;

public record FilteringRequest(
        @NotNull(message = "문제 필터 타입을 제공해 주세요.")
        ProblemFilterType filterType,
        String from,
        String to,
        String equalTo
) {

}
