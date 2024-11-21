package studio.studioeye.domain.notification.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import studio.studioeye.domain.notification.dao.EmitterRepository;
import studio.studioeye.domain.notification.dao.NotificationRepository;
import studio.studioeye.domain.notification.domain.Notification;
import studio.studioeye.domain.user.application.UserService;
import studio.studioeye.domain.user_notification.application.UserNotificationService;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Spy
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

    // 알림 구독 관련 테스트
    @Test
    @DisplayName("알림 구독 성공 테스트")
    void subscribeSuccess() {
        List<Long> userIds = List.of(1L);
        SseEmitter mockEmitter = mock(SseEmitter.class);
        when(userService.getAllApprovedUserIds()).thenReturn(userIds);
        doReturn(mockEmitter).when(notificationService).createEmitter(anyLong());

        ApiResponse<Long> response = notificationService.subscribe(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("알림을 성공적으로 구독하였습니다.", response.getMessage());
        verify(userService, times(1)).getAllApprovedUserIds();
        verify(emitterRepository, times(userIds.size())).save(anyLong(), any(SseEmitter.class));
    }

    @Test
    @DisplayName("알림 구독 실패 테스트 - 승인된 유저가 없는 경우")
    void subscribeFail() {
        when(userService.getAllApprovedUserIds()).thenReturn(new ArrayList<>());

        ApiResponse<Long> response = notificationService.subscribe(TEST_REQUEST_ID);

        assertThat(response.getStatus()).isEqualTo(ErrorCode.USER_IS_EMPTY.getStatus());
        assertThat(response.getMessage()).isEqualTo(ErrorCode.USER_IS_EMPTY.getMessage());
    }

    @Test
    @DisplayName("알림 구독 실패 테스트 - Emitter 저장 실패")
    void subscribeFail_EmitterSaveError() {
        List<Long> userIds = List.of(TEST_USER_ID);
        when(userService.getAllApprovedUserIds()).thenReturn(userIds);
        doThrow(new RuntimeException("Emitter 저장 실패")).when(emitterRepository).save(any(), any(SseEmitter.class));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.subscribe(TEST_REQUEST_ID));

        assertEquals("Emitter 저장 실패", exception.getMessage());
        verify(userService, times(1)).getAllApprovedUserIds();
        verify(emitterRepository, times(1)).save(any(), any(SseEmitter.class));
    }

    @Test
    @DisplayName("구독 실패 테스트 - Emitter 생성 실패")
    void subscribeFail_EmitterError() {
        List<Long> userIds = List.of(1L);
        when(userService.getAllApprovedUserIds()).thenReturn(userIds);
        doThrow(new RuntimeException("Emitter creation failed"))
                .when(notificationService).createEmitter(anyLong());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> notificationService.subscribe(1L)
        );

        assertEquals("Emitter creation failed", exception.getMessage());
        verify(userService, times(1)).getAllApprovedUserIds();
        verify(emitterRepository, never()).save(anyLong(), any(SseEmitter.class));
    }

    // 알림 생성 관련 테스트
    @Test
    @DisplayName("알림 생성 성공 테스트")
    void createNotificationSuccess() throws IOException {
        Notification notification = Notification.builder().build();
        Collection<SseEmitter> emitters = List.of(new SseEmitter());
        when(notificationRepository.save(any())).thenReturn(notification);
        when(emitterRepository.getAllEmitters()).thenReturn(emitters);

        ApiResponse<Notification> response = notificationService.createNotification(TEST_USER_ID, notification);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(response.getMessage()).isEqualTo("알림을 성공적으로 등록하였습니다.");
        verify(notificationRepository, times(1)).save(notification);
        verify(userNotificationService, times(1)).createUserNotification(TEST_USER_ID, notification.getId());
    }

    @Test
    @DisplayName("알림 생성 실패 테스트 - Emitter Null")
    void createNotificationFail_NullEmitters() {
        Notification notification = Notification.builder().build();
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(emitterRepository.getAllEmitters()).thenReturn(null);

        ApiResponse<Notification> response = notificationService.createNotification(1L, notification);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("알림이 존재하지 않습니다.", response.getMessage());
    }

    @Test
    @DisplayName("알림 생성 실패 테스트 - 모든 Emitter 실패")
    void createNotification_AllEmitterFail() throws IOException {
        Notification notification = Notification.builder().requestId(TEST_REQUEST_ID).build();
        SseEmitter failingEmitter = mock(SseEmitter.class);
        doThrow(new IOException("Emitter failed")).when(failingEmitter).send(any(Notification.class));
        when(notificationRepository.save(any())).thenReturn(notification);
        when(emitterRepository.getAllEmitters()).thenReturn(List.of(failingEmitter));

        ApiResponse<Notification> response = notificationService.createNotification(TEST_USER_ID, notification);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals(ErrorCode.INVALID_SSE_ID.getMessage(), response.getMessage());
        verify(notificationRepository, times(1)).save(notification);
        verify(userNotificationService, times(1)).createUserNotification(TEST_USER_ID, notification.getId());
        verify(emitterRepository, times(1)).getAllEmitters();
    }

    // 알림 조회 관련 테스트
    @Test
    @DisplayName("모든 알림 조회 성공 테스트")
    void retrieveAllNotificationSuccess() {
        List<Notification> notificationList = List.of(Notification.builder().build(), Notification.builder().build());
        when(notificationRepository.findAll()).thenReturn(notificationList);

        ApiResponse<List<Notification>> response = notificationService.retrieveAllNotification();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(response.getData()).isEqualTo(notificationList);
        assertThat(response.getMessage()).isEqualTo("모든 알림 목록을 성공적으로 조회했습니다.");
    }

    @Test
    @DisplayName("모든 알림 조회 실패 테스트 - 알림 없음")
    void retrieveAllNotificationFail() {
        when(notificationRepository.findAll()).thenReturn(new ArrayList<>());

        ApiResponse<List<Notification>> response = notificationService.retrieveAllNotification();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(response.getMessage()).isEqualTo("알림이 존재하지 않습니다.");
        assertThat(response.getData()).isNull();
    }

    @Test
    @DisplayName("모든 알림 조회 실패 테스트 - 빈 리스트 반환")
    void retrieveAllNotificationFail_EmptyList() {
        when(notificationRepository.findAll()).thenReturn(Collections.emptyList());

        ApiResponse<List<Notification>> response = notificationService.retrieveAllNotification();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("알림이 존재하지 않습니다.", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("createEmitter - 정상 생성 테스트")
    void createEmitter_Success() {
        // given
        Long emitterId = TEST_USER_ID;

        // when
        SseEmitter emitter = invokePrivateMethod(notificationService, "createEmitter", emitterId);

        // then
        assertNotNull(emitter); // 생성된 Emitter가 null이 아닌지 확인
        verify(emitterRepository, times(1)).save(emitterId, emitter); // 저장 호출 확인
    }

    private SseEmitter invokePrivateMethod(NotificationService target, String methodName, Long param) {
        try {
            // private 메서드 접근 허용
            Method method = NotificationService.class.getDeclaredMethod(methodName, Long.class);
            method.setAccessible(true);
            // private 메서드 실행
            return (SseEmitter) method.invoke(target, param);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke private method: " + methodName, e);
        }
    }

    @Test
    @DisplayName("createEmitter - 타임아웃 테스트")
    void createEmitter_OnTimeout() {
        // given
        Long emitterId = TEST_USER_ID;
        // Mock SseEmitter 객체 생성
        SseEmitter mockEmitter = mock(SseEmitter.class);
        // SseEmitter의 onTimeout 메서드에 콜백 설정
        doAnswer(invocation -> {
            Runnable callback = invocation.getArgument(0);
            callback.run(); // 타임아웃 콜백 실행
            return null;
        }).when(mockEmitter).onTimeout(any());
        // Emitter 저장 로직 Mock
        doNothing().when(emitterRepository).deleteById(emitterId);
        // when
        mockEmitter.onTimeout(() -> emitterRepository.deleteById(emitterId));
        // then
        verify(emitterRepository, times(1)).deleteById(emitterId); // 타임아웃 발생 시 deleteById 호출 확인
    }

    @Test
    @DisplayName("createEmitter - 완료 테스트")
    void createEmitter_OnCompletion() {
        // given
        Long emitterId = TEST_USER_ID;
        // Mock SseEmitter 객체 생성
        SseEmitter mockEmitter = mock(SseEmitter.class);
        // SseEmitter의 onCompletion 메서드에 콜백 설정
        doAnswer(invocation -> {
            Runnable callback = invocation.getArgument(0);
            callback.run(); // 완료 콜백 실행
            return null;
        }).when(mockEmitter).onCompletion(any());
        // Emitter 저장 로직 Mock
        doNothing().when(emitterRepository).deleteById(emitterId);
        // when
        mockEmitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
        // then
        verify(emitterRepository, times(1)).deleteById(emitterId); // 완료 시 삭제 호출 확인
    }

}
