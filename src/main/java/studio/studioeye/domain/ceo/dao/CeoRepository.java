package studio.studioeye.domain.ceo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import studio.studioeye.domain.ceo.domain.Ceo;

public interface CeoRepository extends JpaRepository<Ceo, Long> {
}
