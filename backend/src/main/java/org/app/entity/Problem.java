package org.app.entity;

import jakarta.persistence.*;
import java.time.*;
import lombok.*;

@Getter
@Entity
@Table(name = "problem")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuppressWarnings({"DefaultAnnotationParam", "UnusedReturnValue"})
public class Problem extends BaseTimeEntity implements SoftDelete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "FK__PROBLEM_TO_USER")
    )
    private User user;

    @Column(length = 50, nullable = false)
    private String title;

    @Column(length = 255)
    private String description;

    @Column(length = 50)
    private String rewardMessage;

    @Column(nullable = false)
    private int numOfScenariosToGetReward;

    @Column(nullable = false)
    private int numOfScenariosToFailPlay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProblemVisibility visibility;

    @Column(nullable = false)
    private int numOfTotalScenarios;

    @Lob
    @Column(nullable = false)
    @Basic(fetch = FetchType.LAZY)
    private String serializedScenarioInfo;

    @Column(nullable = false)
    private int numOfRewardSets;

    // TODO : Public 속성인 문제에 대해서만 집계 정보 존재해야 함.
    @OneToOne(
            fetch = FetchType.EAGER, mappedBy = "problem"
            // TODO : gemini 말로는 remove 해도 jpa 가 똑똑하게 먼저 삭제해 준다 함. 나중에 테스트 만들면서 확인해보고 정상 작동하면 적용하기.
            //, cascade = {CascadeType.PERSIST, CascadeType.REMOVE}
    )
    @Setter(AccessLevel.PACKAGE)
    private ProblemAggregation problemAggregation;

    @Embedded
    private SchedueldRemoval schedueldRemoval;

    public Problem(
            User user, String title, String description, String rewardMessage,
            int numOfScenariosToGetReward, int numOfScenariosToFailPlay,
            ProblemVisibility visibility, int numOfTotalScenarios,
            String serializedScenarioInfo
    ) {
        this(
                user, title, description, rewardMessage,
                numOfScenariosToGetReward, numOfScenariosToFailPlay,
                visibility, numOfTotalScenarios,
                serializedScenarioInfo, 0
        );
    }

    public Problem(
            User user, String title, String description, String rewardMessage,
            int numOfScenariosToGetReward, int numOfScenariosToFailPlay,
            ProblemVisibility visibility, int numOfTotalScenarios,
            String serializedScenarioInfo, int numOfRewardSets
    ) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.rewardMessage = rewardMessage;
        this.numOfScenariosToGetReward = numOfScenariosToGetReward;
        this.numOfScenariosToFailPlay = numOfScenariosToFailPlay;
        this.visibility = visibility;
        this.numOfTotalScenarios = numOfTotalScenarios;
        this.serializedScenarioInfo = serializedScenarioInfo;
        this.numOfRewardSets = numOfRewardSets;
        this.schedueldRemoval = SchedueldRemoval.notScheduled();
    }

    public Problem changeTitle(String title) {
        this.title = title;
        return this;
    }

    public Problem changeDescription(String description) {
        this.description = description;
        return this;
    }

    public Problem changeRewardMessage(String rewardMessage) {
        this.rewardMessage = rewardMessage;
        return this;
    }

    public Problem changeNumOfScenariosToGetReward(int numOfScenariosToGetReward) {
        this.numOfScenariosToGetReward = numOfScenariosToGetReward;
        return this;
    }

    public Problem changeNumOfScenariosToFailPlay(int numOfScenariosToFailPlay) {
        this.numOfScenariosToFailPlay = numOfScenariosToFailPlay;
        return this;
    }

    public void changeVisibility(ProblemVisibility visibility) {
        this.visibility = visibility;
    }

    public Problem changeSerializedScenarioInfo(
            int numOfTotalScenarios, String serializedScenarioInfo
    ) {
        this.numOfTotalScenarios = numOfTotalScenarios;
        this.serializedScenarioInfo = serializedScenarioInfo;
        return this;
    }

    public void increaseNumOfRewardSets() {
        this.numOfRewardSets++;
    }

    public void increaseNumOfRewardSets(int addition) {
        this.numOfRewardSets += addition;
    }

    public void decreaseNumOfRewardSets() {
        this.numOfRewardSets--;
    }

    public void decreaseNumOfRewardSets(int subtraction) {
        this.numOfRewardSets -= subtraction;
    }

    public void reserveRemoval(
            LocalDateTime now, LocalDate scheduledRemovalDate
    ) {
        this.schedueldRemoval = SchedueldRemoval.scheduled(now, scheduledRemovalDate);
    }
}
