package org.app.problem.service;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.filter.*;
import org.app.problem.dto.request.*;
import org.app.util.exception.*;
import org.springframework.stereotype.*;

@Component
public class FilterRequestAdaptor {

    private final Map<ProblemFilterType, FilterRequestAdaptingStrategy<?>> strategyMap;

    public FilterRequestAdaptor(List<FilterRequestAdaptingStrategy<?>> strategyList) {
        strategyMap = strategyList.stream().collect(Collectors.toMap(
                FilterRequestAdaptingStrategy::handleableFilterType,
                Function.identity()
        ));
    }

    public List<ProblemFilter<?>> convertToProblemFilterQuery(
            List<FilteringRequest> filteringRequests
    ) throws MalformedFilterRequestException, IllegalFilterValueException {
        // ?? toList() 로 콜렉트 하면 컴파일 에러 뜨고
        // collect(Collectors.toList()) 로 하면 안뜸... 뭐지??
        return filteringRequests.stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    private ProblemFilter<?> convert(FilteringRequest filteringRequest) {
        ProblemFilterType filterType = filteringRequest.filterType();

        if (!strategyMap.containsKey(filterType)) {
            throw new NotImplementedException(
                    String.format(
                            "FilterReuqestAdaptor for filterType=%s has not been implemented",
                            filterType
                    ),
                    String.format(
                            "%s 에 대한 필터링 검색은 아직 구현되지 않았습니다.",
                            filterType
                    )
            );
        }

        return strategyMap.get(filterType).toFilter(filteringRequest);
    }
}
