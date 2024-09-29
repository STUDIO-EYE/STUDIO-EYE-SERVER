package studio.studioeye.domain.partner_information.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import studio.studioeye.domain.partner_information.domain.PartnerInformation;

public interface PartnerInformationRepository extends JpaRepository<PartnerInformation, Long> {
    Page<PartnerInformation> findAll(Pageable pageable);

}
