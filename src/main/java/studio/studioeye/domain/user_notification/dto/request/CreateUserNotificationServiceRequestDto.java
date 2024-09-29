package studio.studioeye.domain.user_notification.dto.request;

import studio.studioeye.domain.user_notification.domain.UserNotification;

public record CreateUserNotificationServiceRequestDto(
        Long userId,
        Long notificationId,
        Boolean isRead
) {
        public UserNotification toEntity() {
            return UserNotification.builder()
                    .userId(userId)
                    .notificationId(notificationId)
                    .isRead(isRead)
                    .build();
        }
}
