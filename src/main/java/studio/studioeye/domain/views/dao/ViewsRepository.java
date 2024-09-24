package studio.studioeye.domain.views.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import studio.studioeye.domain.views.domain.Views;

import java.util.List;
import java.util.Optional;

public interface ViewsRepository extends JpaRepository<Views, Long> {
    Optional<Views> findByYearAndMonth(Integer year, Integer month);
    List<Views> findByYear(Integer year);
    @Query("SELECT v FROM Views v WHERE (v.year > :startYear OR (v.year = :startYear AND v.month >= :startMonth)) " +
            "AND (v.year < :endYear OR (v.year = :endYear AND v.month <= :endMonth))")
    List<Views> findByYearAndMonthBetween(@Param("startYear") Integer startYear,
                                          @Param("startMonth") Integer startMonth,
                                          @Param("endYear") Integer endYear,
                                          @Param("endMonth") Integer endMonth);
}
