package studio.studioeye.domain.project.dto.request;

public record UpdateProjectServiceRequestDto(
	Long projectId,
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
}
