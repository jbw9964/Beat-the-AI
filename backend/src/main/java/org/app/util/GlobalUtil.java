package org.app.util;

import java.util.*;
import java.util.function.*;
import org.springframework.stereotype.*;

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
        return func.apply(identity).filter(filter).orElseThrow(ex);
    }

}
