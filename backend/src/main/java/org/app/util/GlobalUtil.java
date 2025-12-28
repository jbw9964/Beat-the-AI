package org.app.util;

import java.util.*;
import java.util.function.*;
import lombok.extern.slf4j.*;
import org.app.entity.*;
import org.app.util.api.*;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort.*;
import org.springframework.stereotype.*;

@Slf4j
@Component
public class GlobalUtil implements PageableProvider {

    public <E, I> E getOrThrow(
            I identity, Function<I, Optional<E>> func,
            Supplier<RuntimeException> ex, Predicate<E> filter
    ) {
        Optional<E> opt = func.apply(identity);

        if (
                opt.isPresent() &&
                (opt = opt.filter(filter)).isEmpty()
        ) {
            log.info("Value has been filtered by: {}", filter);
        }

        return opt.orElseThrow(ex);
    }

    public <E extends SoftDelete, I> E getNonSoftDeltedOrThrow(
            I identity, Function<I, Optional<E>> func,
            Supplier<RuntimeException> ex
    ) {
        Optional<E> opt = func.apply(identity);

        if (
                opt.isPresent() &&
                (opt = opt.filter(e -> !e.doesRemovalScheduled())).isEmpty()
        ) {
            log.info("Found value but reserved soft deletion.");
        }

        return opt.orElseThrow(ex);
    }

    public <E, I> SimplePageResponse<I> toSimplePageResponse(
            Page<E> find, Function<E, I> mapperFunc
    ) {
        Pageable pageable = find.getPageable();
        int pageNo = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        long numOfTotalElements = find.getTotalElements();
        boolean hasNext = find.hasNext();

        List<I> infos = find.map(mapperFunc).toList();

        return new SimplePageResponse<>(pageNo, pageSize, numOfTotalElements, hasNext, infos);
    }

    @Override
    public Pageable pageable(int pageNo, int pageSize) {
        return PageRequest.of(
                pageNo, pageSize,
                Direction.DESC, "createdAt"
        );
    }
}
