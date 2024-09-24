package studio.studioeye.domain.partner_information.dto.request;

public record UpdatePartnerInfoServiceRequestDto(
	Long id,
	String name,
	Boolean is_main,
	String link
) {


}
