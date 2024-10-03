package studio.studioeye.domain.menu.domain;

import studio.studioeye.domain.menu.dto.request.UpdateMenuServiceRequestDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private Boolean visibility;

    public void update(UpdateMenuServiceRequestDto dto) {
        title = dto.title();
        visibility = dto.visibility();
    }

    @Builder
    public Menu(String title, Boolean visibility) {
        this.title = title;
        this.visibility = visibility;
    }
}
