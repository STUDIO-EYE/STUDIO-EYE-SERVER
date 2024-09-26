package studio.studioeye.global.exception;

import lombok.Getter;
import studio.studioeye.global.exception.error.ExceptionCode;

public class BusinessLogicException extends RuntimeException {

    @Getter
    private ExceptionCode exceptionCode;

    public BusinessLogicException(ExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }
}
