package studio.studioeye.domain.notification.dto.request;

import studio.studioeye.domain.notification.domain.Notification;

public record CreateNotificationServiceRequestDto(
        Long requestId
) {
    public Notification toEntity() {
        return Notification.builder()
                .requestId(requestId)
                .build();
    }
}