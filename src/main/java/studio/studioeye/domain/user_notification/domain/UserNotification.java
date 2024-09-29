package studio.studioeye.domain.user_notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
@IdClass(UserNotificationPK.class)
public class UserNotification {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(nullable = false)
    private Boolean isRead; // 알림 읽었는지 여부

    @Builder
    public UserNotification(Long userId, Long notificationId, Boolean isRead) {
        this.userId = userId;
        this.notificationId = notificationId;
        this.isRead = isRead;
    }

    public void updateIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
}
