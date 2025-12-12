package org.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;

@Getter
@Embeddable
@Accessors(fluent = true, chain = false)
public final class EmbeddedSettingInfo {

    @Column(nullable = false)
    private boolean notiRating;

    @Column(nullable = false)
    private boolean notiSolved;

    @Column(nullable = false)
    private boolean notiRewardGain;

    @Column(nullable = false)
    private boolean notiRewardDispose;

    public EmbeddedSettingInfo() {
        this(
                false, false,
                false, false
        );
    }

    public EmbeddedSettingInfo(
            boolean notiRating, boolean notiSolved,
            boolean notiRewardGain, boolean notiRewardDispose
    ) {
        this.notiRating = notiRating;
        this.notiSolved = notiSolved;
        this.notiRewardGain = notiRewardGain;
        this.notiRewardDispose = notiRewardDispose;
    }

    public EmbeddedSettingInfo(@NonNull EmbeddedSettingInfo copy) {
        this.notiRating = copy.notiRating;
        this.notiSolved = copy.notiSolved;
        this.notiRewardGain = copy.notiRewardGain;
        this.notiRewardDispose = copy.notiRewardDispose;
    }
}
