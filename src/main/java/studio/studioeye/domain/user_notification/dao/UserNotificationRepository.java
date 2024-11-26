package studio.studioeye.domain.user_notification.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import studio.studioeye.domain.user_notification.domain.UserNotification;
import studio.studioeye.domain.user_notification.domain.UserNotificationPK;

import java.util.List;
@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, UserNotificationPK> {
    List<UserNotification> findByUserId(Long userId);

    List<UserNotification> findByNotificationId(Long notificationId);
}

