package studio.studioeye.domain.request.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Request {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String projectName;

	private String category;

	private String clientName;

	private String organization;

	private String contact;

	private String email;

	private String position;

	@Column(columnDefinition = "TEXT")
	private String description;

	@OneToMany(mappedBy = "request", cascade = CascadeType.REMOVE)
	private List<Answer> answers = new ArrayList<>();

	@Column(name = "year_value")
	private Integer year;

	@Column(name = "month_value")
	private Integer month;

	@Enumerated(EnumType.STRING)
	private State state;

	@ElementCollection
	private List<String> fileUrlList = new LinkedList<>();

	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

	@Builder
	public Request(Long id, String category, String projectName, String clientName, String organization, String contact, String email,
				   String position, List<String> fileUrlList, String description, List<Answer> answers,
				   Integer year, Integer month, State state, Date createdAt) {
		this.id = id;
		this.category = category;
		this.projectName = projectName;
		this.clientName = clientName;
		this.organization = organization;
		this.contact = contact;
		this.email = email;
		this.position = position;
		this.fileUrlList = fileUrlList;
		this.description = description;
		this.answers = answers;
		this.year = year;
		this.month = month;
		this.state = state;
		this.createdAt = createdAt;
	}

	public void updateAnswer(List<Answer> answers) {
		this.answers = answers;
	}
	public void updateState(State state) {
		this.state = state;
	}
}
