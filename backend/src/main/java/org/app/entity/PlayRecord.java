package org.app.entity;

import jakarta.persistence.*;
import java.util.*;
import lombok.*;

@Getter
@Entity
@Table(name = "play_record")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "FK__PLAY_RECORD_TO_USER")
    )
    private User user;

    // Problem 과 느슨한 결합
    @Column(nullable = false, updatable = false)
    private Long problemId;

    @Column(length = 50, nullable = false, updatable = false)
    private String title;

    @Column(length = 255, updatable = false)
    private String description;

    @Column(length = 50, updatable = false)
    private String rewardMessage;

    @Column(nullable = false, updatable = false)
    private int numOfScenariosToGetReward;

    @Column(nullable = false, updatable = false)
    private int numOfScenariosToFailPlay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayRecordStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayRecordVisibility visibility;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "record")
    private final List<ScenarioRecord> scenarioRecords = new ArrayList<>();

    public PlayRecord(
            User user, Long problemId, String title, String description,
            String rewardMessage, int numOfScenariosToGetReward, int numOfScenariosToFailPlay,
            PlayRecordStatus status, PlayRecordVisibility visibility
    ) {
        this.user = user;
        this.problemId = problemId;
        this.title = title;
        this.description = description;
        this.rewardMessage = rewardMessage;
        this.numOfScenariosToGetReward = numOfScenariosToGetReward;
        this.numOfScenariosToFailPlay = numOfScenariosToFailPlay;
        this.status = status;
        this.visibility = visibility;
    }

    // TODO : Problem 포함하는 생성자 넣기
}
