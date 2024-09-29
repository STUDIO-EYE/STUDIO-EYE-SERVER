package studio.studioeye.domain.user_notification.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import studio.studioeye.domain.user_notification.application.UserNotificationService;
import studio.studioeye.domain.user_notification.domain.UserNotification;
import studio.studioeye.global.common.response.ApiResponse;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag(name = "특정 유저 알림 API", description = "유저 알림 조회")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserNotificationController {

    private final UserNotificationService userNotificationService;

    @Operation(summary = "특정 유저에게 알림 등록 (TEST) API")
    @PostMapping("/userNotification/{userId}/{notificationId}")
    public ApiResponse<UserNotification> createUserNotificationTEST(@RequestParam Long userId, @PathVariable Long notificationId) {
        return userNotificationService.createUserNotification(userId, notificationId);
    }

    @Operation(summary = "특정 유저의 알림 전체 조회 API")
    @GetMapping("/userNotification/{userId}")
    public ApiResponse<List<Map<String, Object>>> retrieveAllNotification(@PathVariable Long userId) {
        return userNotificationService.retrieveAllUserNotification(userId);
    }

    @Operation(summary = "알림 수정(읽음 처리) API")
    @PutMapping("/userNotification/{userId}/{notificationId}")
    public ApiResponse<UserNotification> checkNotification(@RequestParam Long userId, @PathVariable Long notificationId) {
        return userNotificationService.checkNotification(userId, notificationId);
    }

    @Operation(summary = "알림 삭제 API")
    @DeleteMapping("/userNotification/{userId}/{notificationId}")
    public ApiResponse<Optional<UserNotification>> deleteNotification(@RequestParam Long userId, @PathVariable Long notificationId) {
        return userNotificationService.deleteUserNotification(userId, notificationId);
    }
}