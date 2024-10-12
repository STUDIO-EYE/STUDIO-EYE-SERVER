package studio.studioeye.domain.views.dao;

import studio.studioeye.domain.menu.domain.MenuTitle;
import studio.studioeye.domain.project.domain.ArtworkCategory;
import studio.studioeye.domain.views.domain.Views;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ViewsRepository extends JpaRepository<Views, Long> {
    Optional<Views> findByYearAndMonth(Integer year, Integer month);
    Optional<Views> findByYearAndMonthAndMenuAndCategory(Integer year, Integer month, MenuTitle menu, ArtworkCategory category);
    List<Views> findByYear(Integer year);
    @Query("SELECT v.year AS year, v.month AS month, SUM(v.views) AS views FROM Views v WHERE (v.year > :startYear OR (v.year = :startYear AND v.month >= :startMonth)) " +
            "AND (v.year < :endYear OR (v.year = :endYear AND v.month <= :endMonth)) GROUP BY v.year, v.month")
    List<ViewsSummary> findByYearAndMonthBetween(@Param("startYear") Integer startYear,
                                          @Param("startMonth") Integer startMonth,
                                          @Param("endYear") Integer endYear,
                                          @Param("endMonth") Integer endMonth);

    @Query("SELECT v.year AS year, v.month AS month, SUM(v.views) AS views FROM Views v WHERE (v.year > :startYear OR (v.year = :startYear AND v.month >= :startMonth)) " +
            "AND (v.year < :endYear OR (v.year = :endYear AND v.month <= :endMonth)) AND v.menu = :menu GROUP BY v.year, v.month, v.menu")
    List<ViewsSummary> findByYearAndMonthBetweenAndMenu(@Param("startYear") Integer startYear,
                                                        @Param("startMonth") Integer startMonth,
                                                        @Param("endYear") Integer endYear,
                                                        @Param("endMonth") Integer endMonth,
                                                        MenuTitle menu);

    @Query("SELECT v.year AS year, v.month AS month, SUM(v.views) AS views FROM Views v WHERE (v.year > :startYear OR (v.year = :startYear AND v.month >= :startMonth)) " +
            "AND (v.year < :endYear OR (v.year = :endYear AND v.month <= :endMonth)) AND v.menu = :menu AND v.category = :category GROUP BY v.year, v.month, v.menu, v.category")
    List<ViewsSummary> findByYearAndMonthBetweenAndMenuAndCategory(@Param("startYear") Integer startYear,
                                                                   @Param("startMonth") Integer startMonth,
                                                                   @Param("endYear") Integer endYear,
                                                                   @Param("endMonth") Integer endMonth,
                                                                   MenuTitle menu,
                                                                   ArtworkCategory category);
}
