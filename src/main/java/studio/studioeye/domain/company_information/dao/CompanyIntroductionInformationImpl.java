package studio.studioeye.domain.company_information.dao;

public class CompanyIntroductionInformationImpl implements CompanyIntroductionInformation {

    private String introduction;
    private String sloganImageUrl;

    public CompanyIntroductionInformationImpl(String introduction, String sloganImageUrl) {
        this.introduction = introduction;
        this.sloganImageUrl = sloganImageUrl;
    }

    @Override
    public String getIntroduction() {
        return introduction;
    }

    @Override
    public String getSloganImageUrl() {
        return sloganImageUrl + "?v=" + System.currentTimeMillis();
    }
}
