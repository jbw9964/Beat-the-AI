package org.app.config.domain.image.internal;

import org.app.config.domain.image.*;
import org.app.entity.*;
import org.app.util.exception.*;


public interface RewardImageInvokerStrategy {

    byte[] getImageFrom(ActualRewardImage actualRewardImage)
            throws ImageNotFoundException,
            RewardStorageTypeMismatchException,
            FailedToFindImageOnServerException,
            FailedToGetFileOnServerException,
            BadGatewayException, GatewayTimeoutException;

    byte[] getImageFrom(OverviewRewardImage overviewRewardImage)
            throws ImageNotFoundException,
            RewardStorageTypeMismatchException,
            FailedToFindImageOnServerException,
            FailedToGetFileOnServerException,
            BadGatewayException, GatewayTimeoutException;

    RewardImageInfo saveRewardImages(
            byte[] actualRewardImage, byte[] overviewRewardImage
    ) throws RewardStorageTypeMismatchException,
            FailedToSaveImageToServerException,
            BadGatewayException, GatewayTimeoutException;

    Long delete(ActualRewardImage removalEntity)
            throws RewardStorageTypeMismatchException;

    Long delete(OverviewRewardImage removalEntity)
            throws RewardStorageTypeMismatchException;

    RewardStorageType handleableType();

}
