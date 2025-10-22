package org.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(
        name = "reward",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK__REWARD_ORIGIN_LOCATION",
                        columnNames = "origin_location"
                ),
                @UniqueConstraint(
                        name = "UK__REWARD_OVERVIEW_LOCATION",
                        columnNames = "overview_location"
                )
        }
)
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

    @Column(name = "origin_location", length = 255, nullable = false, updatable = false)
    private String originLocation;

    @Column(name = "overview_location", length = 255, nullable = false, updatable = false)
    private String overviewLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RewardStorageType storageType;

    @Column(nullable = false)
    private boolean hasTransferred;

    public Reward(
            Problem problem, String originLocation,
            String overviewLocation, RewardStorageType storageType
    ) {
        this.problem = problem;
        this.originLocation = originLocation;
        this.overviewLocation = overviewLocation;
        this.storageType = storageType;
        this.hasTransferred = false;
    }
}
