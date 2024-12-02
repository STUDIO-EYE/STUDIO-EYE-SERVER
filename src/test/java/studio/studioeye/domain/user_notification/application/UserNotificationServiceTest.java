package studio.studioeye.domain.user_notification.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import studio.studioeye.domain.notification.dao.NotificationRepository;
import studio.studioeye.domain.notification.domain.Notification;
import studio.studioeye.domain.user_notification.dao.UserNotificationRepository;
import studio.studioeye.domain.user_notification.domain.UserNotification;
import studio.studioeye.domain.user_notification.domain.UserNotificationPK;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserNotificationServiceTest {

    @InjectMocks
    private UserNotificationService userNotificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserNotificationRepository userNotificationRepository;

    @Test
    void createUserNotification_Success() {
        Long userId = 1L;
        Long notificationId = 1L;
        UserNotification userNotification = new UserNotification(userId, notificationId, false);

        when(userNotificationRepository.save(any(UserNotification.class))).thenReturn(userNotification);

        ApiResponse<UserNotification> response = userNotificationService.createUserNotification(userId, notificationId);

        assertEquals("USER_NOTIFICATION 정보를 저장하였습니다.", response.getMessage());
        assertEquals(userNotification, response.getData());
    }

    @Test
    void retrieveAllUserNotification_Success() {
        Long userId = 1L;
        UserNotification userNotification = new UserNotification(userId, 1L, false);
        Notification notification = new Notification(1L);

        when(userNotificationRepository.findByUserId(userId)).thenReturn(Collections.singletonList(userNotification));
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        ApiResponse<List<Map<String, Object>>> response = userNotificationService.retrieveAllUserNotification(userId);

        assertEquals("유저의 알림 목록을 성공적으로 조회했습니다.", response.getMessage());
        assertNotNull(response.getData());
        assertFalse(response.getData().isEmpty());
    }

    @Test
    void retrieveAllUserNotification_EmptyList() {
        Long userId = 1L;
        when(userNotificationRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        ApiResponse<List<Map<String, Object>>> response = userNotificationService.retrieveAllUserNotification(userId);

        assertEquals("유저의 알림이 존재하지 않습니다.", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void checkNotification_Success() {
        Long userId = 1L;
        Long notificationId = 1L;
        UserNotification userNotification = new UserNotification(userId, notificationId, false);
        UserNotificationPK userNotificationPK = new UserNotificationPK(userId, notificationId);

        when(userNotificationRepository.findById(userNotificationPK)).thenReturn(Optional.of(userNotification));
        when(userNotificationRepository.save(userNotification)).thenReturn(userNotification);

        ApiResponse<UserNotification> response = userNotificationService.checkNotification(userId, notificationId);

        assertEquals("알림을 성공적으로 읽음 처리했습니다.", response.getMessage());
        assertTrue(response.getData().getIsRead());
    }

    @Test
    void checkNotification_Fail_InvalidId() {
        Long userId = 1L;
        Long notificationId = 1L;
        UserNotificationPK userNotificationPK = new UserNotificationPK(userId, notificationId);

        when(userNotificationRepository.findById(userNotificationPK)).thenReturn(Optional.empty());

        ApiResponse<UserNotification> response = userNotificationService.checkNotification(userId, notificationId);

        assertEquals(ErrorCode.INVALID_USER_NOTIFICATION_ID.getStatus(), response.getStatus());
    }

    @Test
    void deleteUserNotification_Success() {
        Long userId = 1L;
        Long notificationId = 1L;
        UserNotification userNotification = new UserNotification(userId, notificationId, false);
        UserNotificationPK userNotificationPK = new UserNotificationPK(userId, notificationId);

        when(userNotificationRepository.findById(userNotificationPK)).thenReturn(Optional.of(userNotification));

        ApiResponse<Optional<UserNotification>> response = userNotificationService.deleteUserNotification(userId, notificationId);

        verify(userNotificationRepository, times(1)).delete(userNotification);
        assertEquals("알림을 성공적으로 삭제하였습니다.", response.getMessage());
    }

    @Test
    void deleteUserNotification_Fail_InvalidId() {
        Long userId = 1L;
        Long notificationId = 1L;
        UserNotificationPK userNotificationPK = new UserNotificationPK(userId, notificationId);

        when(userNotificationRepository.findById(userNotificationPK)).thenReturn(Optional.empty());

        ApiResponse<Optional<UserNotification>> response = userNotificationService.deleteUserNotification(userId, notificationId);

        assertEquals(ErrorCode.INVALID_USER_NOTIFICATION_ID.getStatus(), response.getStatus());
    }
}
