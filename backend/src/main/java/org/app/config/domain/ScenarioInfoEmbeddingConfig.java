package org.app.config.domain;

import com.fasterxml.jackson.databind.*;
import org.app.config.domain.internal.*;
import org.springframework.context.annotation.*;

@Configuration
class ScenarioInfoEmbeddingConfig {

    @Bean
    public ScenarioInfoValidator validator() {
        return new ScenarioInfoValidatorImpl();
    }

    @Bean
    public ScenarioInfoSerializer serializer(ObjectMapper objMapper) {
        return new ScenarioInfoSerializerImpl(objMapper, validator());
    }

    @Bean
    public ScenarioInfoDeserializer deserializer(ObjectMapper objMapper) {
        return new ScenarioInfoDeserializerImpl(objMapper, validator());
    }
}
