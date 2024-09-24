package studio.studioeye.domain.project.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import studio.studioeye.domain.project.dto.request.UpdateProjectServiceRequestDto;

import java.util.LinkedList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project {
	private static final String OTHERS_PROJECT_TYPE = "others";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String department;

	private String category;

	private String name;

	private String client;

	private String date;

	private String link;

	private String overView;

	private String projectType;

	private Boolean isPosted;

	private String mainImg;

	private Integer sequence;

	private Integer mainSequence;

	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<ProjectImage> projectImages = new LinkedList<>();

	@Builder
	public Project(String department, String category, String name, String client, String date, String link,
				   String overView, String mainImg, List<ProjectImage> projectImages, Integer sequence,
				   Integer mainSequence, String projectType, Boolean isPosted) {
		this.department = department;
		this.category = category;
		this.name = name;
		this.client = client;
		this.date = date;
		this.link = link;
		this.overView = overView;
		this.mainImg = mainImg;
		this.projectImages = projectImages;
		this.isPosted = isPosted;
		this.projectType = projectType;
		this.sequence = sequence;
		this.mainSequence = mainSequence;
	}

	public Project update(UpdateProjectServiceRequestDto dto) {
		this.department = dto.department();
		this.category = dto.category();
		this.name = dto.name();
		this.client = dto.client();
		this.date = dto.date();
		this.link = dto.link();
		this.overView = dto.overView();
		this.projectType = dto.projectType();
		this.isPosted = dto.isPosted();
		return this;
	}

	public void updateSequence(Integer sequence) {
		this.sequence = sequence;
	}

	public void updateMainSequence(Integer mainSequence) {
		this.mainSequence = mainSequence;
	}

	public Project updatePostingStatus(Boolean isPosted) {
		this.isPosted = isPosted;
		return this;
	}

	public Project updateProjectType(String projectType) {
		this.projectType = projectType;
		return this;
	}

}
