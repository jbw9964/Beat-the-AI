package org.app.util;

import java.util.*;
import java.util.function.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;

@Slf4j
@Component
public class GlobalUtil {

    public <E, I> E getOrThrow(
            I identity, Function<I, Optional<E>> func,
            Supplier<RuntimeException> ex
    ) {
        return func.apply(identity).orElseThrow(ex);
    }

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

}
