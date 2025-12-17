package org.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;

@Getter
@Entity
@Table(name = "reward")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reward extends AuditingCreation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "problem_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "FK__REWARD_TO_PROBLEM")
    )
    private Problem problem;

    @Column(length = 50)
    private String description;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "overview_reward_image_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "FK__REWARD_TO_OVERVIEW_IMAGE")
    )
    private OverviewRewardImage overviewRewardImage;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "actual_reward_image_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "FK__REWARD_TO_ACTUAL_IMAGE")
    )
    private ActualRewardImage actualRewardImage;

    @Column(nullable = false)
    @Accessors(fluent = true, chain = false)
    private boolean hasTransferred;

    public Reward(
            Problem problem,
            OverviewRewardImage overviewRewardImage,
            ActualRewardImage actualRewardImage
    ) {
        this(
                problem, null, false,
                overviewRewardImage, actualRewardImage
        );
    }

    public Reward(
            Problem problem, String description, boolean hasTransferred,
            OverviewRewardImage overviewRewardImage,
            ActualRewardImage actualRewardImage
    ) {
        this.problem = problem;
        this.description = description;
        this.hasTransferred = hasTransferred;
        this.overviewRewardImage = overviewRewardImage;
        this.actualRewardImage = actualRewardImage;
    }
}
