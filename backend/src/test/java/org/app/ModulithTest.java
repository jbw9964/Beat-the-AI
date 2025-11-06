package org.app;

import lombok.extern.slf4j.*;
import org.junit.jupiter.api.*;
import org.springframework.modulith.core.*;
import org.springframework.modulith.docs.*;

@Slf4j
class ModulithTest {

    final ApplicationModules modules = ApplicationModules.of(Backend.class);

    @Test
    @DisplayName("코드가 modulith 하다.")
    void verifyModules() {
        try {
            modules.verify();
        } finally {
            modules.forEach(m -> log.info("Module : {}", m));
        }
    }

    @Test
    @DisplayName("문서로 보여준다.")
    void writeDocument() {
        Documenter documenter = new Documenter(modules);
        documenter.writeDocumentation();
    }
}
