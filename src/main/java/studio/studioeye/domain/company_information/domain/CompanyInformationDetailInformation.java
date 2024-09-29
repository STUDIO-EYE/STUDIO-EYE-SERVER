package studio.studioeye.domain.company_information.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyInformationDetailInformation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_information_id")
    @JsonBackReference
    private CompanyInformation companyInformation;

    @Column(name = "detail_information_key")
    private String key;

    @Column(name = "detail_information")
    private String value;


    @Builder
    public CompanyInformationDetailInformation(CompanyInformation companyInformation, String key, String value) {
        this.companyInformation = companyInformation;
        this.key = key;
        this.value = value;
    }
}
