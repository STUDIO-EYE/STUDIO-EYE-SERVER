package studio.studioeye.domain.menu.domain;

import studio.studioeye.domain.menu.dto.request.UpdateMenuRequestDto;
import jakarta.persistence.*;
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

    @Enumerated(EnumType.STRING)
    private MenuTitle menuTitle;

    private Boolean visibility;

    private Integer sequence;

    public void update(UpdateMenuRequestDto dto) {
        visibility = dto.visibility();
        sequence = dto.sequence();
    }

    @Builder
    public Menu(MenuTitle menuTitle, Boolean visibility, Integer sequence) {
        this.menuTitle = menuTitle;
        this.visibility = visibility;
        this.sequence = sequence;
    }
}
