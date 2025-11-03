package org.app.entity;

import jakarta.persistence.*;
import java.time.*;
import lombok.*;

@Getter
@Entity
@Table(name = "scenario_record")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScenarioRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이건 양방향 연관이 좋을지도?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "record_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "FK__SCENARIO_RECORD_TO_PLAY_RECORD")
    )
    private PlayRecord record;

    @Column(nullable = false, updatable = false)
    private int scenarioOrder;

    @Column(length = 500, nullable = false, updatable = false)
    private String scenarioContent;

    @Column(length = 500)
    private String userSubmissionContent;

    @Column(length = 1000)
    private String aiGeneratedContent;

    @Column(nullable = false)
    private boolean hasSubmitted;

    @Column(nullable = false)
    private boolean hasPassed;

    private LocalDateTime submittedAt;

    public ScenarioRecord(PlayRecord record, int scenarioOrder, String scenarioContent) {
        this.record = record;
        this.scenarioOrder = scenarioOrder;
        this.scenarioContent = scenarioContent;
        this.hasSubmitted = this.hasPassed = false;
    }

    public void updateSubmission(
            String userSubmissionContent, String aiGeneratedContent,
            boolean hasPassed, LocalDateTime submittedAt
    ) {
        this.userSubmissionContent = userSubmissionContent;
        this.aiGeneratedContent = aiGeneratedContent;
        this.hasSubmitted = true;
        this.hasPassed = hasPassed;
        this.submittedAt = submittedAt;
    }
}
