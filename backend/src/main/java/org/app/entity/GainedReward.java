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
            name = "play_record_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "FK__GAINED_REWARD_TO_PLAY_RECORD")
    )
    private PlayRecord playRecord;

    // 혹시 몰라 넣은 reward 느슨한 결합
    @Column(updatable = false)
    private Long rewardId;

    @Column(length = 50, updatable = false)
    private String description;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "actual_reward_image_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "FK__GAINED_REWARD_TO_ACTUAL_IMAGE")
    )
    private ActualRewardImage actualRewardImage;

    public GainedReward(
            User user, PlayRecord playRecord,
            Long rewardId, String description,
            ActualRewardImage actualRewardImage
    ) {
        this.user = user;
        this.playRecord = playRecord;
        this.rewardId = rewardId;
        this.description = description;
        this.actualRewardImage = actualRewardImage;
    }
}
