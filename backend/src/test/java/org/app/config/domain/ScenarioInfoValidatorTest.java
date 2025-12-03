package org.app.config.domain;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import java.util.stream.*;
import org.*;
import org.app.entity.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class ScenarioInfoValidatorTest extends IntegrationTestSupport {

    @Autowired
    ScenarioInfoValidator validator;

    private static ScenarioInfo gen(int order) {
        String sC = String.format("SC-%d", order);
        String aC = String.format("AC-%d", order);
        return new ScenarioInfo(order, sC, aC);
    }

    private static Stream<Arguments> testArguments() {
        return Stream.of(
                Arguments.of(new ScenarioInfo[]{gen(0)},
                        true
                ),
                Arguments.of(new ScenarioInfo[]{
                                gen(0), gen(1), gen(2)
                        },
                        true
                ),
                Arguments.of(null, false),
                Arguments.of(new ScenarioInfo[0], false),
                Arguments.of(new ScenarioInfo[]{gen(1)},
                        false
                ),
                Arguments.of(new ScenarioInfo[]{
                                gen(0), gen(0)
                        },
                        false
                ),
                Arguments.of(new ScenarioInfo[]{
                                gen(0), gen(2)
                        },
                        false
                ),
                Arguments.of(new ScenarioInfo[]{
                                gen(0), null
                        },
                        false
                ),
                Arguments.of(new ScenarioInfo[]{
                                gen(0), gen(1), gen(1)
                        },
                        false
                ),
                Arguments.of(new ScenarioInfo[]{
                                gen(0), null, gen(2)
                        },
                        false
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testArguments")
    @DisplayName("list 형태 정보의 직렬화 가능 여부를 판별할 수 있다.")
    void serializable1(ScenarioInfo[] scenarioInfos, boolean expected) {

        List<ScenarioInfo> scenarioInfoList = scenarioInfos != null ?
                Arrays.asList(scenarioInfos) : null;

        boolean response = validator.serializable(scenarioInfoList);

        assertThat(response).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("testArguments")
    @DisplayName("배열 형태 정보의 직렬화 가능 여부를 판별할 수 있다.")
    void serializable2(ScenarioInfo[] scenarioInfos, boolean expected) {

        boolean response = validator.serializable(scenarioInfos);

        assertThat(response).isEqualTo(expected);
    }
}