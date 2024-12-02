package studio.studioeye.domain.recruitment.domain;

import studio.studioeye.domain.recruitment.dto.request.UpdateRecruitmentServiceRequestDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recruitment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private String title;
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date deadline;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @NotNull
    private String link;

    @Builder
    public Recruitment(String title, Date startDate, Date deadline, String link, Date createdAt, Status status) {
        this.title = title;
        this.startDate = startDate;
        this.deadline = deadline;
        this.link = link;
        this.createdAt = createdAt;
        this.status = status;
    }

    public void update(UpdateRecruitmentServiceRequestDto dto, Status status) {
        this.title = dto.title();
        this.startDate = dto.startDate();
        this.deadline = dto.deadline();
        this.link = dto.link();
        this.status = status;
    }
}
