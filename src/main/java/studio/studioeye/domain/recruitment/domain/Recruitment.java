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
    private String period;

    @NotNull
    private Boolean status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String qualifications;

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String preferential;

    @Builder
    public Recruitment(String title, String period, Date createdAt, String qualifications, String preferential) {
        this.title = title;
        this.period = period;
        this.qualifications = qualifications;
        this.preferential = preferential;
        this.status = true;
        this.createdAt = createdAt;
    }

    public void update(UpdateRecruitmentServiceRequestDto dto) {
        this.title = dto.title();
        this.period = dto.period();
        this.qualifications = dto.qualifications();
        this.preferential = dto.preferential();
    }
}
