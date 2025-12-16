package org.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "problem_aggregation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProblemAggregation extends BaseTimeEntity {

    @Id
    @SuppressWarnings("unused")
    private Long problemId;

    @MapsId
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "problem_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "FK__PROBLEM_AGGREGATION_TO_PROBLEM")
    )
    private Problem problem;

    @Embedded
    private AggregatedProblemRatingInfo ratingInfo;

    public ProblemAggregation(Problem problem) {
        this(
                problem,
                new AggregatedProblemPlayInfo(),
                new AggregatedProblemRatingInfo()
        );
    }

    /*
    어떤 정보들을 집합시켜놔야 할까?
    1. 문제 평가 개수
    2. 문제 평가 평균
    3. 문제 플레이 수
    4. 문제에 설정된 보상 개수
    5. 문제에 설정된 시나리오 개수

    이들은 언제 업데이트 되어야 할까?
    1 & 2 : 누군가 문제 평가했을 때
    3 : 누군가 문제 플레이 시작했을 때
    4 : 문제 생성자가 보상 생성, update, 삭제 등 했을 때
    5 : 문제 생성자가 시나리오 정보 수정했을 때
     */

    @Embedded
    private AggregatedProblemPlayInfo playInfo;

    public ProblemAggregation(
            Problem problem,
            AggregatedProblemPlayInfo playInfo,
            AggregatedProblemRatingInfo ratingInfo
    ) {
        // 원래 this.problemId = problem.getId() 로 하려 했었는데
        // 테스트 중 org.springframework.orm.ObjectOptimisticLockingFailureException:
        // Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect)
        // 에러 발생함.
        // 아마 entity id 존재하면 일반적으로 deteached 상태로 간주되는데
        // 이게 @MapsId 랑 뭔가 충돌? 나서 이렇게 발생하는 듯.
        //this.problemId = problem.getId();

        this.problem = problem;
        problem.setProblemAggregation(this);

        this.playInfo = playInfo != null ?
                playInfo : new AggregatedProblemPlayInfo();
        this.ratingInfo = ratingInfo != null ?
                ratingInfo : new AggregatedProblemRatingInfo();
    }

    public void prepareAggregationRemoval() {
        Problem realted = this.getProblem();
        realted.removeProblemAggregation();
        this.problem = null;
    }
}
