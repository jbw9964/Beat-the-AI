package org.app.config.domain.image.internal.invoker;

import org.app.config.domain.image.*;
import org.app.config.domain.image.internal.*;
import org.app.config.domain.image.internal.repository.*;
import org.app.entity.*;
import org.springframework.stereotype.*;

@Component
public non-sealed class DbStorageInvockingStrategy
        extends AbstractReardImageInvokingStrategy<DbStorageActualRewardImage,
        DbStorageOverviewRewardImage> {

    private final EntityEditor entityEditor;

    public DbStorageInvockingStrategy(EntityEditor entityEditor) {
        super(
                DbStorageActualRewardImage.class,
                DbStorageOverviewRewardImage.class
        );
        this.entityEditor = entityEditor;
    }

    @Override
    public byte[] getImageFrom(ActualRewardImage actualRewardImage)
            throws RewardStorageTypeMismatchException {

        super.throwExOnTypeMismatch(actualRewardImage);

        return super.castEntity(actualRewardImage).getActualImage();
    }

    @Override
    public byte[] getImageFrom(OverviewRewardImage overviewRewardImage)
            throws RewardStorageTypeMismatchException {

        super.throwExOnTypeMismatch(overviewRewardImage);

        return super.castEntity(overviewRewardImage).getOverviewImage();
    }

    @Override
    public RewardImageInfo saveRewardImages(
            byte[] actualRewardImage, byte[] overviewRewardImage
    )
            throws RewardStorageTypeMismatchException {

        DbStorageActualRewardImage newAEntity
                = new DbStorageActualRewardImage(actualRewardImage);
        DbStorageOverviewRewardImage newOEntity
                = new DbStorageOverviewRewardImage(overviewRewardImage);

        return entityEditor.saveEntities(newAEntity, newOEntity);
    }

    @Override
    public Long delete(ActualRewardImage removalEntity)
            throws RewardStorageTypeMismatchException {

        super.throwExOnTypeMismatch(removalEntity);
        super.castEntity(removalEntity);

        Long id = removalEntity.getId();

        return entityEditor.requestActualRewardImageRemoval(
                id, removalEntity.getStorageType()
        );
    }

    @Override
    public Long delete(OverviewRewardImage removalEntity)
            throws RewardStorageTypeMismatchException {

        super.throwExOnTypeMismatch(removalEntity);
        super.castEntity(removalEntity);

        Long id = removalEntity.getId();

        return entityEditor.requestOverviewRewardImageRemoval(
                id, removalEntity.getStorageType()
        );
    }

    @Override
    public RewardStorageType handleableType() {
        return RewardStorageType.DB;
    }
}
