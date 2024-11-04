package studio.studioeye.domain.project.dto.request;

import studio.studioeye.domain.project.domain.Project;
import studio.studioeye.domain.project.domain.ProjectImage;

import java.util.List;

public record CreateProjectServiceRequestDto(
	String department,
	String category,
	String name,
	String client,
	String date,
	String link,
	String overView,
	String projectType,
	Boolean isPosted
) {
	public Project toEntity(String mainImg, String mainImgFileName, String responsiveMainImg, String responsiveMainImgFileName, List<ProjectImage> projectImages, long projectCount, Integer mainSequence) {
		return Project.builder()
				.department(department)
				.category(category)
				.name(name)
				.client(client)
				.date(date)
				.link(link)
				.overView(overView)
				.mainImg(mainImg)
				.mainImgFileName(mainImgFileName)
				.responsiveMainImg(responsiveMainImg)
				.responsiveMainImgFileName(responsiveMainImgFileName)
				.projectImages(projectImages)
				.sequence((int) (projectCount + 1))
				.mainSequence(mainSequence)
				.projectType(projectType)
				.isPosted(isPosted)
				.build();
	}
}
