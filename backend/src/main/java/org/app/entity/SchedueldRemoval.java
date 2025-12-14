package org.app.entity;

import jakarta.persistence.*;
import java.time.*;
import lombok.*;
import lombok.experimental.*;

@Getter
@Embeddable
@Accessors(fluent = true, chain = false)
public class SchedueldRemoval {

    @Column(nullable = false)
    private boolean doesRemovalScheduled;

    private LocalDateTime requestedAt;

    private LocalDateTime scheduedAt;

    protected SchedueldRemoval() {
        this.doesRemovalScheduled = false;
        this.requestedAt = this.scheduedAt = null;
    }

    protected SchedueldRemoval(
            LocalDateTime requestedAt, LocalDate scheduledDate
    ) {
        this.doesRemovalScheduled = true;
        this.requestedAt = requestedAt;
        this.scheduedAt = scheduledDate.atStartOfDay().minusMinutes(5L);
    }

    public static SchedueldRemoval notScheduled() {
        return new SchedueldRemoval();
    }

    public static SchedueldRemoval scheduled(
            LocalDateTime removalRequestedAt, LocalDate removalScheduledDate
    ) {
        return new SchedueldRemoval(removalRequestedAt, removalScheduledDate);
    }
}
