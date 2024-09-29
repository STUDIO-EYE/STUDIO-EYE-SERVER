package studio.studioeye.domain.benefit.dao;

import studio.studioeye.domain.benefit.domain.Benefit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BenefitRepository extends JpaRepository<Benefit, Long> {
}
