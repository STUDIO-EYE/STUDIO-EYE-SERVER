package studio.studioeye.domain.notification.application;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import studio.studioeye.domain.notification.dao.EmitterRepository;
import studio.studioeye.domain.notification.dao.NotificationRepository;
import studio.studioeye.domain.notification.domain.Notification;
import studio.studioeye.domain.notification.dto.request.CreateNotificationServiceRequestDto;
import studio.studioeye.domain.user.service.UserServiceImpl;
import studio.studioeye.domain.user_notification.application.UserNotificationService;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.error.ErrorCode;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmitterRepository emitterRepository;
    private final UserServiceImpl userServiceImpl;
    private final UserNotificationService userNotificationService;

    // 기본 타임아웃 설정
    private static final Long DEFAULT_TIMEOUT = 600L * 1000 * 60;

    public ApiResponse<Long> subscribe(Long requestId) {
        // 모든 유저 가져오기
        List<Long> userIds = userServiceImpl.getAllApprovedUserIds();
        if (userIds.isEmpty()) {
            return ApiResponse.withError(ErrorCode.USER_IS_EMPTY);
        } else {
            Notification notification = new CreateNotificationServiceRequestDto(requestId).toEntity();
            for (Long userId : userIds) {
                SseEmitter emitter = createEmitter(userId);
                emitterRepository.save(userId, emitter);
                createNotification(userId, notification);

                // Emitter가 완료될 때(모든 데이터가 성공적으로 전송되었을 때) Emitter를 삭제한다.
                emitter.onCompletion(() -> emitterRepository.deleteById(userId));
                // Emitter가 타임아웃 되었을 때(지정된 시간동안 어떠한 이벤트도 전송되지 않았을 때) Emitter를 삭제한다.
                emitter.onTimeout(() -> emitterRepository.deleteById(userId));
            }

            return ApiResponse.ok("알림을 성공적으로 구독하였습니다.", requestId);
        }
    }

    public ApiResponse<Notification> createNotification(Long userId, Notification notification) {
        // notification 저장
        Notification savedNotification = notificationRepository.save(notification);

        // user_notification 저장
        userNotificationService.createUserNotification(userId, savedNotification.getId());

        Collection<SseEmitter> emitters = emitterRepository.getAllEmitters();
        for (SseEmitter emitter : emitters) {
            if (emitter != null) {
                try {
                    emitter.send(savedNotification);
                } catch (IOException e) {
                    emitter.completeWithError(e);
                    return ApiResponse.withError(ErrorCode.INVALID_SSE_ID);
                }
            }
        }
        return ApiResponse.ok("알림을 성공적으로 등록하였습니다.", savedNotification);
    }

    public ApiResponse<List<Notification>> retrieveAllNotification() {
        List<Notification> notificationList = notificationRepository.findAll();
        if(notificationList.isEmpty()) {
            return ApiResponse.ok("알림이 존재하지 않습니다.");
        }
        return ApiResponse.ok("모든 알림 목록을 성공적으로 조회했습니다.", notificationList);
    }

    private SseEmitter createEmitter(Long id) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(id, emitter);

        // Emitter가 완료될 때(모든 데이터가 성공적으로 전송되었을 때) Emitter를 삭제한다.
        emitter.onCompletion(() -> emitterRepository.deleteById(id));
        // Emitter가 타임아웃 되었을 때(지정된 시간동안 어떠한 이벤트도 전송되지 않았을 때) Emitter를 삭제한다.
        emitter.onTimeout(() -> emitterRepository.deleteById(id));

        return emitter;
    }

}
