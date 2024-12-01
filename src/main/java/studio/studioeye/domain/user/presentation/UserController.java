package studio.studioeye.domain.user.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import studio.studioeye.domain.user.application.UserService;
import studio.studioeye.domain.user.dto.request.RequestLogin;
import studio.studioeye.domain.user.dto.request.RequestUser;
import studio.studioeye.domain.user.dto.response.EmailVerificationResult;
import studio.studioeye.domain.user.dto.response.JWTAuthResponse;
import studio.studioeye.domain.user.dto.response.UserResponse;
import studio.studioeye.global.exception.BusinessLogicException;
import studio.studioeye.global.exception.error.ExceptionCode;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user-service")
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    ResponseEntity<JWTAuthResponse> login(@RequestBody RequestLogin requestLogin) {
        JWTAuthResponse token = userService.login(requestLogin);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RequestUser requestUser) {
        String response = userService.register(requestUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/response_userById/{userId}")
    public ResponseEntity<UserResponse> findUserResponseByUserId(@PathVariable("userId") Long userId) {
        try {
            UserResponse userResponse = userService.getUserResponseByUserId(userId);
            return ResponseEntity.ok().body(userResponse);
        } catch (BusinessLogicException e) {
            ExceptionCode exceptionCode = e.getExceptionCode();
            HttpStatus status = exceptionCode.getStatus();
            String message = exceptionCode.getMessage();
            return ResponseEntity.status(status).body(new UserResponse(message));
        }
    }

    @GetMapping("/response_userByEmail/{email}")
    public ResponseEntity<UserResponse> findUserResponseByEmail(@PathVariable String email) {
        try {
            UserResponse userResponse = userService.findUserResponseByEmail(email);
            return ResponseEntity.ok().body(userResponse);
        } catch (BusinessLogicException e) {
            ExceptionCode exceptionCode = e.getExceptionCode();
            HttpStatus status = exceptionCode.getStatus();
            String message = exceptionCode.getMessage();
            return ResponseEntity.status(status).body(new UserResponse(message));
        }
    }

    @GetMapping("/users-id/all")
    public ResponseEntity<List<Long>> getAllApprovedUserIds() {
        List<Long> userIds = userService.getAllApprovedUserIds();
        return ResponseEntity.ok(userIds);
    }

    //이메일 인증번호 전송
    @PostMapping("/emails/verification-requests")
    public ResponseEntity<Void> sendMessage(@RequestParam("email") @Valid String email) { // 제네릭 타입 명시
        userService.sendCodeToEmail(email);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //이메일 인증번호 검증
    @GetMapping("/emails/verifications")
    public ResponseEntity<EmailVerificationResult> verificationEmail( // 제네릭 타입 명시
                                                                      @RequestParam("email") @Valid @Email String email,
                                                                      @RequestParam("code") String authCode) {
        EmailVerificationResult response = userService.verifiedCode(email, authCode);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //회원 탈퇴
    @DeleteMapping("/unregister")
    public ResponseEntity unregister(@RequestParam("userId") @Valid Long userId) {
        return userService.unregister(userId);
    }

    //모든 회원 정보 반환
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    //승인 여부 변경
    @PutMapping("/approve")
    public ResponseEntity approveUser(@RequestParam("userId") @Valid Long userId,
                                      @RequestParam("approved") boolean approved) {
        boolean isApproved = userService.updateApproved(userId, approved);
        return ResponseEntity.ok().body(isApproved);
    }
}
