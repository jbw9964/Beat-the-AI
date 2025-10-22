package org.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Embeddable
public final class EmbeddedSettingInfo {

    private boolean notiRating;

    private boolean notiSolved;

    private boolean notiRewardGain;

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

    public EmbeddedSettingInfo(EmbeddedSettingInfo copy) {
        if (copy == null) {
            throw new IllegalArgumentException("copy cannot be null");
        }

        this.notiRating = copy.notiRating;
        this.notiSolved = copy.notiSolved;
        this.notiRewardGain = copy.notiRewardGain;
        this.notiRewardDispose = copy.notiRewardDispose;
    }
}
