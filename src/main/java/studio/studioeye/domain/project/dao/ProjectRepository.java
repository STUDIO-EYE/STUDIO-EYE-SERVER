package studio.studioeye.domain.project.dao;

import studio.studioeye.domain.project.domain.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.projectImages")
    List<Project> findAllWithImages();

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.projectImages")
    Page<Project> findAll(Pageable pageable);

    List<Project> findByProjectType(String projectType);

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.projectImages ORDER BY p.sequence ASC")
    List<Project> findAllWithImagesAndOrderBySequenceAsc();

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.projectImages WHERE p.projectType = 'main' ORDER BY p.mainSequence ASC")
    List<Project> findAllWithImagesAndOrderByMainSequenceAsc();

    List<Project> findAllBySequenceGreaterThan(Integer sequence);

    List<Project> findAllByMainSequenceGreaterThanAndMainSequenceNot(Integer mainSequence, Integer notMainSequence);

    Integer countByProjectType(String projectType);
}