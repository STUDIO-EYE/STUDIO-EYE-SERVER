package studio.studioeye.domain.partner_information.dto.request;

import studio.studioeye.domain.partner_information.domain.PartnerInformation;

public record CreatePartnerInfoServiceRequestDto(
	String name,
	Boolean is_main,
	String link
) {

	public PartnerInformation toEntity(String logoImgStr) {
		return PartnerInformation.builder()
				.logoImageUrl(logoImgStr)
				.name(name)
				.is_main(is_main)
				.link(link)
				.build();
	}

}
