package studio.studioeye.domain.request.dao;

import studio.studioeye.domain.request.domain.Request;
import studio.studioeye.domain.request.domain.State;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    Page<Request> findAll(Pageable pageable);
    List<Request> findByState(State state);
    Long countByState(State state);
    // 기간 중 request 수
    @Query("SELECT r.year AS year, r.month AS month, COUNT(r) AS requestCount " +
            "FROM Request r " +
            "WHERE (r.year > :startYear OR (r.year = :startYear AND r.month >= :startMonth)) " +
            "AND (r.year < :endYear OR (r.year = :endYear AND r.month <= :endMonth))" +
            "GROUP BY r.year, r.month")
    List<RequestCount> findByYearAndMonthBetween(@Param("startYear") Integer startYear,
                                                 @Param("startMonth") Integer startMonth,
                                                 @Param("endYear") Integer endYear,
                                                 @Param("endMonth") Integer endMonth);

    // 기간 중 category마다의 request 수
    @Query("SELECT r.year AS year, r.month AS month, r.category AS category, COUNT(r) AS requestCount " +
            "FROM Request r " +
            "WHERE (r.year > :startYear OR (r.year = :startYear AND r.month >= :startMonth)) " +
            "AND (r.year < :endYear OR (r.year = :endYear AND r.month <= :endMonth)) " +
            "GROUP BY r.year, r.month, r.category")
    List<RequestCategoryCount> findCategoryReqNumByYearAndMonthBetween(@Param("startYear") Integer startYear,
                                                                        @Param("startMonth") Integer startMonth,
                                                                        @Param("endYear") Integer endYear,
                                                                        @Param("endMonth") Integer endMonth);
    // 기간 중 state별 request 수
    @Query("SELECT r.year AS year, r.month AS month, r.state AS state, COUNT(r) AS requestCount " +
            "FROM Request r " +
            "WHERE (r.year > :startYear OR (r.year = :startYear AND r.month >= :startMonth)) " +
            "AND (r.year < :endYear OR (r.year = :endYear AND r.month <= :endMonth)) " +
            "GROUP BY r.year, r.month, r.state")
    List<RequestStateCount> findStateReqNumByYearAndMonthBetween(@Param("startYear") Integer startYear,
                                                                 @Param("startMonth") Integer startMonth,
                                                                 @Param("endYear") Integer endYear,
                                                                 @Param("endMonth") Integer endMonth);

}
