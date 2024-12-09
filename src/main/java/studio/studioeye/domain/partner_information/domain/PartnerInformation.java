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

	private Boolean isMain;

	private String link;

	@Builder
	public PartnerInformation(String logoImageUrl, String name, Boolean isMain, String link) {
		this.name = name;
		this.logoImageUrl = logoImageUrl;
		this.isMain = isMain;
		this.link = link;
	}

}
