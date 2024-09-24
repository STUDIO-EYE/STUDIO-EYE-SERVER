package studio.studioeye.domain.request.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import studio.studioeye.domain.request.domain.Answer;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

}
