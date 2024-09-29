package studio.studioeye.domain.notification.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import studio.studioeye.domain.notification.application.NotificationService;
import studio.studioeye.domain.notification.domain.Notification;
import studio.studioeye.global.common.response.ApiResponse;

import java.util.List;

@Tag(name = "알림 API", description = "알림 등록 / 조회")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "알림 등록 (TEST) API")
    @PostMapping("/notification/{requestId}")
    public ApiResponse<Long> createNotification(@PathVariable("requestId") Long requestId) {
        return notificationService.subscribe(requestId);
    }

    @Operation(summary = "모든 알림 조회 API")
    @GetMapping("/notification")
    public ApiResponse<List<Notification>> retrieveAllNotification(){
        return notificationService.retrieveAllNotification();
    }
}
