package studio.studioeye.domain.ceo.dao;

import studio.studioeye.domain.ceo.domain.Ceo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CeoRepository extends JpaRepository<Ceo, Long> {
}
