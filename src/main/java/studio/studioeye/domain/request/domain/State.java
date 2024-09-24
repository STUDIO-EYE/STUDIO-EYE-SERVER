package studio.studioeye.domain.request.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum State {
    WAITING, //대기
    APPROVED, //승인
    REJECTED, //거절
    DISCUSSING; //논의 중
}
