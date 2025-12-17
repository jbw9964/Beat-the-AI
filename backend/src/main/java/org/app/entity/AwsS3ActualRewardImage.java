package org.app.entity;

import jakarta.persistence.*;
import java.time.*;
import lombok.*;

@Getter
@Entity
@Table(
        name = "aws_s3_actual_reward_image",
        uniqueConstraints = @UniqueConstraint(
                name = "UK__AWS_S3_ACTUAL_IMAGE_PATH",
                columnNames = "actual_image_path"
        )
)
@DiscriminatorValue(value = RewardStorageType.Names.AWS_S3)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AwsS3ActualRewardImage extends ActualRewardImage implements SoftDelete {

    @Column(
            name = "actual_image_path",
            length = 500, nullable = false
    )
    private String actualImagePath;

    @Embedded
    private ScheduledRemoval scheduledRemoval;

    public AwsS3ActualRewardImage(String actualImagePath) {
        this.actualImagePath = actualImagePath;
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
