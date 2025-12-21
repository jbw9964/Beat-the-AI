package org.app.config.domain.image.internal.handler;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.app.config.domain.image.*;
import org.app.config.domain.image.internal.*;
import org.app.config.domain.image.internal.repository.*;
import org.app.entity.*;
import org.app.util.exception.*;

public abstract class AbstractRewardImageHandler
        implements RewardImageHandler {

    private final ActualRewardImageRepository actualImageRepo;
    private final OverviewRewardImageRepository overviewImageRepo;
    private final Map<RewardStorageType, RewardImageInvokerStrategy> strategyMap;

    protected AbstractRewardImageHandler(
            ActualRewardImageRepository actualImageRepo,
            OverviewRewardImageRepository overviewImageRepo,
            List<RewardImageInvokerStrategy> strategies
    ) {
        this.actualImageRepo = actualImageRepo;
        this.overviewImageRepo = overviewImageRepo;
        this.strategyMap = strategies.stream().collect(Collectors.toMap(
                RewardImageInvokerStrategy::handleableType, Function.identity()
        ));
    }

    @Override
    public byte[] getActualRewardImage(Long actualRewardImageId)
            throws ImageNotFoundException, BadGatewayException, GatewayTimeoutException {

        ActualRewardImage find = this.findAcutalOrThrow(actualRewardImageId);
        RewardStorageType storageType = find.getStorageType();

        return strategyMap.get(storageType).getImageFrom(find);
    }

    @Override
    public byte[] getOverviewRewardImage(Long overviewRewardImageId)
            throws ImageNotFoundException, BadGatewayException, GatewayTimeoutException {

        OverviewRewardImage find = this.findOverviewOrThrow(overviewRewardImageId);
        RewardStorageType storageType = find.getStorageType();

        return strategyMap.get(storageType).getImageFrom(find);
    }

    @Override
    public RewardImageInfo saveRewardImages(
            byte[] actualRewardImage, byte[] overviewRewardImage
    )
            throws FailedToSaveImageToServerException,
            BadGatewayException, GatewayTimeoutException {

        RewardStorageType savingStorageType = this.savingStorageType();

        return strategyMap.get(savingStorageType).saveRewardImages(
                actualRewardImage, overviewRewardImage
        );
    }

    @Override
    public RewardImageInfo deleteRewardImages(RewardImageInfo removalInfo)
            throws ImageNotFoundException {

        Long actualRewardImageId = removalInfo.actualRewardImageId();
        Long overviewRewardImageId = removalInfo.overviewRewardImageId();

        ActualRewardImage findActual = this.findAcutalOrThrow(actualRewardImageId);
        OverviewRewardImage findOverview = this.findOverviewOrThrow(overviewRewardImageId);

        RewardStorageType storageTypeA = findActual.getStorageType();
        RewardStorageType storageTypeO = findOverview.getStorageType();

        actualRewardImageId = strategyMap.get(storageTypeA).delete(findActual);
        overviewRewardImageId = strategyMap.get(storageTypeO).delete(findOverview);

        return new RewardImageInfo(actualRewardImageId, overviewRewardImageId);
    }

    private ActualRewardImage findAcutalOrThrow(Long id) {
        return actualImageRepo.findById(id).orElseThrow(ImageNotFoundException::new);
    }

    private OverviewRewardImage findOverviewOrThrow(Long id) {
        return overviewImageRepo.findById(id).orElseThrow(ImageNotFoundException::new);
    }
}
