package org.app.entity;

import jakarta.persistence.*;
import java.time.*;
import lombok.*;

@Getter
@Entity
@Table(
        name = "server_storage_actual_reward_image",
        uniqueConstraints = @UniqueConstraint(
                name = "UK__SERVER_ACTUAL_IMAGE_PATH",
                columnNames = "actual_image_path"
        )
)
@DiscriminatorValue(value = RewardStorageType.Names.SERVER)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerStorageActualRewardImage extends ActualRewardImage implements SoftDelete {

    @Column(
            name = "actual_image_path",
            length = 500, nullable = false
    )
    private String actualImagePath;

    @Embedded
    private ScheduledRemoval scheduledRemoval;

    public ServerStorageActualRewardImage(String actualImagePath) {
        this.actualImagePath = actualImagePath;
        this.scheduledRemoval = ScheduledRemoval.notScheduled();
    }

    @Override
    public RewardStorageType getStorageType() {
        return RewardStorageType.SERVER;
    }

    public void reserveRemoval(
            LocalDateTime now, LocalDate scheduledRemovalDate
    ) {
        this.scheduledRemoval = ScheduledRemoval.scheduled(now, scheduledRemovalDate);
    }
}
