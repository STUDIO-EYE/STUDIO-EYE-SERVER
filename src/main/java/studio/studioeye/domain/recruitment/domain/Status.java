package studio.studioeye.domain.recruitment.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Status {
    PREPARING("시작전"),
    OPEN("진행중"),
    CLOSE("마감");
    public final String label;
    public String getLabel() {
        return this.label;
    }
}