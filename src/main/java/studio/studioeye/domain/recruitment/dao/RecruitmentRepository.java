package studio.studioeye.domain.recruitment.dao;

import studio.studioeye.domain.recruitment.domain.Recruitment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {

    @Query("SELECT r.id AS id, r.title AS title, r.status AS status FROM Recruitment r  ORDER BY r.deadline ASC")
    Page<RecruitmentTitle> findAllRecruitments(Pageable pageable);

    Optional<Recruitment> findTopByOrderByCreatedAtDesc();

    @Query("SELECT r FROM Recruitment r WHERE r.status = 'PREPARING' OR r.status = 'OPEN'")
    List<Recruitment> findByStatusNotClose();
}
