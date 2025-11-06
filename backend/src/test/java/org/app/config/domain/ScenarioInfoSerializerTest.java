package org.app.config.domain;

import static org.assertj.core.api.Assertions.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import java.util.*;
import lombok.extern.slf4j.*;
import org.*;
import org.app.entity.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;

@Slf4j
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class ScenarioInfoSerializerTest extends IntegrationTestSupport {

    @Autowired
    ScenarioInfoSerializer serializer;

    @Autowired
    ObjectMapper objMapper;

    @Test
    @DisplayName("리스트 형태의 시나리오 정보를 직렬화할 수 있다.")
    void testSerialize2() throws JsonProcessingException {
        List<ScenarioInfo> infos = new ArrayList<>();
        {
            int i = 0;
            for (; i < 10; i++) {
                String sC = String.format("SC-%d", i);
                String aC = String.format("AC-%d", i);
                infos.add(new ScenarioInfo(i, sC, aC));
            }
        }

        String serializedInfo = serializer.serialize(infos);
        logSerializedInfo(serializedInfo);

        assertThat(serializedInfo).isNotEmpty();

        ScenarioInfo[] givens = objMapper.readValue(serializedInfo, ScenarioInfo[].class);
        assertThat(givens).isNotNull().hasSize(infos.size());

        for (int i = 0; i < givens.length; i++) {
            assertThat(givens[i]).isEqualTo(infos.get(i));
        }
    }

    @Test
    @DisplayName("배열 형태의 시나리오 정보를 직렬화할 수 있다.")
    void testSerialize3() throws JsonProcessingException {
        int size = 10;
        ScenarioInfo[] infos = new ScenarioInfo[size];
        for (int i = 0; i < size; i++) {
            String sC = String.format("SC-%d", i);
            String aC = String.format("AC-%d", i);
            infos[i] = new ScenarioInfo(i, sC, aC);
        }

        String serializedInfo = serializer.serialize(infos);
        logSerializedInfo(serializedInfo);

        assertThat(serializedInfo).isNotEmpty();

        ScenarioInfo[] givens = objMapper.readValue(serializedInfo, ScenarioInfo[].class);
        assertThat(givens).isNotNull().hasSize(size);

        for (int i = 0; i < size; i++) {
            assertThat(givens[i]).isEqualTo(infos[i]);
        }
    }

    @Test
    @DisplayName("Null 또는 비어있는 복수 객체는 직렬화 할 수 없다.")
    void testException1() {
        ScenarioInfo[] invalid = new ScenarioInfo[]{
                new ScenarioInfo(0, "a", "b"), null
        };

        assertThatThrownBy(() -> serializer.serialize((List<ScenarioInfo>) null))
                .isInstanceOf(InvalidScenarioInfoException.class);
        assertThatThrownBy(() -> serializer.serialize(Collections.emptyList()))
                .isInstanceOf(InvalidScenarioInfoException.class);
        assertThatThrownBy(() -> serializer.serialize(Arrays.asList(invalid)))
                .isInstanceOf(InvalidScenarioInfoException.class);

        assertThatThrownBy(() -> serializer.serialize((ScenarioInfo[]) null))
                .isInstanceOf(InvalidScenarioInfoException.class);
        assertThatThrownBy(() -> serializer.serialize(new ScenarioInfo[0]))
                .isInstanceOf(InvalidScenarioInfoException.class);
        assertThatThrownBy(() -> serializer.serialize(invalid))
                .isInstanceOf(InvalidScenarioInfoException.class);
    }

    @Test
    @DisplayName("제공된 순서가 맞지 않으면 직렬화 할 수 없다.")
    void testException2() {
        ScenarioInfo[] invalidArr = new ScenarioInfo[]{
                new ScenarioInfo(0, "a", "b"),
                new ScenarioInfo(10, "a", "b"),
                new ScenarioInfo(2, "a", "b")
        };

        assertThatThrownBy(() -> serializer.serialize(Arrays.asList(invalidArr)))
                .isInstanceOf(InvalidScenarioInfoException.class);
        assertThatThrownBy(() -> serializer.serialize(invalidArr))
                .isInstanceOf(InvalidScenarioInfoException.class);
    }

    private void logSerializedInfo(String serializeInfo) {
        log.info("Serialized Info: {}", serializeInfo);
    }
}
