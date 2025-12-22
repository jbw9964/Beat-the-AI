package org.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;

@Getter
@Entity
@Table(name = "problem_reward")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProblemReward extends AuditingCreation {

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
            name = "actual_reward_image_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "FK__REWARD_TO_ACTUAL_IMAGE")
    )
    private ActualRewardImage actualRewardImage;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "overview_reward_image_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "FK__REWARD_TO_OVERVIEW_IMAGE")
    )
    private OverviewRewardImage overviewRewardImage;

    @Column(nullable = false)
    @Accessors(fluent = true, chain = false)
    private boolean hasTransferred;

    public ProblemReward(
            Problem problem, String description,
            ActualRewardImage actualRewardImage,
            OverviewRewardImage overviewRewardImage
    ) {
        this(
                problem, description, false,
                actualRewardImage, overviewRewardImage
        );
    }

    public ProblemReward(
            Problem problem, String description, boolean hasTransferred,
            ActualRewardImage actualRewardImage,
            OverviewRewardImage overviewRewardImage
    ) {
        this.problem = problem;
        this.description = description;
        this.hasTransferred = hasTransferred;
        this.actualRewardImage = actualRewardImage;
        this.overviewRewardImage = overviewRewardImage;
    }
}
