package org.app.config.domain;

import java.time.*;
import org.springframework.modulith.*;
import org.springframework.stereotype.*;

@Component
@NamedInterface
public class SoftDeletePolicy {

    public LocalDate getRemovalDateOn(LocalDateTime from) {
        return from.plusHours(72L).toLocalDate();
    }

}
