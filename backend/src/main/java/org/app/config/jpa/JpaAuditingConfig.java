package org.app.config.jpa;

import java.time.*;
import java.util.*;
import lombok.*;
import org.springframework.context.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.config.*;

@Configuration
@EnableJpaAuditing(modifyOnCreate = false)
@RequiredArgsConstructor
class JpaAuditingConfig {

    private final EntityAuditingProvider entityAuditingProvider;

    @Bean
    public AuditorAware<LocalDateTime> localDateTimeAuditor() {
        return () -> Optional.of(entityAuditingProvider.provideLocalDateTime());
    }

}
