package studio.studioeye.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RequestUser {

    @NotNull(message = "이메일을 입력해주세요")
    @Size(min = 5, message = "이메일은 5자 이상이어야 합니다")
    @Email
    private String email;

    @NotNull(message = "비밀번호를 입력해주세요")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    private String pwd;

    @NotNull(message = "이름을 입력해주세요")
    @Size(min = 2, message = "이름은 2자 이상이어야 합니다")
    private String name;

    @NotNull(message = "전화번호를 입력해주세요")
    @Size(min = 10, max = 15, message = "전화번호는 10자에서 15자 사이여야 합니다")
    private String phoneNumber;

}