package org.app.config.domain;

import com.fasterxml.jackson.databind.*;
import org.app.config.domain.internal.*;
import org.springframework.context.annotation.*;

@Configuration
class ScenarioInfoEmbedderConfig {

    @Bean
    public ScenarioInfoEmbedder scenarioInfoEmbedder(ObjectMapper objMapper) {
        return new ScenarioInfoEmbedderImpl(objMapper);
    }

}
