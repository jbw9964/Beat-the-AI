package org.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "temporal_problem")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    private int numOfScenariosToGetReward;

    private int numOfScenariosToFailPlay;

    @Enumerated(EnumType.STRING)
    private ProblemVisibility visibility;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String serializedScenarioInfo;

    /*
        TODO : 직렬화된 시나리오 info 관련해서 작업 필요함.
        1. 엔티티 serializedScenarioInfo 를 List<ScenarioInfo> 로 제공하는 메서드
        2. List<ScenarioInfo> 를 직렬화해 serializedScenarioInfo 로 저장하는 메서드
        위 과정에서 ScenarioInfo 의 scenarioOrder 잘 생각해서 작업해야 함.
     */

    public TemporalProblem(User user, String title) {
        this.user = user;
        this.title = title;
    }

    // TODO : 임시저장 바꿨을 때 각 속성 잘 바꿔주는 기능 구성해야 함.
}
