package org;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import java.util.function.*;
import org.app.util.api.*;

public class TestUtils {

    public static <T> void assertSimplePageResponseEquality(
            SimplePageResponse<T> pageResponse, int pageNo, int pageSize,
            int numOfPagedElements, int numOfTotal, boolean hasNext
    ) {

        assertThat(pageResponse).isNotNull();
        assertThat(pageResponse.pageNoRequest()).isEqualTo(pageNo);
        assertThat(pageResponse.pageSizeRequest()).isEqualTo(pageSize);
        assertThat(pageResponse.numOfPagedElements()).isEqualTo(numOfPagedElements);
        assertThat(pageResponse.numOfTotalElements()).isEqualTo(numOfTotal);
        assertThat(pageResponse.hasNext()).isEqualTo(hasNext);

        List<T> elements = pageResponse.pagedElements();
        assertThat(elements).isNotNull().hasSize(numOfPagedElements);
    }

    public static <T> void assertThrow(
            T t, Function<T, ?> func,
            Class<? extends RuntimeException> ex
    ) {
        assertThatThrownBy(() -> func.apply(t))
                .isInstanceOf(ex);
    }

    public static <T, U> void assertThrow(
            T t, U u, BiFunction<T, U, ?> func,
            Class<? extends RuntimeException> ex
    ) {
        assertThatThrownBy(() -> func.apply(t, u))
                .isInstanceOf(ex);
    }

    public static <T, U> void assertThrow(
            T t, U u, BiConsumer<T, U> func,
            Class<? extends RuntimeException> ex
    ) {
        assertThatThrownBy(() -> func.accept(t, u))
                .isInstanceOf(ex);
    }

    public static <T, U, D> void assertThrow(
            T t, U u, D d, Triplet<T, U, D, ?> func,
            Class<? extends RuntimeException> ex
    ) {
        assertThatThrownBy(() -> func.apply(t, u, d))
                .isInstanceOf(ex);
    }

    @FunctionalInterface
    @SuppressWarnings("UnusedReturnValue")
    public interface Triplet<T, U, D, R> {

        R apply(T t, U u, D d);
    }
}
