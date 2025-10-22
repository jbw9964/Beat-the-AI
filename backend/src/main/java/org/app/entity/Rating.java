package org.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(
        name = "rating",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK__RATING_PROBLEM_WITH_USER",
                        columnNames = {"problem_id", "user_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rating extends BaseTimeEntity {

    // problem 이랑 양방향 좋을지도?
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "problem_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "FK__RATING_TO_PROBLEM")
    )
    private Problem problem;

    // user 와 느슨한 결합
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(length = 50)
    private String comment;

    @Column(nullable = false)
    private int rating;

    // TODO : rating 점수 관련해서 app 레벨 제약사항 필요 : [0, 5] 사이의 정수

    public Rating(Problem problem, Long userId, int rating) {
        this.problem = problem;
        this.userId = userId;
        this.rating = rating;
    }
}
