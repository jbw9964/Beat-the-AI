package org.app.config;

import com.fasterxml.jackson.databind.*;
import jakarta.annotation.*;
import lombok.*;
import org.springframework.context.annotation.*;

@Configuration
@RequiredArgsConstructor
public class AdditionalJacksonConfig {

    private final ObjectMapper objMapper;

    @PostConstruct
    public void addConfigs() {
        unknownEnumValuesAsNull();
    }

    /**
     * 역직렬화에 실패한 {@code enum} 은 {@code null} 로 만든어준다.
     */
    private void unknownEnumValuesAsNull() {
        objMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
    }
}
