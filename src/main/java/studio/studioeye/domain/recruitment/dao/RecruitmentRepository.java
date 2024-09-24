package studio.studioeye.domain.recruitment.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import studio.studioeye.domain.recruitment.domain.Recruitment;

public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {

    @Query("SELECT r.id AS id, r.title AS title FROM Recruitment r")
    Page<RecruitmentTitle> findAllRecruitments(Pageable pageable);
}
