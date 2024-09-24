package studio.studioeye.domain.project.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @JsonBackReference
    private Project project;

    private String imageUrlList;

    private String fileName;

    @Builder
    public ProjectImage(Project project, String imageUrlList, String fileName) {
        this.project = project;
        this.imageUrlList = imageUrlList;
        this.fileName = fileName;
    }
}