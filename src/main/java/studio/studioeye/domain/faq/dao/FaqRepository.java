package studio.studioeye.domain.faq.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import studio.studioeye.domain.faq.domain.Faq;

import java.util.List;

public interface FaqRepository extends JpaRepository<Faq, Long> {
    @Query("SELECT f.id AS id, f.question AS question FROM Faq f")
    List<FaqQuestions> findAllQuestions();

    Page<Faq> findAll(Pageable pageable);
}
