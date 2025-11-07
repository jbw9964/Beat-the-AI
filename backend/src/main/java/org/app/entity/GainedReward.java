package org.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "gained_reward")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GainedReward extends AuditingCreation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "FK__GAINED_REWARD_TO_USER")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "record_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "FK__GAINED_REWARD_TO_PLAY_RECORD")
    )
    private PlayRecord playRecord;

    // 혹시 몰라 넣은 reward 느슨한 결합
    @Column(updatable = false)
    private Long rewardId;

    @Column(length = 50, updatable = false)
    private String description;

    @Column(length = 255, nullable = false, updatable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private RewardStorageType storageType;

    public GainedReward(
            User user, PlayRecord playRecord,
            String location, RewardStorageType storageType
    ) {
        this.user = user;
        this.playRecord = playRecord;
        this.location = location;
        this.storageType = storageType;
    }

    public GainedReward(
            User user, PlayRecord playRecord, Long rewardId,
            String description, String location,
            RewardStorageType storageType
    ) {
        this.user = user;
        this.playRecord = playRecord;
        this.rewardId = rewardId;
        this.description = description;
        this.location = location;
        this.storageType = storageType;
    }
}
