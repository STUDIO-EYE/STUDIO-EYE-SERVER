package studio.studioeye.domain.ceo.dto.request;

import studio.studioeye.domain.ceo.domain.Ceo;

public record CreateCeoServiceRequestDto(
    String name,
    String introduction
) {
    public Ceo toEntity(String imageFileName, String imageUrl) {
        return Ceo.builder()
                .name(name)
                .introduction(introduction)
                .imageFileName(imageFileName)
                .imageUrl(imageUrl)
                .build();
    }
    public UpdateCeoServiceRequestDto toUpdateServiceRequest() {
        return new UpdateCeoServiceRequestDto(name, introduction);
    }
}
