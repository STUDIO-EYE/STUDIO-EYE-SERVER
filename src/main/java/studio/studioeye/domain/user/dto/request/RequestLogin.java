package studio.studioeye.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequestLogin {

    @Email
    @NotNull(message = "이메일을 입력해주세요")
    @Size(min = 5, message = "이메일은 5자 이상이어야 합니다")
    private String email;

    @NotNull(message = "비밀번호를 입력해주세요")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    private String pwd;
}
