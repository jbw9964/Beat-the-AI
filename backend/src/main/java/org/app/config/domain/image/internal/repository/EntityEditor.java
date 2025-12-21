package org.app.config.domain.image.internal.repository;

import java.time.*;
import lombok.*;
import org.app.config.domain.*;
import org.app.config.domain.image.*;
import org.app.entity.*;
import org.app.util.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

@Component
@RequiredArgsConstructor
public class EntityEditor {

    private final ActualRewardImageRepository actualRewardImageRepo;
    private final OverviewRewardImageRepository overviewRewardImageRepo;

    private final DateTimeProvider dateTimeProvider;
    private final SoftDeletePolicy softDeletePolicy;

    @Transactional
    public RewardImageInfo saveEntities(
            ActualRewardImage newAEntity, OverviewRewardImage newOEntity
    ) {
        return new RewardImageInfo(
                actualRewardImageRepo.save(newAEntity).getId(),
                overviewRewardImageRepo.save(newOEntity).getId()
        );
    }

    @Transactional
    public Long requestActualRewardImageRemoval(Long id, RewardStorageType storageType) {
        ActualRewardImage removalEntity = actualRewardImageRepo.findById(id)
                .orElseThrow(ImageNotFoundException::new);

        LocalDateTime now = dateTimeProvider.localDateTimeNow();
        LocalDate removalDate = softDeletePolicy.getRemovalDateOn(now);

        switch (storageType) {
            case DB -> actualRewardImageRepo.deleteById(id);
            case SERVER -> {
                ServerStorageActualRewardImage casted
                        = (ServerStorageActualRewardImage) removalEntity;

                if (casted.doesRemovalScheduled()) {
                    throw new ImageNotFoundException();
                }

                casted.reserveRemoval(now, removalDate);
            }
            case AWS_S3 -> {
                AwsS3ActualRewardImage casted
                        = (AwsS3ActualRewardImage) removalEntity;

                if (casted.doesRemovalScheduled()) {
                    throw new ImageNotFoundException();
                }

                casted.reserveRemoval(now, removalDate);
            }
        }

        return removalEntity.getId();
    }

    @Transactional
    public Long requestOverviewRewardImageRemoval(Long id, RewardStorageType storageType) {

        OverviewRewardImage removalEntity = overviewRewardImageRepo.findById(id)
                .orElseThrow(ImageNotFoundException::new);

        LocalDateTime now = dateTimeProvider.localDateTimeNow();
        LocalDate removalDate = softDeletePolicy.getRemovalDateOn(now);

        switch (storageType) {
            case DB -> overviewRewardImageRepo.deleteById(id);
            case SERVER -> {
                ServerStorageOverviewRewardImage casted
                        = (ServerStorageOverviewRewardImage) removalEntity;
                casted.reserveRemoval(now, removalDate);
            }
            case AWS_S3 -> {
                AwsS3OverviewRewardImage casted
                        = (AwsS3OverviewRewardImage) removalEntity;
                casted.reserveRemoval(now, removalDate);
            }
        }

        return removalEntity.getId();
    }
}
