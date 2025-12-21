package org.app.config.domain.scenario;

import static org.assertj.core.api.Assertions.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import org.*;
import org.app.entity.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class ScenarioInfoDeserializerTest extends IntegrationTestSupport {

    @Autowired
    ScenarioInfoDeserializer deserializer;

    @Autowired
    ObjectMapper objMapper;

    @Test
    @DisplayName("직렬화된 내용을 역직렬화할 수 있다.")
    void deserialize() throws JsonProcessingException {
        int size = 10;
        ScenarioInfo[] infos = new ScenarioInfo[size];
        for (int i = 0; i < size; i++) {
            String sC = String.format("SC-%d", i);
            String aC = String.format("AC-%d", i);
            infos[i] = new ScenarioInfo(i, sC, aC);
        }

        String serialized = objMapper.writeValueAsString(infos);

        ScenarioInfo[] givens = deserializer.deserialize(serialized);
        assertThat(givens).isNotNull().hasSize(size);

        for (int i = 0; i < size; i++) {
            assertThat(givens[i]).isEqualTo(infos[i]);
        }
    }

    @Test
    @DisplayName("올바르지 않은 문자열은 역직렬화할 수 없다.")
    void testException() throws JsonProcessingException {
        assertThatThrownBy(() -> deserializer.deserialize(null))
                .isInstanceOf(InvalidSerializedScenarioInfoException.class);
        assertThatThrownBy(() -> deserializer.deserialize(""))
                .isInstanceOf(InvalidSerializedScenarioInfoException.class);

        int size = 10;
        ScenarioInfo[] infos = new ScenarioInfo[size];
        for (int i = 0; i < size; i++) {
            String sC = String.format("SC-%d", i);
            String aC = String.format("AC-%d", i);
            infos[i] = new ScenarioInfo(i, sC, aC);
        }

        String serialized = objMapper.writeValueAsString(infos);
        String invalid = serialized.substring(serialized.length() / 2);

        assertThatThrownBy(() -> deserializer.deserialize(invalid))
                .isInstanceOf(InvalidSerializedScenarioInfoException.class);
    }
}