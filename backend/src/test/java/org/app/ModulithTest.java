package org.app;

import com.tngtech.archunit.core.domain.JavaClass.*;
import lombok.extern.slf4j.*;
import org.junit.jupiter.api.*;
import org.springframework.modulith.core.*;
import org.springframework.modulith.docs.*;

@Slf4j
class ModulithTest {

    static final String
            utilPackage = "org.app.util..",
            entityPackage = "org.app.entity..";

    final ApplicationModules modules = ApplicationModules.of(
            Backend.class, Predicates.resideInAnyPackage(
                    utilPackage, entityPackage
            )
    );

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
