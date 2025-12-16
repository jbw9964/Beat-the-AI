package org.app.entity;

import jakarta.persistence.*;
import java.time.*;
import lombok.*;
import lombok.experimental.*;

@Getter
@Embeddable
@Accessors(fluent = true, chain = false)
public class ScheduledRemoval {

    @Column(nullable = false)
    private boolean doesRemovalScheduled;

    private LocalDateTime requestedAt;

    private LocalDateTime scheduedAt;

    protected ScheduledRemoval() {
        this.doesRemovalScheduled = false;
        this.requestedAt = this.scheduedAt = null;
    }

    protected ScheduledRemoval(
            LocalDateTime requestedAt, LocalDate scheduledDate
    ) {
        this.doesRemovalScheduled = true;
        this.requestedAt = requestedAt;
        this.scheduedAt = scheduledDate.atStartOfDay().minusMinutes(5L);
    }

    public static ScheduledRemoval notScheduled() {
        return new ScheduledRemoval();
    }

    public static ScheduledRemoval scheduled(
            LocalDateTime removalRequestedAt, LocalDate removalScheduledDate
    ) {
        return new ScheduledRemoval(removalRequestedAt, removalScheduledDate);
    }
}
