package org.app.config.domain.image.internal.invoker;

import org.app.config.domain.image.internal.*;
import org.app.entity.*;
import org.app.util.exception.*;

public abstract sealed
class AbstractReardImageInvokingStrategy<A extends ActualRewardImage, O extends OverviewRewardImage>
        implements RewardImageInvokerStrategy
        permits DbStorageInvockingStrategy, ServerStorageInvockingStrategy {

    private final Class<A> actualRewardImageEntityClass;
    private final Class<O> overviewRewardImageEntityClass;

    protected AbstractReardImageInvokingStrategy(
            Class<A> actualRewardImageEntityClass,
            Class<O> overviewRewardImageEntityClass
    ) {
        this.actualRewardImageEntityClass = actualRewardImageEntityClass;
        this.overviewRewardImageEntityClass = overviewRewardImageEntityClass;
    }

    protected final void throwExOnTypeMismatch(StorageType storageType) {
        RewardStorageType given = storageType.getStorageType();
        RewardStorageType handleableType = this.handleableType();

        if (!handleableType.equals(given)) {
            throw new RewardStorageTypeMismatchException(String.format(
                    "Expected to get [%s]-typed reward storage, "
                    + "but encountered [%s] type on invoking strategy: %s",
                    handleableType, given, this.getClass().getSimpleName()
            ));
        }
    }

    protected final A castEntity(
            ActualRewardImage entity
    ) throws InternalServerErrorException {
        try {
            return actualRewardImageEntityClass.cast(entity);
        } catch (ClassCastException e) {
            throw new InternalServerErrorException(
                    String.format(
                            "Failed to cast entity (%s) to clazz (%s)",
                            entity.getClass().getSimpleName(),
                            actualRewardImageEntityClass.getSimpleName()
                    ),
                    "보상 이미지를 처리하던중 예상치 못한 에러가 발생했습니다.", e
            );
        }
    }

    protected final O castEntity(
            OverviewRewardImage entity
    ) {
        try {
            return overviewRewardImageEntityClass.cast(entity);
        } catch (ClassCastException e) {
            throw new InternalServerErrorException(
                    String.format(
                            "Failed to cast entity (%s) to clazz (%s)",
                            entity.getClass().getSimpleName(),
                            actualRewardImageEntityClass.getSimpleName()
                    ),
                    "보상 이미지를 처리하던중 예상치 못한 에러가 발생했습니다.", e
            );
        }
    }
}
