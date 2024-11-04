package studio.studioeye.domain.user.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final MailService mailService;
    private final RedisService redisService;

    @Value("${jwt.access.expiration}")
    private long accessTokenValidityInSeconds;

    @Value("${spring.mail.auth-code-expiration-millis}")
    private long authCodeExpirationMillis;

    private static final String AUTH_CODE_PREFIX = "AuthCode ";

    @Override
    public JWTAuthResponse login(RequestLogin requestLogin) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                requestLogin.getEmail(), requestLogin.getPwd()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        Long userId = userDetailsServiceImpl.findUserIdByEmail(requestLogin.getEmail());

        User user = userRepository.findById(userId).orElseThrow(); // new ExceptionCode.MEMBER_NOT_FOUND

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken();

        return JWTAuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpireDate(accessTokenValidityInSeconds)
                .id(userId)
                .isApproved(user.isApproved())
                .build();
    }

    @Override
    public String register(RequestUser requestUser) {

        // add check for email & phoneNumber exists in database
        if(userRepository.existsByEmail(requestUser.getEmail())){
            throw new BusinessLogicException(ExceptionCode.EMAIL_DUPLICATE);
        }
        if (userRepository.existsByPhoneNumber(requestUser.getPhoneNumber())){
            throw new BusinessLogicException(ExceptionCode.PHONE_NUMBER_DUPLICATE);
        }

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        User User = mapper.map(requestUser, User.class);
        User.setEncryptedPwd(passwordEncoder.encode(requestUser.getPwd()));
        User.setApproved(false);
        User.setCreatedAt(LocalDate.now());
        userRepository.save(User);

        return "User registered successfully!";
    }

    @Override
    public UserResponse getUserResponseByUserId(Long userId) {
        UserResponse userResponse = userRepository.findUserResponseByUserId(userId);
        if (userResponse == null) {
            throw new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND);
        }
        return userResponse;
    }

    @Override
    public UserResponse findUserResponseByEmail(String email) {
        UserResponse userResponse = userRepository.findUserResponseByEmail(email);
        if (userResponse == null) {
            throw new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND);
        }
        return userResponse;
    }

    // 토큰 재발급
//    @Override
//    public JWTAuthResponse reissueAccessToken(String refreshToken) {
//        this.verifiedRefreshToken(refreshToken);
//        String email = jwtTokenProvider.getEmail(refreshToken);
//        String redisRefreshToken = redisService.getValues(email);
//
//        if (redisService.checkExistsValue(redisRefreshToken) && refreshToken.equals(redisRefreshToken)) {
//            Optional<User> findUser = this.findOne(email);
//            User User = User.of(findUser);
//            JWTAuthResponse tokenDto = jwtTokenProvider.generateToken(email, jwtTokenProvider.getAuthentication(refreshToken), User.getId());
//            String newAccessToken = tokenDto.getAccessToken();
//            long refreshTokenExpirationMillis = jwtTokenProvider.getRefreshTokenExpirationMillis();
//            return tokenDto;
//        } else throw new BusinessLogicException(ExceptionCode.TOKEN_IS_NOT_SAME);
//    }

    //이메일 인증번호 관련 메소드
    public void sendCodeToEmail(String toEmail) {
        this.checkDuplicatedEmail(toEmail);
        String title = "STUDIO_EYE 회원가입 이메일 인증";
        String authCode = this.createCode();

        // 인증 이메일 내용을 작성
        String emailContent = "안녕하세요,\n\n";
        emailContent += "STUDIO_EYE에서 발송한 이메일 인증 번호는 다음과 같습니다:\n\n";
        emailContent += "인증 번호: " + authCode + "\n\n";
        emailContent += "이 인증 번호를 STUDIO_EYE 웹에서 입력하여 이메일을 인증해주세요.\n\n";
        emailContent += "감사합니다,\nSTUDIO_EYE 팀";

        mailService.sendEmail(toEmail, title, emailContent);

        // 이메일 인증 요청 시 인증 번호 Redis에 저장( key = "AuthCode " + Email / value = AuthCode )
        redisService.setValues(AUTH_CODE_PREFIX + toEmail,
                authCode, Duration.ofMillis(this.authCodeExpirationMillis));
    }

    //중복 이메일 체크
    private void checkDuplicatedEmail(String email) {
        Optional<User> User = userRepository.findByEmail(email);
        if (User.isPresent()) {
            log.debug("UserServiceImpl.checkDuplicatedEmail exception occur email: {}", email);
            throw new BusinessLogicException(ExceptionCode.EMAIL_DUPLICATE);
        }
    }

    //랜덤 인증번호 생성
    private String createCode() {
        int lenth = 6;
        try {
            Random random = SecureRandom.getInstanceStrong();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < lenth; i++) {
                builder.append(random.nextInt(10));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            log.debug("MemberService.createCode() exception occur");
            throw new BusinessLogicException(ExceptionCode.NO_SUCH_ALGORITHM);
        }
    }

    //인증번호 확인
    public EmailVerificationResult verifiedCode(String email, String authCode) {
        this.checkDuplicatedEmail(email);
        String redisAuthCode = redisService.getValues(AUTH_CODE_PREFIX + email);
        boolean authResult = redisService.checkExistsValue(redisAuthCode) && redisAuthCode.equals(authCode);

        return EmailVerificationResult.of(authResult);
    }

    @Override
    public List<Long> getAllApprovedUserIds() {
        return userRepository.getAllApprovedUserIds();
    }

    @Override
    public ResponseEntity unregister(Long userId) {
        userRepository.deleteById(userId);

        return null;
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<UserResponse> userResponses = userRepository.findAllUsers();
        if (userResponses == null) {
            throw new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND);
        }
        return userResponses;
    }

    @Override
    public boolean updateApproved(Long userId, boolean approved) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setApproved(approved);
        userRepository.save(user);
        return user.isApproved();
    }
}
