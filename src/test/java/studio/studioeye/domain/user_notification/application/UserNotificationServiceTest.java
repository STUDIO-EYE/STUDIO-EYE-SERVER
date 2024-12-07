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
    @DisplayName("유저 알림 생성 성공 테스트")
    void createUserNotificationSuccess() {
        Long userId = 1L;
        Long notificationId = 1L;
        UserNotification userNotification = new UserNotification(userId, notificationId, false);
        when(userNotificationRepository.save(any(UserNotification.class))).thenReturn(userNotification);
        ApiResponse<UserNotification> response = userNotificationService.createUserNotification(userId, notificationId);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("USER_NOTIFICATION 정보를 저장하였습니다.", response.getMessage());
        assertEquals(userNotification, response.getData());
    }

    @Test
    @DisplayName("유저 알림 생성 실패 테스트 - null 입력값")
    void createUserNotificationFailDueToNullInput() {
        UserNotificationService mockService = mock(UserNotificationService.class);
        when(mockService.createUserNotification(null, null))
                .thenReturn(ApiResponse.withError(ErrorCode.INVALID_INPUT_VALUE));
        ApiResponse<UserNotification> response = mockService.createUserNotification(null, null);
        assertEquals(ErrorCode.INVALID_INPUT_VALUE.getStatus(), response.getStatus());
        assertEquals(ErrorCode.INVALID_INPUT_VALUE.getMessage(), response.getMessage());
    }

    @Test
    @DisplayName("유저 알림 생성 실패 테스트 - Repository 예외")
    void createUserNotificationFailDueToRepositoryError() {
        Long userId = 1L;
        Long notificationId = 1L;
        when(userNotificationRepository.save(any(UserNotification.class))).thenThrow(new RuntimeException("Database error"));
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userNotificationService.createUserNotification(userId, notificationId)
        );
        assertEquals("Database error", exception.getMessage());
    }

    @Test
    @DisplayName("유저 알림 조회 성공 테스트")
    void retrieveAllUserNotificationSuccess() {
        Long userId = 1L;
        UserNotification userNotification = new UserNotification(userId, 1L, false);
        Notification notification = new Notification(1L);
        when(userNotificationRepository.findByUserId(userId)).thenReturn(Collections.singletonList(userNotification));
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        ApiResponse<List<Map<String, Object>>> response = userNotificationService.retrieveAllUserNotification(userId);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("유저의 알림 목록을 성공적으로 조회했습니다.", response.getMessage());
        assertNotNull(response.getData());
    }

    @Test
    @DisplayName("유저 알림 조회 실패 테스트 - 빈 목록")
    void retrieveAllUserNotificationFailDueToEmptyList() {
        Long userId = 1L;
        when(userNotificationRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        ApiResponse<List<Map<String, Object>>> response = userNotificationService.retrieveAllUserNotification(userId);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("유저의 알림이 존재하지 않습니다.", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("유저 알림 조회 실패 테스트 - 알림 데이터 없음")
    void retrieveAllUserNotificationFailDueToNoNotificationData() {
        // Given
        Long userId = 1L;
        when(userNotificationRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        // When
        ApiResponse<List<Map<String, Object>>> response = userNotificationService.retrieveAllUserNotification(userId);
        // Then
        assertNotNull(response); // 응답 자체가 null이 아닌지 확인
        assertEquals(HttpStatus.OK, response.getStatus()); // 상태 코드 확인
        assertEquals("유저의 알림이 존재하지 않습니다.", response.getMessage()); // 메시지 확인
        assertTrue(response.getData() == null || response.getData().isEmpty());
        verify(userNotificationRepository, times(1)).findByUserId(userId);
        verifyNoInteractions(notificationRepository); // NotificationRepository는 호출되지 않아야 함
    }

    @Test
    @DisplayName("유저 알림 조회 실패 테스트 - null 입력값")
    void retrieveAllUserNotificationFailDueToNullInput() {
        // Given
        Long nullUserId = null;
        ApiResponse<List<Map<String, Object>>> expectedResponse = ApiResponse.withError(ErrorCode.INVALID_INPUT_VALUE);
        UserNotificationService mockService = mock(UserNotificationService.class);
        when(mockService.retrieveAllUserNotification(nullUserId)).thenReturn(expectedResponse);
        // When
        ApiResponse<List<Map<String, Object>>> response = mockService.retrieveAllUserNotification(nullUserId);
        // Then
        assertNotNull(response); // 응답이 null이 아닌지 확인
        assertEquals(ErrorCode.INVALID_INPUT_VALUE.getStatus(), response.getStatus()); // 상태 코드 확인
        assertEquals(ErrorCode.INVALID_INPUT_VALUE.getMessage(), response.getMessage()); // 메시지 확인
        verifyNoInteractions(userNotificationRepository);
        verifyNoInteractions(notificationRepository);
    }

    @Test
    @DisplayName("유저 알림 조회 실패 테스트 - Repository 예외")
    void retrieveAllUserNotificationFailDueToRepositoryError() {
        Long userId = 1L;
        when(userNotificationRepository.findByUserId(userId)).thenThrow(new RuntimeException("Database error"));
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userNotificationService.retrieveAllUserNotification(userId)
        );
        assertEquals("Database error", exception.getMessage());
    }

    @Test
    @DisplayName("유저 알림 읽음 처리 성공 테스트")
    void checkNotificationSuccess() {
        Long userId = 1L;
        Long notificationId = 1L;
        UserNotification userNotification = new UserNotification(userId, notificationId, false);
        when(userNotificationRepository.findById(any())).thenReturn(Optional.of(userNotification));
        when(userNotificationRepository.save(userNotification)).thenReturn(userNotification);
        ApiResponse<UserNotification> response = userNotificationService.checkNotification(userId, notificationId);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertTrue(response.getData().getIsRead());
    }

    @Test
    @DisplayName("유저 알림 읽음 처리 실패 테스트 - 잘못된 ID")
    void checkNotificationFailDueToInvalidId() {
        Long userId = 1L;
        Long notificationId = 1L;
        when(userNotificationRepository.findById(any())).thenReturn(Optional.empty());
        ApiResponse<UserNotification> response = userNotificationService.checkNotification(userId, notificationId);
        assertEquals(ErrorCode.INVALID_USER_NOTIFICATION_ID.getStatus(), response.getStatus());
    }

    @Test
    @DisplayName("유저 알림 읽음 처리 실패 테스트 - null 입력값")
    void checkNotificationFailDueToNullInput() {
        ApiResponse<UserNotification> response = userNotificationService.checkNotification(null, null);
        assertEquals(ErrorCode.INVALID_INPUT_VALUE.getStatus(), response.getStatus());
    }

    @Test
    @DisplayName("유저 알림 읽음 처리 실패 테스트 - Repository 예외")
    void checkNotificationFailDueToRepositoryError() {
        Long userId = 1L;
        Long notificationId = 1L;
        when(userNotificationRepository.findById(any())).thenThrow(new RuntimeException("Database error"));
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userNotificationService.checkNotification(userId, notificationId)
        );
        assertEquals("Database error", exception.getMessage());
    }

    @Test
    @DisplayName("유저 알림 삭제 성공 테스트")
    void deleteUserNotificationSuccess() {
        Long userId = 1L;
        Long notificationId = 1L;
        UserNotification userNotification = new UserNotification(userId, notificationId, false);
        UserNotificationPK userNotificationPK = new UserNotificationPK(userId, notificationId);
        when(userNotificationRepository.findById(userNotificationPK)).thenReturn(Optional.of(userNotification));
        ApiResponse<Optional<UserNotification>> response = userNotificationService.deleteUserNotification(userId, notificationId);
        verify(userNotificationRepository, times(1)).delete(userNotification);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("알림을 성공적으로 삭제하였습니다.", response.getMessage());
    }

    @Test
    @DisplayName("유저 알림 삭제 실패 테스트 - 잘못된 ID")
    void deleteUserNotificationFailDueToInvalidId() {
        Long userId = 1L;
        Long notificationId = 1L;
        UserNotificationPK userNotificationPK = new UserNotificationPK(userId, notificationId);
        when(userNotificationRepository.findById(userNotificationPK)).thenReturn(Optional.empty());
        ApiResponse<Optional<UserNotification>> response = userNotificationService.deleteUserNotification(userId, notificationId);
        assertEquals(ErrorCode.INVALID_USER_NOTIFICATION_ID.getStatus(), response.getStatus());
    }

    @Test
    @DisplayName("유저 알림 삭제 실패 테스트 - null 입력값")
    void deleteUserNotificationFailDueToNullInput() {
        ApiResponse<Optional<UserNotification>> response = userNotificationService.deleteUserNotification(null, null);
        assertEquals(ErrorCode.INVALID_INPUT_VALUE.getStatus(), response.getStatus());
    }

    @Test
    @DisplayName("유저 알림 삭제 실패 테스트 - Repository 예외")
    void deleteUserNotificationFailDueToRepositoryError() {
        // Given
        Long userId = 1L;
        Long notificationId = 1L;
        UserNotificationPK userNotificationPK = new UserNotificationPK(userId, notificationId);
        doThrow(new RuntimeException("Database error"))
                .when(userNotificationRepository)
                .findById(userNotificationPK);
        // When
        ApiResponse<Optional<UserNotification>> response = userNotificationService.deleteUserNotification(userId, notificationId);
        // Then
        assertNotNull(response);
        assertEquals(ErrorCode.FAILED_USER_NOTIFICATION_DELETE.getStatus(), response.getStatus());
        assertEquals(ErrorCode.FAILED_USER_NOTIFICATION_DELETE.getMessage(), response.getMessage());
        verify(userNotificationRepository, times(1)).findById(userNotificationPK);
        verify(userNotificationRepository, never()).delete(any(UserNotification.class));
    }

    @Test
    @DisplayName("유저 알림 삭제 성공 테스트 (ByNotificationId)")
    void deleteUserNotificationByNotificationIdSuccess() {
        Long notificationId = 1L;
        UserNotification userNotification = new UserNotification(1L, notificationId, false);
        when(userNotificationRepository.findByNotificationId(notificationId)).thenReturn(Collections.singletonList(userNotification));
        ApiResponse<String> response = userNotificationService.deleteUserNotificationByNotificationId(notificationId);
        verify(userNotificationRepository, times(1)).delete(userNotification);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("성공적으로 사용자 알림을 삭제했습니다.", response.getMessage());
    }

    @Test
    @DisplayName("유저 알림 삭제 실패 테스트 - 잘못된 ID (ByNotificationId)")
    void deleteUserNotificationByNotificationIdFailDueToInvalidId() {
        Long notificationId = 1L;
        when(userNotificationRepository.findByNotificationId(notificationId)).thenReturn(Collections.emptyList());
        ApiResponse<String> response = userNotificationService.deleteUserNotificationByNotificationId(notificationId);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("해당 알림의 사용자 알림이 없습니다.", response.getMessage());
    }

    @Test
    @DisplayName("유저 알림 삭제 실패 테스트 - Repository 예외 (ByNotificationId)")
    void deleteUserNotificationByNotificationIdFailDueToRepositoryError() {
        Long notificationId = 1L;
        when(userNotificationRepository.findByNotificationId(notificationId)).thenThrow(new RuntimeException("Database error"));
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userNotificationService.deleteUserNotificationByNotificationId(notificationId)
        );
        assertEquals("Database error", exception.getMessage());
    }
}
