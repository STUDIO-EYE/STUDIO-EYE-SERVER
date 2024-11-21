package studio.studioeye.domain.notification.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import studio.studioeye.domain.notification.dao.EmitterRepository;
import studio.studioeye.domain.notification.dao.NotificationRepository;
import studio.studioeye.domain.notification.domain.Notification;
import studio.studioeye.domain.notification.dto.request.CreateNotificationServiceRequestDto;
import studio.studioeye.domain.user.application.UserService;
import studio.studioeye.domain.user_notification.application.UserNotificationService;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private EmitterRepository emitterRepository;
    @Mock
    private UserService userService;
    @Mock
    private UserNotificationService userNotificationService;

    private static final Long TEST_REQUEST_ID = 1L;
    private static final Long TEST_USER_ID = 1L;

//    @Test
//    @DisplayName("알림 구독 성공 테스트")
//    void subscribeSuccess() {
//        // given
//        CreateNotificationServiceRequestDto createNotificationServiceRequestDto = new CreateNotificationServiceRequestDto(1L);
//        List<Long> userIds = List.of(TEST_USER_ID);
//        when(userService.getAllApprovedUserIds()).thenReturn(userIds);
//        when(emitterRepository.save(any(), any())).thenReturn(null);
////        when(userNotificationService.createUserNotification(any(), any())).thenReturn();
//
//        // when
//        ApiResponse<Long> response = notificationService.subscribe(TEST_REQUEST_ID);
//
//        // then
//        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
//        assertThat(response.getMessage()).isEqualTo("알림을 성공적으로 구독하였습니다.");
//        verify(emitterRepository, times(userIds.size())).save(any(), any(SseEmitter.class));
//    }

    @Test
    @DisplayName("알림 구독 실패 테스트 - 승인된 유저가 없는 경우")
    void subscribeFail() {
        // given
        when(userService.getAllApprovedUserIds()).thenReturn(new ArrayList<>());
        // when
        ApiResponse<Long> response = notificationService.subscribe(TEST_REQUEST_ID);
        // then
        assertThat(response.getStatus()).isEqualTo(ErrorCode.USER_IS_EMPTY.getStatus());
        assertThat(response.getMessage()).isEqualTo(ErrorCode.USER_IS_EMPTY.getMessage());
    }

    @Test
    @DisplayName("알림 구독 실패 테스트 - Emitter 저장 실패")
    void subscribeFail_EmitterSaveError() {
        // given
        List<Long> userIds = List.of(TEST_USER_ID);
        when(userService.getAllApprovedUserIds()).thenReturn(userIds);
        doThrow(new RuntimeException("Emitter 저장 실패")).when(emitterRepository).save(any(), any(SseEmitter.class));
        // when
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.subscribe(TEST_REQUEST_ID)
        );
        // then
        assertEquals("Emitter 저장 실패", exception.getMessage());
        verify(userService, times(1)).getAllApprovedUserIds();
        verify(emitterRepository, times(1)).save(any(), any(SseEmitter.class));
    }

    @Test
    @DisplayName("알림 생성 성공 테스트")
    void createNotificationSuccess() throws IOException {
        // given
        Notification notification = Notification.builder().build();
        Collection<SseEmitter> emitters = List.of(new SseEmitter());
        when(notificationRepository.save(any())).thenReturn(notification);
        when(emitterRepository.getAllEmitters()).thenReturn(emitters);
        // when
        ApiResponse<Notification> response = notificationService.createNotification(TEST_USER_ID, notification);
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(response.getMessage()).isEqualTo("알림을 성공적으로 등록하였습니다.");
        verify(notificationRepository, times(1)).save(notification);
        verify(userNotificationService, times(1)).createUserNotification(TEST_USER_ID, notification.getId());
    }

//    @Test
//    @DisplayName("알림 생성 실패 테스트")
//    void createNotificationFail() throws IOException {
//        // given
//        Notification notification = Notification.builder().build();
//        SseEmitter emitter = mock(SseEmitter.class);
//        doThrow(new IOException()).when(emitter).send(Optional.ofNullable(any()));
//        when(emitterRepository.getAllEmitters()).thenReturn(List.of(emitter));
//        // when
//        ApiResponse<Notification> response = notificationService.createNotification(TEST_USER_ID, notification);
//        // then
//        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
//        assertThat(response.getMessage()).isEqualTo(ErrorCode.INVALID_SSE_ID.getMessage());
//    }

    @Test
    @DisplayName("모든 알림 조회 성공 테스트")
    void retrieveAllNotificationSuccess() {
        // given
        List<Notification> notificationList = List.of(Notification.builder().build(), Notification.builder().build());
        when(notificationRepository.findAll()).thenReturn(notificationList);
        // when
        ApiResponse<List<Notification>> response = notificationService.retrieveAllNotification();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(response.getData()).isEqualTo(notificationList);
        assertThat(response.getMessage()).isEqualTo("모든 알림 목록을 성공적으로 조회했습니다.");
    }

    @Test
    @DisplayName("모든 알림 조회 실패 테스트 - 알림 없음")
    void retrieveAllNotificationFail() {
        // given
        when(notificationRepository.findAll()).thenReturn(new ArrayList<>());
        // when
        ApiResponse<List<Notification>> response = notificationService.retrieveAllNotification();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(response.getMessage()).isEqualTo("알림이 존재하지 않습니다.");
        assertThat(response.getData()).isNull();
    }
}
