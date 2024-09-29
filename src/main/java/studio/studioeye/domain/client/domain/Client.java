package studio.studioeye.domain.client.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import studio.studioeye.domain.client.dto.request.UpdateClientServiceRequestDto;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String logoImg;

    private Boolean visibility;

    @Builder
    public Client(String name, String logoImg, Boolean visibility) {
        this.name = name;
        this.logoImg = logoImg;
        this.visibility = visibility;
    }

    public Client update(UpdateClientServiceRequestDto dto) {
        this.name = dto.name();
        this.visibility = dto.visibility();
        return this;
    }
}
