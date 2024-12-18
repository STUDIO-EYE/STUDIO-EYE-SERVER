package studio.studioeye.domain.views.dto.request;

import studio.studioeye.domain.menu.domain.MenuTitle;
import studio.studioeye.domain.project.domain.ArtworkCategory;

public record UpdateViewsServiceRequestDto(
        MenuTitle menu,
        ArtworkCategory category
) {
}
