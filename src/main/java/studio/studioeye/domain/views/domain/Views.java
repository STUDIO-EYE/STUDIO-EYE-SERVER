package studio.studioeye.domain.views.domain;

import studio.studioeye.domain.menu.domain.MenuTitle;
import studio.studioeye.domain.project.domain.ArtworkCategory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Views {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "year_value")
	private Integer year;

	@Column(name = "month_value")
	private Integer month;

	private Long views;

	@Enumerated(EnumType.STRING)
	private MenuTitle menu;

	@Enumerated(EnumType.STRING)
	private ArtworkCategory category;

	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

	@Builder
	public Views(Integer year, Integer month, Long views, MenuTitle menu, ArtworkCategory category, Date createdAt) {
		this.year = year;
		this.month = month;
		this.views = views;
		this.menu = menu;
		this.category = category;
		this.createdAt = createdAt;
	}

	public void updateViews(Long views) {
		this.views = views;
	}
}
