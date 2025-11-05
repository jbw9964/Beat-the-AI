package org.app.config.jpa;

import java.time.*;
import org.springframework.stereotype.*;

@Component
public class EntityAuditingProvider {

    public LocalDateTime provideLocalDateTime() {
        return LocalDateTime.now();
    }

}
