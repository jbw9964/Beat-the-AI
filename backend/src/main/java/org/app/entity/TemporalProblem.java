package org.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "temporal_problem")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuppressWarnings("UnusedReturnValue")
public class TemporalProblem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id", updatable = false,
            foreignKey = @ForeignKey(name = "FK__TEMPORAL_PROBLEM_TO_USER")
    )
    private User user;

    @Column(length = 50, nullable = false)
    private String title;

    @Column(length = 255)
    private String description;

    @Column(length = 50)
    private String rewardMessage;

    private Integer numOfScenariosToGetReward;

    private Integer numOfScenariosToFailPlay;

    @Enumerated(EnumType.STRING)
    private ProblemVisibility visibility;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String serializedScenarioInfo;

    public TemporalProblem(
            User user, String title, String description, String rewardMessage,
            Integer numOfScenariosToGetReward, Integer numOfScenariosToFailPlay,
            ProblemVisibility visibility, String serializedScenarioInfo
    ) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.rewardMessage = rewardMessage;
        this.numOfScenariosToGetReward = numOfScenariosToGetReward;
        this.numOfScenariosToFailPlay = numOfScenariosToFailPlay;
        this.visibility = visibility;
        this.serializedScenarioInfo = serializedScenarioInfo;
    }

    public TemporalProblem changeTitle(String title) {
        this.title = title;
        return this;
    }

    public TemporalProblem changeDescription(String description) {
        this.description = description;
        return this;
    }

    public TemporalProblem changeRewardMessage(String rewardMessage) {
        this.rewardMessage = rewardMessage;
        return this;
    }

    public TemporalProblem changeNumOfScenariosToGetReward(Integer numOfScenariosToGetReward) {
        this.numOfScenariosToGetReward = numOfScenariosToGetReward;
        return this;
    }

    public TemporalProblem changeNumOfScenariosToFailPlay(Integer numOfScenariosToFailPlay) {
        this.numOfScenariosToFailPlay = numOfScenariosToFailPlay;
        return this;
    }

    public TemporalProblem changeVisibility(ProblemVisibility visibility) {
        this.visibility = visibility;
        return this;
    }

    public TemporalProblem changeSerializedScenarioInfo(String serializedScenarioInfo) {
        this.serializedScenarioInfo = serializedScenarioInfo;
        return this;
    }
}
