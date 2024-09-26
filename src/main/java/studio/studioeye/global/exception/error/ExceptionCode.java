package studio.studioeye.global.exception.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExceptionCode {
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않았습니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근이 금지되었습니다"),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "유효하지 않은 입력입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류입니다"),
    EMAIL_DUPLICATE(HttpStatus.CONFLICT, "이미 존재하는 회원입니다 (이메일 중복)"),
    PHONE_NUMBER_DUPLICATE(HttpStatus.CONFLICT, "이미 존재하는 회원입니다 (전화번호 중복)"),
    NO_SUCH_ALGORITHM(HttpStatus.INTERNAL_SERVER_ERROR, "존재하지 않는 알고리즘입니다"),
    UNABLE_TO_SEND_EMAIL(HttpStatus.INTERNAL_SERVER_ERROR, "이메일을 전송할 수 없습니다"),
    TOKEN_IS_NOT_SAME(HttpStatus.UNAUTHORIZED, "액세스 토큰과 리프레시 토큰이 일치하지 않습니다"),
    HEADER_REFRESH_TOKEN_NOT_EXISTS(HttpStatus.BAD_REQUEST, "요청 헤더에 리프레시 토큰이 누락되었습니다");

    private final HttpStatus status;
    private final String message;

}