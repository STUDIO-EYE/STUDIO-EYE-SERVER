package studio.studioeye.domain.recruitment.dao;

import studio.studioeye.domain.recruitment.domain.Status;

public interface RecruitmentTitle {
    Long getId();
    String getTitle();
    Status getStatus();
}
