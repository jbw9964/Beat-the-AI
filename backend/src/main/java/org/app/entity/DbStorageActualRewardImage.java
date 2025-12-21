package org.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "db_storage_actual_reward_image")
@DiscriminatorValue(value = RewardStorageType.Names.DB)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DbStorageActualRewardImage extends ActualRewardImage {

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private byte[] actualImage;

    public DbStorageActualRewardImage(byte[] actualImage) {
        this.actualImage = actualImage;
    }

    @Override
    public RewardStorageType getStorageType() {
        return RewardStorageType.DB;
    }
}
