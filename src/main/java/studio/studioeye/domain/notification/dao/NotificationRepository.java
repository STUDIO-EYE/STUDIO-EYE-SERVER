package studio.studioeye.domain.notification.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import studio.studioeye.domain.notification.domain.Notification;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findById(Long id);
}
