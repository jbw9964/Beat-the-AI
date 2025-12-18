package org.app.entity;

import jakarta.persistence.*;
import java.time.*;
import lombok.*;

@Getter
@Entity
@Table(
        name = "server_storage_overview_reward_image",
        uniqueConstraints = @UniqueConstraint(
                name = "UK__SERVER_OVERVIEW_IMAGE_PATH",
                columnNames = "overview_image_path"
        )
)
@DiscriminatorValue(value = RewardStorageType.Names.SERVER)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerStorageOverviewRewardImage extends OverviewRewardImage implements SoftDelete {

    @Column(
            name = "overview_image_path",
            length = 500, nullable = false
    )
    private String overviewImagePath;

    @Embedded
    private ScheduledRemoval scheduledRemoval;

    public ServerStorageOverviewRewardImage(String overviewImagePath) {
        this.overviewImagePath = overviewImagePath;
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
