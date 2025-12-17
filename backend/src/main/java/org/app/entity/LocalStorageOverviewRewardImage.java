package org.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "local_storage_overview_reward_image")
@DiscriminatorValue(value = RewardStorageType.Names.LOCAL_STORAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LocalStorageOverviewRewardImage extends OverviewRewardImage {

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private byte[] overviewImage;

    public LocalStorageOverviewRewardImage(byte[] overviewImage) {
        this.overviewImage = overviewImage;
    }

    @Override
    public RewardStorageType getStorageType() {
        return RewardStorageType.LOCAL_STORAGE;
    }
}
