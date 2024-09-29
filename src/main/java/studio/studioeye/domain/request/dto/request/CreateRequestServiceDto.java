package studio.studioeye.domain.request.dto.request;

import studio.studioeye.domain.request.domain.Answer;
import studio.studioeye.domain.request.domain.Request;
import studio.studioeye.domain.request.domain.State;

import java.util.Date;
import java.util.List;

public record CreateRequestServiceDto(
	 String category,
	 String projectName,
	 String clientName,
	 String organization,
	 String contact,
	 String email,
	 String position,
	 String description
) {
	public Request toEntity(List<String> fileUrlList, List<Answer> answers, Integer year, Integer month, State state,
							Date date) {
		return Request.builder()
				.category(category)
				.projectName(projectName)
				.clientName(clientName)
				.organization(organization)
				.contact(contact)
				.email(email)
				.position(position)
				.description(description)
				.answers(answers)
				.year(year)
				.month(month)
				.fileUrlList(fileUrlList)
				.state(state)
				.createdAt(date)
				.build();
	}
}
