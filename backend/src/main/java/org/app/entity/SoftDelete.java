package org.app.entity;

public interface SoftDelete {

    ScheduledRemoval getScheduledRemoval();

    default boolean doesRemovalScheduled() {
        ScheduledRemoval scheduled = this.getScheduledRemoval();
        return scheduled != null && scheduled.doesRemovalScheduled();
    }
}
