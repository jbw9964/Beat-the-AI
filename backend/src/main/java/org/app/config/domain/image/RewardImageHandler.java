package org.app.config.domain.image;

import org.app.entity.*;
import org.app.util.exception.*;
import org.springframework.modulith.*;

/*
TODO : 역시 찾아보니 이미지 DB 저장하는것 보다는 서버 파일 시스템에 저장하는게 좋다고 함.
    시간 되면 성능 테스트까지 해보면 좋을듯?
 */
@NamedInterface
public interface RewardImageHandler {

    byte[] getActualRewardImage(Long actualRewardImageId)
            throws ImageNotFoundException,
            FailedToFindImageOnServerException,
            FailedToGetFileOnServerException,
            BadGatewayException,
            GatewayTimeoutException;

    byte[] getOverviewRewardImage(Long overviewRewardImageId)
            throws ImageNotFoundException,
            FailedToFindImageOnServerException,
            FailedToGetFileOnServerException,
            BadGatewayException,
            GatewayTimeoutException;

    RewardImageInfo saveRewardImages(
            byte[] actualRewardImage,
            byte[] overviewRewardImage
    ) throws FailedToSaveImageToServerException,
            BadGatewayException, GatewayTimeoutException;

    RewardImageInfo deleteRewardImages(RewardImageInfo removalInfo)
            throws ImageNotFoundException;

    RewardStorageType savingStorageType();
}
