package org.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "problem")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuppressWarnings("DefaultAnnotationParam")
public class Problem extends BaseTimeEntity {

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

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String serializedScenarioInfo;

    // TODO : Public 속성인 문제에 대해서만 집계 정보 존재해야 함.
    @OneToOne(
            fetch = FetchType.EAGER, mappedBy = "problem"
            // TODO : gemini 말로는 remove 해도 jpa 가 똑똑하게 먼저 삭제해 준다 함. 나중에 테스트 만들면서 확인해보고 정상 작동하면 적용하기.
            //, cascade = {CascadeType.PERSIST, CascadeType.REMOVE}
    )
    private ProblemAggregation problemAggregation;

    public Problem(
            User user, String title,
            int numOfScenariosToGetReward, int numOfScenariosToFailPlay,
            ProblemVisibility visibility, String serializedScenarioInfo
    ) {
        this.user = user;
        this.title = title;
        this.numOfScenariosToGetReward = numOfScenariosToGetReward;
        this.numOfScenariosToFailPlay = numOfScenariosToFailPlay;
        this.visibility = visibility;
        this.serializedScenarioInfo = serializedScenarioInfo;
    }

    public void changeDescription(String newDescription) {
        this.description = newDescription;
    }

    public void changeRewardMessage(String rewardMessage) {
        this.rewardMessage = rewardMessage;
    }

    /*
        TODO : 직렬화된 시나리오 info 관련해서 작업 필요함.
        1. 엔티티 serializedScenarioInfo 를 List<ScenarioInfo> 로 제공하는 메서드
        2. List<ScenarioInfo> 를 직렬화해 serializedScenarioInfo 로 저장하는 메서드
        위 과정에서 ScenarioInfo 의 scenarioOrder 잘 생각해서 작업해야 함.
     */

}
