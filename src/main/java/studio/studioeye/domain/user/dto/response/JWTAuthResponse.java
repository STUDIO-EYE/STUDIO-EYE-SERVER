package studio.studioeye.domain.user.dto.response;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JWTAuthResponse {
    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpireDate;
    private Long id;
    private boolean isApproved;

    @Builder
    public JWTAuthResponse(String accessToken, String refreshToken, Long accessTokenExpireDate, Long id, boolean isApproved) {
        this.tokenType = "Bearer";
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpireDate = accessTokenExpireDate;
        this.id = id;
        this.isApproved = isApproved;
    }
}
