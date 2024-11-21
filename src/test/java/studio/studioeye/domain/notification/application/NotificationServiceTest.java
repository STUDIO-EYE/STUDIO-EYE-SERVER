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
import studio.studioeye.domain.notification.dto.request.CreateNotificationServiceRequestDto;
import studio.studioeye.domain.user.application.UserService;
import studio.studioeye.domain.user_notification.application.UserNotificationService;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;

import java.io.IOException;
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

    @Test
    @DisplayName("알림 생성 실패 테스트 - Emitter Null")
    void createNotificationFail_NullEmitters() {
        // given
        Notification notification = Notification.builder().build();
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification); // Mock 반환 설정
        when(emitterRepository.getAllEmitters()).thenReturn(null); // Null 반환
        // when
        ApiResponse<Notification> response = notificationService.createNotification(1L, notification);
        // then
        assertNotNull(response); // 응답이 null이 아님을 확인
        assertEquals(HttpStatus.OK, response.getStatus()); // 응답 상태 확인
        assertEquals("알림이 존재하지 않습니다.", response.getMessage()); // 예상 메시지 확인
    }
    @Test
    @DisplayName("알림 생성 실패 테스트 - 모든 Emitter 실패")
    void createNotification_AllEmitterFail() throws IOException {
        // given
        Notification notification = Notification.builder().requestId(TEST_REQUEST_ID).build();
        SseEmitter failingEmitter = mock(SseEmitter.class);
        // Mock IOException 발생
        doThrow(new IOException("Emitter failed")).when(failingEmitter).send(any(Notification.class));
        // Mock Repository와 Emitter 설정
        when(notificationRepository.save(any())).thenReturn(notification);
        when(emitterRepository.getAllEmitters()).thenReturn(List.of(failingEmitter));
        // when
        ApiResponse<Notification> response = notificationService.createNotification(TEST_USER_ID, notification);
        // then
        assertNotNull(response); // 응답이 null이 아님을 확인
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus()); // NotificationService의 기존 설정에 맞춘 상태 코드 검증
        assertEquals(ErrorCode.INVALID_SSE_ID.getMessage(), response.getMessage()); // 예상 메시지 검증
        // 호출 검증
        verify(notificationRepository, times(1)).save(notification); // Notification 저장 호출 확인
        verify(userNotificationService, times(1)).createUserNotification(TEST_USER_ID, notification.getId()); // UserNotification 호출 확인
        verify(emitterRepository, times(1)).getAllEmitters(); // 모든 Emitter 가져오기 호출 확인
    }

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

    @Test
    @DisplayName("모든 알림 조회 실패 테스트 - 빈 리스트 반환")
    void retrieveAllNotificationFail_EmptyList() {
        // given
        when(notificationRepository.findAll()).thenReturn(Collections.emptyList()); // 빈 리스트 반환
        // when
        ApiResponse<List<Notification>> response = notificationService.retrieveAllNotification();
        // then
        assertNotNull(response); // 응답이 null이 아님을 확인
        assertEquals(HttpStatus.OK, response.getStatus()); // 응답 상태 확인
        assertEquals("알림이 존재하지 않습니다.", response.getMessage()); // 예상 메시지 확인
        assertNull(response.getData()); // 데이터가 null이어야 함
    }
    @Test
    @DisplayName("알림 구독 성공 테스트")
    void subscribeSuccess() {
        // given
        List<Long> userIds = List.of(1L);
        SseEmitter mockEmitter = mock(SseEmitter.class);
        when(userService.getAllApprovedUserIds()).thenReturn(userIds); // 승인된 유저 목록 반환
        doReturn(mockEmitter).when(notificationService).createEmitter(anyLong()); // Spy로 createEmitter Mock
        // when
        ApiResponse<Long> response = notificationService.subscribe(1L);
        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("알림을 성공적으로 구독하였습니다.", response.getMessage());
        // 호출 검증
        verify(userService, times(1)).getAllApprovedUserIds();
        verify(emitterRepository, times(userIds.size())).save(anyLong(), any(SseEmitter.class));
    }

    @Test
    @DisplayName("구독 실패 테스트 - Emitter 생성 실패")
    void subscribeFail_EmitterError() {
        // given
        List<Long> userIds = List.of(1L);
        // 유저 리스트 Mock 반환
        when(userService.getAllApprovedUserIds()).thenReturn(userIds);
        // createEmitter 메서드에서 예외를 던지도록 설정
        doThrow(new RuntimeException("Emitter creation failed"))
                .when(notificationService).createEmitter(anyLong());
        // when
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> notificationService.subscribe(1L)
        );
        // then
        assertEquals("Emitter creation failed", exception.getMessage());
        // 호출 검증
        verify(userService, times(1)).getAllApprovedUserIds();
        verify(emitterRepository, never()).save(anyLong(), any(SseEmitter.class)); // 저장되지 않아야 함
    }


//    @Test
//    @DisplayName("createEmitter - 성공 테스트")
//    void createEmitterSuccess() {
//        // given
//        Long emitterId = TEST_USER_ID;
//
//        // when
//        SseEmitter emitter = invokePrivateMethod(notificationService, "createEmitter", emitterId);
//
//        // then
//        assertNotNull(emitter);
//        verify(emitterRepository, times(1)).save(emitterId, emitter);
//    }

//    @Test
//    @DisplayName("Emitter onTimeout - 동작 확인")
//    void emitterOnTimeout() {
//        // given
//        SseEmitter emitter = new SseEmitter();
//        Long emitterId = TEST_USER_ID;
//        doNothing().when(emitterRepository).deleteById(emitterId);
//
//        // when
//        emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));
//        emitter.completeWithTimeout();
//
//        // then
//        verify(emitterRepository, times(1)).deleteById(emitterId);
//    }

//    @Test
//    @DisplayName("Emitter onCompletion - 동작 확인")
//    void emitterOnCompletion() {
//        // given
//        SseEmitter emitter = new SseEmitter();
//        Long emitterId = TEST_USER_ID;
//        doNothing().when(emitterRepository).deleteById(emitterId);
//        // when
//        emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
//        emitter.complete();
//        // then
//        verify(emitterRepository, times(1)).deleteById(emitterId);
//        }

    @Test
    @DisplayName("createNotification - 실패 테스트 (savedNotification이 null)")
    void createNotificationFail_SavedNotificationNull() {
        // given
        lenient().when(notificationRepository.save(any(Notification.class))).thenReturn(null); // lenient로 설정
        Notification notification = Notification.builder()
                .requestId(TEST_REQUEST_ID)
                .build();
        Collection<SseEmitter> emitters = List.of(new SseEmitter());
        lenient().when(emitterRepository.getAllEmitters()).thenReturn(emitters); // lenient 설정 추가
        // when
        ApiResponse<Notification> response = notificationService.createNotification(TEST_USER_ID, notification);
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST); // 상태 코드 검증
        assertThat(response.getMessage()).isIn(
                ErrorCode.INVALID_SSE_ID.getMessage(),
                ErrorCode.INVALID_INPUT_VALUE.getMessage()
        );
    }

}
