package studio.studioeye.domain.company_information.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import studio.studioeye.domain.company_information.dto.request.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String mainOverview;

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String commitment;

    @NotNull
    private String address;

    @NotNull
    private String addressEnglish;

    @NotNull
    private String logoImageFileName;

    @NotNull
    private String logoImageUrl;

    @NotNull
    private String phone;

    @NotNull
    private String fax;

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String introduction;

    @NotNull
    private String sloganImageFileName;

    @NotNull
    private String sloganImageUrl;

    @OneToMany(mappedBy = "companyInformation", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<CompanyInformationDetailInformation> detailInformation;

    @Builder
    public CompanyInformation(String mainOverview, String commitment,
                              String address,
                              String addressEnglish,
                              String logoImageFileName, String logoImageUrl,
                              String phone,
                              String fax,
                              String introduction,
                              String sloganImageFileName, String sloganImageUrl,
                              List<CompanyInformationDetailInformation> detailInformation) {
        this.mainOverview = mainOverview;
        this.commitment = commitment;
        this.address = address;
        this.addressEnglish = addressEnglish;
        this.logoImageFileName = logoImageFileName;
        this.logoImageUrl = logoImageUrl;
        this.phone = phone;
        this.fax = fax;
        this.introduction = introduction;
        this.sloganImageFileName = sloganImageFileName;
        this.sloganImageUrl = sloganImageUrl;
        this.detailInformation = new ArrayList<>();
        if (detailInformation != null) {
            this.detailInformation.addAll(detailInformation);
        }
    }

    public void deleteLogoImage() {
        this.logoImageFileName = null;
        this.logoImageUrl = null;
    }

    public void deleteCompanyBasicInformation() {
        this.address = null;
        this.addressEnglish = null;
        this.phone = null;
        this.fax = null;
    }

    public void deleteCompanyDetailInformation() {
        if (this.detailInformation != null) {
            this.detailInformation.clear();
        }
    }

    public void deleteCompanyIntroductionInformation() {
        this.introduction = null;
        this.sloganImageFileName = null;
        this.sloganImageUrl = null;
    }

    public void updateCompanyLogo(String logoImageFileName, String logoImageUrl) {
        this.logoImageFileName = logoImageFileName;
        this.logoImageUrl = logoImageUrl;
    }

    public void updateCompanySlogan(String sloganImageFileName, String sloganImageUrl) {
        this.sloganImageFileName = sloganImageFileName;
        this.sloganImageUrl = sloganImageUrl;
    }

    public void updateAllCompanyInformation(UpdateAllCompanyInformationServiceRequestDto dto, String logoImageFileName,
                                            String logoImageUrl, String sloganImageFileName, String sloganImageUrl) {
        this.mainOverview = dto.mainOverview();
        this.commitment = dto.commitment();
        this.address = dto.address();
        this.addressEnglish = dto.addressEnglish();
        this.logoImageFileName = logoImageFileName;
        this.logoImageUrl = logoImageUrl;
        this.phone = dto.phone();
        this.fax = dto.fax();
        this.introduction = dto.introduction();
        this.sloganImageFileName = sloganImageFileName;
        this.sloganImageUrl = sloganImageUrl;
        updateDetailInformation(dto.detailInformation());
    }

    public void updateAllCompanyTextInformation(UpdateAllCompanyInformationServiceRequestDto dto) {
        this.mainOverview = dto.mainOverview();
        this.commitment = dto.commitment();
        this.address = dto.address();
        this.addressEnglish = dto.addressEnglish();
        this.phone = dto.phone();
        this.fax = dto.fax();
        this.introduction = dto.introduction();
        updateDetailInformation(dto.detailInformation());
    }

    public void updateCompanyBasicInformation(UpdateCompanyBasicInformationServiceRequestDto dto) {
        this.address = dto.address();
        this.addressEnglish = dto.addressEnglish();
        this.phone = dto.phone();
        this.fax = dto.fax();
    }

    public void updateCompanyDetailInformation(UpdateCompanyDetailInformationServiceRequestDto dto) {
        updateDetailInformation(dto.detailInformation());
    }

    public void updateCompanyIntroductionInformation(UpdateCompanyIntroductionInformationServiceRequestDto dto) {
        this.mainOverview = dto.mainOverview();
        this.commitment = dto.commitment();
        this.introduction = dto.introduction();
    }

    public void updateCompanyLogoAndSlogan(String logoImageFileName, String logoImageUrl, String sloganImageFileName, String sloganImageUrl) {
        this.logoImageFileName = logoImageFileName;
        this.logoImageUrl = logoImageUrl;
        this.sloganImageFileName = sloganImageFileName;
        this.sloganImageUrl = sloganImageUrl;
    }

    private void updateDetailInformation(List<DetailInformationDTO> dtos) {
        if (dtos == null) {
            this.detailInformation = null;
            return;
        }

        this.detailInformation.clear();

        for (DetailInformationDTO dto : dtos) {
            CompanyInformationDetailInformation detail = CompanyInformationDetailInformation.builder()
                    .companyInformation(this)
                    .key(dto.getKey())
                    .value(dto.getValue())
                    .build();
            this.detailInformation.add(detail);
        }
    }

    public void initDetailInformation( List<CompanyInformationDetailInformation> detailInformation) {
        if (this.detailInformation == null)
            this.detailInformation = new ArrayList<>();
        this.detailInformation = detailInformation;
    }

}

