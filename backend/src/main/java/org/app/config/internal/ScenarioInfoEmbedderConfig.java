package org.app.config.internal;

import com.fasterxml.jackson.databind.*;
import org.springframework.context.annotation.*;

@Configuration
class ScenarioInfoEmbedderConfig {

    @Bean
    public ScenarioInfoEmbedder scenarioInfoEmbedder(ObjectMapper objMapper) {
        return new ScenarioInfoEmbedderImpl(objMapper);
    }

}
