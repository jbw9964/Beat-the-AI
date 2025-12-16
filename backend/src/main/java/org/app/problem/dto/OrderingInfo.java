package org.app.problem.dto;

import org.app.problem.domain.search.order.*;

public record OrderingInfo(
        ProblemOrderType appliedOrderType,
        Integer appliedPriority
) {

}
