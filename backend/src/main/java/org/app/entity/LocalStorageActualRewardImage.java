package org.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "local_storage_actual_reward_image")
@DiscriminatorValue(value = RewardStorageType.Names.LOCAL_STORAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LocalStorageActualRewardImage extends ActualRewardImage {

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private byte[] actualImage;

    public LocalStorageActualRewardImage(byte[] actualImage) {
        this.actualImage = actualImage;
    }

    @Override
    public RewardStorageType getStorageType() {
        return RewardStorageType.LOCAL_STORAGE;
    }
}
