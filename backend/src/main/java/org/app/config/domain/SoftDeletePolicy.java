package org.app.config.domain;

import java.time.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.modulith.*;
import org.springframework.stereotype.*;

@Component
@NamedInterface
public class SoftDeletePolicy {

    private final long hoursToScheduleRemoval;

    public SoftDeletePolicy(
            @Value("${soft-delete.hours-to-schedule-removal:72}")
            long hoursToScheduleRemoval
    ) {
        this.hoursToScheduleRemoval = hoursToScheduleRemoval;
    }

    public LocalDate getRemovalDateOn(LocalDateTime from) {
        return from.plusHours(hoursToScheduleRemoval).toLocalDate();
    }

}
