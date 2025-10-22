package org.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends AuditingCreation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "FK__NOTIFICATION_TO_USER")
    )
    private User user;

    @Column(length = 50, nullable = false)
    private String overview;

    @Column(length = 255, nullable = false)
    private String description;

    public Notification(User user, String overview, String description) {
        this.user = user;
        this.overview = overview;
        this.description = description;
    }

}
