package org.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "db_storage_overview_reward_image")
@DiscriminatorValue(value = RewardStorageType.Names.DB)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DbStorageOverviewRewardImage extends OverviewRewardImage {

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private byte[] overviewImage;

    public DbStorageOverviewRewardImage(byte[] overviewImage) {
        this.overviewImage = overviewImage;
    }

    @Override
    public RewardStorageType getStorageType() {
        return RewardStorageType.DB;
    }
}
