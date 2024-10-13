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

    // 기간 중 category와 state에 따른 request 수
    @Query("SELECT r.year AS year, r.month AS month, r.state AS state, COUNT(r) AS requestCount " +
            "FROM Request r " +
            "WHERE (r.year > :startYear OR (r.year = :startYear AND r.month >= :startMonth)) " +
            "AND (r.year < :endYear OR (r.year = :endYear AND r.month <= :endMonth)) " +
            "AND (:category IS NULL OR r.category = :category) " +
            "AND (:state IS NULL OR r.state = :state) " +
            "GROUP BY r.year, r.month, r.state")
    List<RequestCount> findReqNumByYearAndMonthBetweenWithCategoryAndState(
            @Param("startYear") Integer startYear,
            @Param("startMonth") Integer startMonth,
            @Param("endYear") Integer endYear,
            @Param("endMonth") Integer endMonth,
            @Param("category") String category,
            @Param("state") State state);
}
