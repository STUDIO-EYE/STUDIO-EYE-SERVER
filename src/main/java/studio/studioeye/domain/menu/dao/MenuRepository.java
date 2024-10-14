package studio.studioeye.domain.menu.dao;

import studio.studioeye.domain.menu.domain.Menu;
import studio.studioeye.domain.menu.domain.MenuTitle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    @Query("SELECT m.menuTitle AS menuTitle FROM Menu m WHERE m.visibility = true ORDER BY m.sequence ASC")
    List<MenuTitle> findTitleByVisibilityTrue();
}
