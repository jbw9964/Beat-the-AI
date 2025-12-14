package org.app.util;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import java.util.function.*;
import org.app.entity.*;
import org.junit.jupiter.api.*;

class GlobalUtilTest {

    static final Map<Integer, Integer> testMap = new HashMap<>();
    static final Map<Integer, SomeRecord> testMap2 = new HashMap<>();

    final GlobalUtil globalUtil = new GlobalUtil();

    @BeforeEach
    void setUp() {
        testMap.clear();
    }

    @AfterEach
    void tearDown() {
        testMap.clear();
    }

    Optional<Integer> getOptionalMapValue(Integer i) {
        Integer find = testMap.get(i);
        return Optional.ofNullable(find);
    }

    Optional<SomeRecord> getOptionalSomeRecordMapValue(Integer i) {
        SomeRecord find = testMap2.get(i);
        return Optional.ofNullable(find);
    }

    @Test
    @DisplayName("GetOrThrow 가 정상 작동한다.")
    void testGetOrThrow() {
        int identity = 1, value = 2;
        int notExistingIdentity = Integer.MAX_VALUE;

        testMap.put(identity, value);
        testMap.remove(notExistingIdentity);

        Integer get = globalUtil.getOrThrow(
                identity, this::getOptionalMapValue, CustomException::new
        );
        assertThat(get).isEqualTo(value);

        assertThatThrownBy(() -> globalUtil.getOrThrow(
                notExistingIdentity, this::getOptionalMapValue, CustomException::new
        ))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("Filter 를 포함한 GetOrThrow 가 정상 작동한다.")
    void testGetOrThrowFilter() {
        int identity1 = 1, value1 = 2;
        int identity2 = 3, value2 = 4;
        int notExistingIdentity = Integer.MAX_VALUE;

        testMap.put(identity1, value1);
        testMap.put(identity2, value2);
        testMap.remove(notExistingIdentity);

        Predicate<Integer> filterVal2 = i -> i != value2;

        Integer get = globalUtil.getOrThrow(
                identity1, this::getOptionalMapValue,
                CustomException::new, filterVal2
        );

        assertThat(get).isEqualTo(value1);

        assertThatThrownBy(() -> globalUtil.getOrThrow(
                notExistingIdentity, this::getOptionalMapValue,
                CustomException::new, filterVal2
        ))
                .isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> globalUtil.getOrThrow(
                identity2, this::getOptionalMapValue,
                CustomException::new, filterVal2
        ))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("Soft delete 객체에 대해 GetOrThrow 가 정상 작동한다.")
    void testGetNonSoftDeltedOrThrow() {
        int idnetity1 = 1, identity2 = 2;
        int notExistingIdentity = Integer.MAX_VALUE;
        SomeRecord val1 = new SomeRecord(false);
        SomeRecord val2 = new SomeRecord(true);

        testMap2.put(idnetity1, val1);
        testMap2.put(identity2, val2);

        SomeRecord get = globalUtil.getNonSoftDeltedOrThrow(
                idnetity1, this::getOptionalSomeRecordMapValue,
                CustomException::new
        );

        assertThat(get).isEqualTo(val1);

        assertThatThrownBy(() -> globalUtil.getNonSoftDeltedOrThrow(
                notExistingIdentity, this::getOptionalSomeRecordMapValue,
                CustomException::new
        ))
                .isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> globalUtil.getNonSoftDeltedOrThrow(
                identity2, this::getOptionalSomeRecordMapValue,
                CustomException::new
        ))
                .isInstanceOf(CustomException.class);
    }

    private static class CustomException extends RuntimeException {

    }

    private record SomeRecord(
            boolean softDeleted
    ) implements SoftDelete {

        @Override
        public SchedueldRemoval getSchedueldRemoval() {
            return null;
        }

        @Override
        public boolean doesRemovalScheduled() {
            return softDeleted;
        }
    }
}