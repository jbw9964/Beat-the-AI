package org.app.entity;

import jakarta.persistence.*;
import java.time.*;
import lombok.*;

@Getter
@Entity
@Table(
        name = "aws_s3_overview_reward_image",
        uniqueConstraints = @UniqueConstraint(
                name = "UK__AWS_S3_OVERVIEW_IMAGE_PATH",
                columnNames = "overview_image_path"
        )
)
@DiscriminatorValue(value = RewardStorageType.Names.AWS_S3)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AwsS3OverviewRewardImage extends OverviewRewardImage implements SoftDelete {

    @Column(
            name = "overview_image_path",
            length = 500, nullable = false
    )
    private String overviewImagePath;

    @Embedded
    private ScheduledRemoval scheduledRemoval;

    public AwsS3OverviewRewardImage(String overviewImagePath) {
        this.overviewImagePath = overviewImagePath;
        this.scheduledRemoval = ScheduledRemoval.notScheduled();
    }

    @Override
    public RewardStorageType getStorageType() {
        return RewardStorageType.AWS_S3;
    }

    public void reserveRemoval(
            LocalDateTime now, LocalDate scheduledRemovalDate
    ) {
        this.scheduledRemoval = ScheduledRemoval.scheduled(now, scheduledRemovalDate);
    }
}
