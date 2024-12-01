package studio.studioeye.domain.user.application;

import org.springframework.http.ResponseEntity;
import studio.studioeye.domain.user.dto.request.RequestLogin;
import studio.studioeye.domain.user.dto.request.RequestUser;
import studio.studioeye.domain.user.dto.response.EmailVerificationResult;
import studio.studioeye.domain.user.dto.response.JWTAuthResponse;
import studio.studioeye.domain.user.dto.response.UserResponse;

import java.util.List;

public interface UserService{

    JWTAuthResponse login(RequestLogin requestLogin);

    String register(RequestUser requestUser);

    UserResponse getUserResponseByUserId(Long userId);

    UserResponse findUserResponseByEmail(String email);

//    JWTAuthResponse reissueAccessToken(String encryptedRefreshToken);

    void sendCodeToEmail(String toEmail);

    EmailVerificationResult verifiedCode(String email, String authCode);

    List<Long> getAllApprovedUserIds();

    ResponseEntity unregister(Long userId);

    List<UserResponse> getAllUsers();

    boolean updateApproved(Long userId, boolean approved);
}

