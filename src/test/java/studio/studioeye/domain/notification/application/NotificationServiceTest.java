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

    @Test
    @DisplayName("알림 생성 성공 테스트")
    void createNotificationSuccess() {
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

    @Test
    @DisplayName("createEmitter - 예외 발생 테스트")
    void createEmitter_ExceptionThrown() {
        // given
        Long emitterId = TEST_USER_ID;
        // Mock 저장 시 예외 발생 설정
        doThrow(new RuntimeException("Emitter creation error"))
                .when(emitterRepository).save(eq(emitterId), any(SseEmitter.class));
        // when
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> {
                    // Mock으로 Emitter 생성 및 저장
                    SseEmitter mockEmitter = mock(SseEmitter.class);
                    emitterRepository.save(emitterId, mockEmitter);
                }
        );
        // then
        assertEquals("Emitter creation error", exception.getMessage()); // 예외 메시지 확인
        verify(emitterRepository, times(1)).save(eq(emitterId), any(SseEmitter.class)); // 호출 확인
    }

    @Test
    @DisplayName("createEmitter - 람다 onTimeout 실행 테스트")
    void createEmitter_OnTimeout_WithMock() {
        // given
        Long emitterId = TEST_USER_ID;
        // Mock SseEmitter 생성
        SseEmitter mockEmitter = mock(SseEmitter.class);
        doNothing().when(emitterRepository).deleteById(emitterId);
        // when
        // emitter.onTimeout()을 설정하여 목업이 deleteById 호출을 트리거하도록 설정
        doAnswer(invocation -> {
            Runnable callback = invocation.getArgument(0); // onTimeout 콜백
            callback.run(); // 콜백 실행
            return null;
        }).when(mockEmitter).onTimeout(any(Runnable.class));
        mockEmitter.onTimeout(() -> emitterRepository.deleteById(emitterId)); // 타임아웃 설정
        // then
        verify(emitterRepository, times(1)).deleteById(emitterId); // deleteById 호출 확인
    }

    @Test
    @DisplayName("createEmitter - 람다 onCompletion 실행 테스트")
    void createEmitter_OnCompletion_WithMock() {
        // given
        Long emitterId = TEST_USER_ID;
        // Mock SseEmitter 생성
        SseEmitter mockEmitter = mock(SseEmitter.class);
        doNothing().when(emitterRepository).deleteById(emitterId);
        // when
        // emitter.onCompletion()을 설정하여 목업이 deleteById 호출을 트리거하도록 설정
        doAnswer(invocation -> {
            Runnable callback = invocation.getArgument(0); // onCompletion 콜백
            callback.run(); // 콜백 실행
            return null;
        }).when(mockEmitter).onCompletion(any(Runnable.class));
        mockEmitter.onCompletion(() -> emitterRepository.deleteById(emitterId)); // 완료 설정
        // then
        verify(emitterRepository, times(1)).deleteById(emitterId); // deleteById 호출 확인
    }

    @Test
    @DisplayName("subscribe - 타임아웃 발생 테스트")
    void subscribe_Timeout_WithMock() {
        // given
        List<Long> userIds = List.of(TEST_USER_ID);
        SseEmitter mockEmitter = mock(SseEmitter.class);
        // Mock 반환 설정
        when(userService.getAllApprovedUserIds()).thenReturn(userIds);
        doReturn(mockEmitter).when(notificationService).createEmitter(anyLong());
        // onTimeout 콜백 실행 설정
        doAnswer(invocation -> {
            Runnable callback = invocation.getArgument(0);
            callback.run(); // 타임아웃 콜백 실행
            return null;
        }).when(mockEmitter).onTimeout(any(Runnable.class));
        // deleteById 호출을 목업 처리
        doNothing().when(emitterRepository).deleteById(TEST_USER_ID);
        // when
        notificationService.subscribe(TEST_REQUEST_ID);
        // then
        verify(emitterRepository, times(1)).deleteById(TEST_USER_ID); // 타임아웃 발생 시 deleteById 호출 확인
    }

    @Test
    @DisplayName("createNotification - emitters가 null인 경우 테스트")
    void createNotification_NullEmitters_WithMock() {
        // given
        Notification notification = Notification.builder().build();
        // Mock 설정
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(emitterRepository.getAllEmitters()).thenReturn(null); // emitters가 null 반환
        // when
        ApiResponse<Notification> response = notificationService.createNotification(TEST_USER_ID, notification);
        // then
        assertNotNull(response); // 응답이 null이 아님을 확인
        assertEquals(HttpStatus.OK, response.getStatus()); // 상태 코드 확인
        assertEquals("알림이 존재하지 않습니다.", response.getMessage()); // 예상 메시지 확인
    }

    @Test
    @DisplayName("createNotification - send 호출 시 예외 처리 테스트")
    void createNotification_SendException_WithMock() throws IOException {
        // given
        Notification notification = Notification.builder().build();
        SseEmitter failingEmitter = mock(SseEmitter.class);
        // Mock 설정: 정확한 인자 전달
        doThrow(new IOException("Send failed"))
                .when(failingEmitter)
                .send(any(Notification.class)); // 정확한 인자를 명시
        when(notificationRepository.save(any())).thenReturn(notification);
        when(emitterRepository.getAllEmitters()).thenReturn(List.of(failingEmitter));
        // when
        ApiResponse<Notification> response = notificationService.createNotification(TEST_USER_ID, notification);
        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus()); // 상태 코드 확인
        assertEquals(ErrorCode.INVALID_SSE_ID.getMessage(), response.getMessage()); // 예상 메시지 확인
        verify(failingEmitter, times(1)).completeWithError(any()); // completeWithError 호출 확인
    }
}
