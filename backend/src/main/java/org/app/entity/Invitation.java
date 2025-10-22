package org.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(
        name = "invitation",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK__INVITATION_CODE",
                        columnNames = "code"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invitation extends AuditingCreation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "problem_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "FK__INVITATION_TO_PROBLEM")
    )
    private Problem problem;

    @Column(length = 30, nullable = false, updatable = false)
    private String code;

    public Invitation(Problem problem, String code) {
        this.problem = problem;
        this.code = code;
    }
}
