package org.app.problem.dto;

import org.app.problem.domain.search.filter.*;

public record FilteringInfo(
        ProblemFilterType appliedFilterType,
        String from,
        String to,
        String euqalTo
) {

    public FilteringInfo(ProblemFilter<?> problemFilter) {
        this(
                problemFilter.getFilterType(),
                getStringOrNull(problemFilter.getFrom()),
                getStringOrNull(problemFilter.getTo()),
                getStringOrNull(problemFilter.getEqualTo())
        );
    }

    private static String getStringOrNull(Object obj) {
        return obj == null ? null : obj.toString();
    }
}
