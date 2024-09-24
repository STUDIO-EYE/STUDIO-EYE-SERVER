package studio.studioeye.domain.partner_information.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PartnerInformation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private String logoImageUrl;

	private Boolean is_main;

	private String link;

	@Builder
	public PartnerInformation(String logoImageUrl, String name, Boolean is_main, String link) {
		this.name = name;
		this.logoImageUrl = logoImageUrl;
		this.is_main = is_main;
		this.link = link;
	}

}
