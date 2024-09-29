package studio.studioeye.domain.company_information.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import studio.studioeye.domain.company_information.domain.CompanyInformation;

import java.util.List;

public interface CompanyInformationRepository extends JpaRepository<CompanyInformation, Long> {
    @Query("SELECT c.logoImageUrl AS logoImageUrl FROM CompanyInformation c")
    List<String> findLogoImageUrl();
    @Query("SELECT c.address AS address, c.addressEnglish AS addressEnglish, c.phone AS phone, c.fax AS fax FROM CompanyInformation c")
    List<CompanyBasicInformation> findAddressAndPhoneAndFax();
    @Query("SELECT c.introduction AS introduction, c.sloganImageUrl AS sloganImageUrl FROM CompanyInformation c")
    List<CompanyIntroductionInformation> findIntroductionAndSloganImageUrl();
}
