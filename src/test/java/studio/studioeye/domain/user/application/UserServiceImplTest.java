package studio.studioeye.domain.user.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import studio.studioeye.domain.user.dao.UserRepository;
import studio.studioeye.domain.user.domain.User;
import studio.studioeye.domain.user.dto.request.RequestLogin;
import studio.studioeye.domain.user.dto.request.RequestUser;
import studio.studioeye.domain.user.dto.response.EmailVerificationResult;
import studio.studioeye.domain.user.dto.response.JWTAuthResponse;
import studio.studioeye.domain.user.dto.response.UserResponse;
import studio.studioeye.global.exception.BusinessLogicException;
import studio.studioeye.global.exception.error.ExceptionCode;
import studio.studioeye.global.security.UserDetailsServiceImpl;
import studio.studioeye.global.security.jwt.JwtTokenProvider;
import studio.studioeye.infrastructure.mail.MailService;
import studio.studioeye.infrastructure.redis.RedisService;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @InjectMocks
    private UserServiceImpl userServiceImpl;
    @Mock
    private UserDetailsServiceImpl userDetailsServiceImpl;
    @Mock
    private RedisService redisService;
    @Mock
    private MailService mailService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthenticationManager authenticationManager;
    private RequestLogin requestLogin;
    private RequestUser requestUser;
    private UserResponse userResponse;

    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccess() {
        // given
        Authentication authentication = mock(Authentication.class);
        String email = "email@google.com";
        String password = "password";
        String name = "홍길동";
        String phoneNumber = "010-1234-5678";
        User user = new User(1L, email, name, phoneNumber, password, true);
        user.setId(1L);
        // 예제 로그인 요청과 User 객체 설정
        requestLogin = new RequestLogin();
        requestLogin.setEmail(email);
        requestLogin.setPwd(password);
        // stub
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userDetailsServiceImpl.findUserIdByEmail(email)).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(jwtTokenProvider.createAccessToken(user.getId())).thenReturn(accessToken);
        when(jwtTokenProvider.createRefreshToken()).thenReturn(refreshToken);
        // when
        JWTAuthResponse response = userServiceImpl.login(requestLogin);
        // then
        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        assertTrue(response.isApproved());
    }

    @Test
    @DisplayName("로그인 실패 테스트")
    void loginFail() {
        // given
        Authentication authentication = mock(Authentication.class);
        String email = "email@google.com";
        String password = "password";
        requestLogin = new RequestLogin();
        requestLogin.setEmail(email);
        requestLogin.setPwd(password);
        //stub
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userDetailsServiceImpl.findUserIdByEmail(email)).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        // when & then
        assertThrows(Exception.class, () -> userServiceImpl.login(requestLogin));
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    void registerSuccess() {
        // given
        String email = "email@google.com";
        String password = "password";
        String name = "홍길동";
        String phoneNumber = "010-1234-5678";
        requestUser = new RequestUser(email, password, name, phoneNumber);
        // stub
        when(userRepository.existsByEmail(requestUser.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(requestUser.getPhoneNumber())).thenReturn(false);
        when(passwordEncoder.encode(requestUser.getPwd())).thenReturn("encryptedPwd");
        // when
        String result = userServiceImpl.register(requestUser);
        // then
        assertEquals("User registered successfully!", result);
    }

    @Test
    @DisplayName("회원가입 실패 테스트 - 중복된 이메일")
    void registerFail_DuplicateEmail() {
        // given
        String email = "email@google.com";
        String password = "password";
        String name = "홍길동";
        String phoneNumber = "010-1234-5678";
        requestUser = new RequestUser(email, password, name, phoneNumber);
        // stub
        when(userRepository.existsByEmail(requestUser.getEmail())).thenReturn(true);
        // when & then
        assertThrows(BusinessLogicException.class, () -> userServiceImpl.register(requestUser),
                ExceptionCode.EMAIL_DUPLICATE.getMessage());
    }

    @Test
    @DisplayName("회원가입 실패 테스트 - 중복된 전화번호")
    void registerFail_DuplicatePhoneNumber() {
        // given
        String email = "email@google.com";
        String password = "password";
        String name = "홍길동";
        String phoneNumber = "010-1234-5678";
        requestUser = new RequestUser(email, password, name, phoneNumber);
        // stub
        when(userRepository.existsByEmail(requestUser.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(requestUser.getPhoneNumber())).thenReturn(true);
        // then
        assertThrows(BusinessLogicException.class, () -> userServiceImpl.register(requestUser),
                ExceptionCode.PHONE_NUMBER_DUPLICATE.getMessage());
    }

    @Test
    @DisplayName("유저 조회 성공 테스트")
    void getUserResponseByUserId_Success() {
        // given
        Long userId = 1L;
        String email = "email@google.com";
        String name = "홍길동";
        String phoneNumber = "010-1234-5678";
        userResponse = new UserResponse(userId, email, name, phoneNumber, LocalDate.now(), true);
        // stub
        when(userRepository.findUserResponseByUserId(userId)).thenReturn(userResponse);
        // when
        UserResponse response = userServiceImpl.getUserResponseByUserId(userId);
        // then
        assertNotNull(response);
        assertEquals(userResponse.getEmail(), response.getEmail());
    }

    @Test
    @DisplayName("유저 조회 실패 테스트")
    void getUserResponseByUserId_Fail_NotFound() {
        // given
        Long userId = 1L;
        when(userRepository.findUserResponseByUserId(userId)).thenReturn(null);
        // then
        assertThrows(BusinessLogicException.class, () -> userServiceImpl.getUserResponseByUserId(userId),
                "존재하지 않는 유저 ID로 조회 시 예외 발생해야 함");
    }

    @Test
    @DisplayName("이메일로 유저 조회 성공 테스트")
    void findUserResponseByEmail_Success() {
        // given
        Long userId = 1L;
        String email = "email@google.com";
        String name = "홍길동";
        String phoneNumber = "010-1234-5678";
        userResponse = new UserResponse(userId, email, name, phoneNumber, LocalDate.now(), true);
        when(userRepository.findUserResponseByEmail(email)).thenReturn(userResponse);
        // when
        UserResponse response = userServiceImpl.findUserResponseByEmail(email);
        // then
        assertNotNull(response);
        assertEquals(userResponse.getEmail(), response.getEmail());
    }

    @Test
    @DisplayName("이메일로 유저 조회 실패 테스트")
    void findUserResponseByEmail_Fail_NotFound() {
        // given
        String email = "notfound@example.com";
        when(userRepository.findUserResponseByEmail(email)).thenReturn(null);
        // then
        assertThrows(BusinessLogicException.class, () -> userServiceImpl.findUserResponseByEmail(email),
                "존재하지 않는 이메일로 조회 시 예외 발생해야 함");
    }

    @Test
    @DisplayName("이메일 인증번호 전송 성공 테스트")
    void sendCodeToEmailSuccess() {
        // given
        when(userRepository.findByEmail("email@google.com")).thenReturn(Optional.empty());
        doNothing().when(mailService).sendEmail(anyString(), anyString(), anyString());
        doNothing().when(redisService).setValues(anyString(), anyString(), any(Duration.class));
        // when
        userServiceImpl.sendCodeToEmail("email@google.com");
        // then
        verify(mailService, times(1)).sendEmail(eq("email@google.com"), eq("STUDIO_EYE 회원가입 이메일 인증"),
                contains("인증 번호: ")); // 인증 코드 형식 확인
        verify(redisService, times(1)).setValues(startsWith("AuthCode email@google.com"), anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("이메일 인증번호 전송 실패 테스트 - 중복 이메일")
    void sendCodeToEmailFail_DuplicateEmail() {
        String email = "email@google.com";
        String password = "password";
        String name = "홍길동";
        String phoneNumber = "010-1234-5678";
        User user = new User(1L, email, name, phoneNumber, password, true);
        // given
        when(userRepository.findByEmail("email@google.com")).thenReturn(Optional.of(user));
        // when & then
        assertThrows(BusinessLogicException.class, () -> userServiceImpl.sendCodeToEmail("email@google.com"));
    }

    @Test
    @DisplayName("중복 이메일 체크 테스트")
    void sendCodeToEmailDuplicateCheck() {
        // given
        String email = "email@google.com";
        String password = "password";
        String name = "홍길동";
        String phoneNumber = "010-1234-5678";
        User user = new User(1L, email, name, phoneNumber, password, true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        // when & then
        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> userServiceImpl.sendCodeToEmail(email));
        assertEquals(ExceptionCode.EMAIL_DUPLICATE, exception.getExceptionCode());
    }

    @Test
    @DisplayName("랜덤 인증번호 생성 테스트")
    void createCodeTest() throws Exception {
        // given
        Method createCodeMethod = UserServiceImpl.class.getDeclaredMethod("createCode");
        createCodeMethod.setAccessible(true);
        // when
        String code = (String) createCodeMethod.invoke(userServiceImpl);
        // then
        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}"));
    }

    @Test
    @DisplayName("인증번호 확인 성공 테스트")
    void verifyCodeSuccess() {
        // given
        String authCode = "123456";
        when(redisService.getValues("AuthCode email@google.com")).thenReturn(authCode);
        when(redisService.checkExistsValue(anyString())).thenReturn(true);
        // when
        EmailVerificationResult result = userServiceImpl.verifiedCode("email@google.com", authCode);
        // then
        assertTrue(result.isVerificationStatus(), "Email verification should succeed");
        assertEquals("Email verification successful", result.getMessage());
    }

    @Test
    @DisplayName("인증번호 확인 실패 테스트")
    void verifyCodeFailIncorrectCode() {
        // given
        String correctAuthCode = "123456";
        String incorrectAuthCode = "654321";
        when(redisService.getValues("AuthCode email@google.com")).thenReturn(correctAuthCode);
        when(redisService.checkExistsValue(anyString())).thenReturn(true);
        // when
        EmailVerificationResult result = userServiceImpl.verifiedCode("email@google.com", incorrectAuthCode);
        // then
        assertFalse(result.isVerificationStatus(), "Email verification should fail due to incorrect code");
        assertEquals("Email verification failed", result.getMessage());
    }

    @Test
    @DisplayName("모든 승인된 사용자 ID 조회 테스트")
    void getAllApprovedUserIdsTest() {
        // given
        List<Long> approvedUserIds = List.of(1L, 2L, 3L);
        when(userRepository.getAllApprovedUserIds()).thenReturn(approvedUserIds);
        // when
        List<Long> result = userServiceImpl.getAllApprovedUserIds();
        // then
        assertEquals(approvedUserIds, result);
    }

    // 모든 승인된 사용자 ID 조회 실패 테스트 - 결과가 비어있을 경우
    @Test
    @DisplayName("모든 승인된 사용자 ID 조회 실패 테스트 - 비어있는 경우")
    void getAllApprovedUserIdsFail_NoApprovedUsers() {
        // given
        when(userRepository.getAllApprovedUserIds()).thenReturn(Collections.emptyList());
        // when
        List<Long> result = userServiceImpl.getAllApprovedUserIds();
        // then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("회원 탈퇴 성공 테스트")
    void unregisterSuccess() {
        // given
        Long userId = 1L;
        doNothing().when(userRepository).deleteById(userId);
        // when
        ResponseEntity response = userServiceImpl.unregister(userId);
        // then
        verify(userRepository, times(1)).deleteById(userId);
        assertNull(response);
    }

    // 회원 탈퇴 실패 테스트 - 존재하지 않는 사용자 ID
    @Test
    @DisplayName("회원 탈퇴 실패 테스트 - 존재하지 않는 사용자 ID")
    void unregisterFail_UserNotFound() {
        // given
        Long userId = 1L;
        doThrow(new EmptyResultDataAccessException(1)).when(userRepository).deleteById(userId);
        // when & then
        assertThrows(EmptyResultDataAccessException.class, () -> userServiceImpl.unregister(userId));
    }

    @Test
    @DisplayName("모든 사용자 조회 성공 테스트")
    void getAllUsersSuccess() {
        // given
        Long userId = 1L;
        String email = "email@google.com";
        String name = "홍길동";
        String phoneNumber = "010-1234-5678";
        userResponse = new UserResponse(userId, email, name, phoneNumber, LocalDate.now(), true);
        List<UserResponse> users = List.of(userResponse);
        when(userRepository.findAllUsers()).thenReturn(users);
        // when
        List<UserResponse> result = userServiceImpl.getAllUsers();
        // then
        assertEquals(users, result);
    }

    // 모든 사용자 조회 실패 테스트 - 조회 결과가 없을 경우
    @Test
    @DisplayName("모든 사용자 조회 실패 테스트 - 조회 결과 없음")
    void getAllUsersFail_NoUsersFound() {
        // given
        when(userRepository.findAllUsers()).thenReturn(Collections.emptyList());
        // when
        List<UserResponse> result = userServiceImpl.getAllUsers();
        // then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("사용자 승인 상태 변경 성공 테스트")
    void updateApprovedStatus() {
        // given
        Long userId = 1L;
        String email = "email@google.com";
        String password = "password";
        String name = "홍길동";
        String phoneNumber = "010-1234-5678";
        User user = new User(1L, email, name, phoneNumber, password, true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        // when
        boolean result = userServiceImpl.updateApproved(userId, true);
        // then
        assertTrue(result);
        assertTrue(user.isApproved());
    }

    // 사용자 승인 상태 변경 실패 테스트 - 존재하지 않는 사용자 ID
    @Test
    @DisplayName("사용자 승인 상태 변경 실패 테스트 - 사용자 없음")
    void updateApprovedFail_UserNotFound() {
        // given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        // when & then
        assertThrows(NoSuchElementException.class, () -> userServiceImpl.updateApproved(userId, true));
    }
}