package studio.studioeye.domain.ceo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import studio.studioeye.domain.ceo.dto.request.UpdateCeoServiceRequestDto;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ceo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String introduction;

    @NotNull
    private String imageFileName;

    @NotNull
    private String imageUrl;

    @Builder
    public Ceo(String name, String introduction, String imageFileName, String imageUrl) {
        this.name = name;
        this.introduction = introduction;
        this.imageFileName = imageFileName;
        this.imageUrl = imageUrl;
    }

    public void updateCeoInformation(UpdateCeoServiceRequestDto dto, String imageFileName, String imageUrl) {
        this.name = dto.name();
        this.introduction = dto.introduction();
        this.imageFileName = imageFileName;
        this.imageUrl = imageUrl;
    }
    public void updateCeoTextInformation(UpdateCeoServiceRequestDto dto) {
        this.name = dto.name();
        this.introduction = dto.introduction();
    }

    public void updateCeoImageInformation(String imageFileName, String imageUrl) {
        this.imageFileName = imageFileName;
        this.imageUrl = imageUrl;
    }

}
