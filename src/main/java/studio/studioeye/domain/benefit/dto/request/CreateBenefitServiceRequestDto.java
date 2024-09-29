package studio.studioeye.domain.benefit.dto.request;

import studio.studioeye.domain.benefit.domain.Benefit;

public record CreateBenefitServiceRequestDto(
        String title,
        String content
) {
    public Benefit toEntity(String imageUrl, String imageFileName) {
        return Benefit.builder()
                .imageUrl(imageUrl)
                .imageFileName(imageFileName)
                .title(title)
                .content(content)
                .build();
    }
}
