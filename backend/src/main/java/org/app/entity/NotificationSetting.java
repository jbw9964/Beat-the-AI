package org.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "notification_setting")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting {

    @Id
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "FK__NOTIFICATION_SETTING_TO_USER")
    )
    private User user;

    @Embedded
    private EmbeddedSettingInfo settingInfo;

    public NotificationSetting(User user) {
        this.userId = user.getId();
        this.user = user;
        this.settingInfo = new EmbeddedSettingInfo();
    }

    public NotificationSetting(User user, EmbeddedSettingInfo copy) {
        this.userId = user.getId();
        this.user = user;
        this.settingInfo = new EmbeddedSettingInfo(copy);
    }

    public void changeSetting(EmbeddedSettingInfo copy) {
        this.settingInfo = new EmbeddedSettingInfo(copy);
    }
}
