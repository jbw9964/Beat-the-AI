package org.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "received_invitation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReceivedInvitation extends AuditingCreation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "FK__RECEIVED_INVITATION_TO_USER")
    )
    private User user;

    // Problem 과 느슨한 결합
    @Column(nullable = false, updatable = false)
    private Long problemId;

    @Column(length = 50, nullable = false)
    private String problemTitle;

    @Column(length = 30, nullable = false, updatable = false)
    private String code;

    public ReceivedInvitation(User user, Long problemId, String problemTitle, String code) {
        this.user = user;
        this.problemId = problemId;
        this.problemTitle = problemTitle;
        this.code = code;
    }

    // TODO : Problem 받는 생성자 만들기
}
