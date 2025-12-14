package org.app.entity;

public interface SoftDelete {

    SchedueldRemoval getSchedueldRemoval();

    default boolean doesRemovalScheduled() {
        SchedueldRemoval scheduled = this.getSchedueldRemoval();
        return scheduled != null && scheduled.doesRemovalScheduled();
    }
}
