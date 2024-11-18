package studio.studioeye.domain.project.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import studio.studioeye.domain.project.dao.ProjectRepository;
import studio.studioeye.domain.project.domain.Project;
import studio.studioeye.domain.project.domain.ProjectImage;
import studio.studioeye.domain.project.dto.request.CreateProjectServiceRequestDto;
import studio.studioeye.domain.project.dto.request.UpdatePostingStatusDto;
import studio.studioeye.domain.project.dto.request.UpdateProjectServiceRequestDto;
import studio.studioeye.domain.project.dto.request.UpdateProjectTypeDto;
import studio.studioeye.domain.recruitment.domain.Recruitment;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;
import studio.studioeye.infrastructure.s3.S3Adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @InjectMocks
    private ProjectService projectService;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private S3Adapter s3Adapter;

    // Mock MultipartFile 생성
    MockMultipartFile mockFile = new MockMultipartFile(
            "file",
            "testImage.jpg",
            "image/jpeg",
            "Test Image Content".getBytes()
    );

    @Test
    @DisplayName("Project 생성 성공 테스트")
    public void createProjectSuccess() throws IOException {
        // given
        CreateProjectServiceRequestDto requestDto = new CreateProjectServiceRequestDto(
                "Test Department",
                "Entertainment",
                "Test Name",
                "Test Client",
                "2024-01-01",
                "Test Link",
                "Test Overview",
                "main",
                true
        );

        // List<MultipartFile>로 변환
        List<MultipartFile> projectImages = List.of(mockFile);

        Project mockProject = Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .build();

        // stub
        // Mock S3 upload 동작 설정
        when(s3Adapter.uploadFile(any(MultipartFile.class)))
                .thenReturn(ApiResponse.ok("프로젝트를 성공적으로 등록하였습니다.", "http://example.com/testImage.jpg"));
        when(projectRepository.save(any(Project.class))).thenReturn(mockProject);

        // when
        ApiResponse<Project> response = projectService.createProject(requestDto, mockFile, mockFile, projectImages);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getData());
        assertEquals("프로젝트를 성공적으로 등록하였습니다.", response.getMessage());
        Mockito.verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    @DisplayName("Project 생성 실패 테스트 - 이미지 업로드 실패")
    public void createProjectFail() throws IOException {
        // given
        CreateProjectServiceRequestDto requestDto = new CreateProjectServiceRequestDto(
                "Test Department",
                "Entertainment",
                "Test Name",
                "Test Client",
                "2024-01-01",
                "Test Link",
                "Test Overview",
                "main",
                true
        );

        // stub - S3 upload 실패
        when(s3Adapter.uploadFile(any(MultipartFile.class))).thenReturn(ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT));

        // when
        List<MultipartFile> projectImages = List.of(mockFile);
        ApiResponse<Project> response = projectService.createProject(requestDto, mockFile, mockFile, projectImages);

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.ERROR_S3_UPDATE_OBJECT.getStatus(), response.getStatus()); // 적절한 상태 코드 수정
        assertEquals(ErrorCode.ERROR_S3_UPDATE_OBJECT.getMessage(), response.getMessage()); // 예외 메시지 수정
        Mockito.verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("Project 수정 성공 테스트")
    void updateProjectSuccess() throws IOException {
        // given
        Long id = 1L;
        UpdateProjectServiceRequestDto requestDto = new UpdateProjectServiceRequestDto(
                id, "Updated Department", "Entertainment", "Updated Name", "Updated Client", "2024-01-02", "Updated Link", "Updated Overview", "main", true);

        // Mock existing images as MultipartFile
        List<MultipartFile> existingImages = List.of(mockFile);

        Project mockProject = Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .build();

        List<ProjectImage> mockProjectImages = new ArrayList<>();
        mockProjectImages.add(ProjectImage.builder()
                .project(mockProject)
                .fileName(mockProject.getName())
                .imageUrlList(mockProject.getMainImg())
                .build());

        mockProject.setProjectImages(mockProjectImages);

        // stub
        when(projectRepository.findById(requestDto.projectId())).thenReturn(Optional.of(mockProject));
        when(s3Adapter.deleteFile(any(String.class))).thenReturn(
                ApiResponse.ok("S3 버킷에서 이미지를 성공적으로 삭제하였습니다.", "http://example.com/testImage.jpg"));
        when(s3Adapter.uploadFile(any(MultipartFile.class)))
                .thenReturn(ApiResponse.ok("S3 버킷에 이미지 업로드를 성공하였습니다.", "Updated Test ImageUrl"));
        when(projectRepository.save(any(Project.class))).thenReturn(mockProject);

        // when
        ApiResponse<Project> response = projectService.updateProject(requestDto, mockFile, mockFile, existingImages);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("프로젝트를 성공적으로 수정했습니다.", response.getMessage());
        assertEquals("Updated Test ImageUrl", mockProject.getMainImg());
        Mockito.verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    @DisplayName("Project 수정 실패 테스트 - 유효하지 않은 ID")
    void updateProjectFail_invalidID() throws IOException {
        // given
        Long id = 1L;
        UpdateProjectServiceRequestDto requestDto = new UpdateProjectServiceRequestDto(
                id, "Updated Department", "Entertainment", "Updated Name", "Updated Client", "2024-01-02", "Updated Link", "Updated Overview", "main", true);

        // stub
        when(projectRepository.findById(requestDto.projectId())).thenReturn(Optional.empty());

        // when
        ApiResponse<Project> response = projectService.updateProject(requestDto, mockFile, mockFile, List.of(mockFile));

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.INVALID_PROJECT_ID.getStatus(), response.getStatus());
        assertEquals(ErrorCode.INVALID_PROJECT_ID.getMessage(), response.getMessage());
        Mockito.verify(projectRepository, never()).save(any(Project.class));
    }


    @Test
    @DisplayName("프로젝트 게시 상태 수정 성공 테스트")
    void UpdatePostingStatusSuccess() {
        Long projectId = 1L;
        UpdatePostingStatusDto dto = new UpdatePostingStatusDto(projectId, true);

        Project mockProject = Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(mockProject));

        ApiResponse<Project> response = projectService.updatePostingStatus(dto);

        assertEquals("프로젝트 게시 여부를 성공적으로 변경하였습니다.", response.getMessage());
        assertTrue(mockProject.getIsPosted()); // 게시 상태가 true로 변경되었는지 확인
    }

    @Test
    @DisplayName("프로젝트 게시 상태 수정 실패 테스트 - 유효하지 않은 ID")
    void UpdatePostingStatusFail() {
        UpdatePostingStatusDto dto = new UpdatePostingStatusDto(999L, true); // 유효하지 않은 ID

        when(projectRepository.findById(dto.projectId())).thenReturn(Optional.empty());

        ApiResponse<Project> response = projectService.updatePostingStatus(dto);

        assertEquals(ErrorCode.INVALID_PROJECT_ID.getStatus(), response.getStatus());
    }

    @Test
    @DisplayName("프로젝트 타입 수정 성공 테스트 - 기존 타입이 top인 경우")
    void UpdateProjectTypeSuccess_topType() {
        Long projectId = 1L;
        String newType = "main"; // 변경할 타입
        UpdateProjectTypeDto dto = new UpdateProjectTypeDto(projectId, newType);
        Project mockProject = Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("top")
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(mockProject));
        when(projectRepository.findByProjectType(newType)).thenReturn(new ArrayList<>());

        ApiResponse<Project> response = projectService.updateProjectType(dto);

        assertEquals("프로젝트 타입을 성공적으로 변경하였습니다.", response.getMessage());
        assertEquals(newType, mockProject.getProjectType()); // 타입이 변경되었는지 확인
    }

    @Test
    @DisplayName("프로젝트 타입 수정 성공 테스트 - 기존 타입이 main인 경우")
    void UpdateProjectTypeSuccess_mainType() {
        Long projectId = 1L;
        String newType = "main"; // 변경할 타입
        UpdateProjectTypeDto dto = new UpdateProjectTypeDto(projectId, newType);
        Project mockProject = Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(mockProject));

        ApiResponse<Project> response = projectService.updateProjectType(dto);

        assertEquals("프로젝트 타입을 성공적으로 변경하였습니다.", response.getMessage());
        assertEquals(newType, mockProject.getProjectType()); // 타입이 변경되었는지 확인
    }

    @Test
    @DisplayName("프로젝트 타입 수정 성공 테스트 - 기존 타입이 other인 경우")
    void UpdateProjectTypeSuccess_otherType() {
        Long projectId = 1L;
        String newType = "main"; // 변경할 타입
        UpdateProjectTypeDto dto = new UpdateProjectTypeDto(projectId, newType);
        Project mockProject = Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("other")
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(mockProject));
        when(projectRepository.findByProjectType(newType)).thenReturn(new ArrayList<>());

        ApiResponse<Project> response = projectService.updateProjectType(dto);

        assertEquals("프로젝트 타입을 성공적으로 변경하였습니다.", response.getMessage());
        assertEquals(newType, mockProject.getProjectType()); // 타입이 변경되었는지 확인
    }

//
//    @Test
//    @DisplayName("프로젝트 타입 수정 실패 - 유효하지 않은 ID")
//    void UpdateProjectTypeFail() {
//        UpdateProjectTypeDto dto = new UpdateProjectTypeDto(999L, "top"); // 유효하지 않은 ID
//
//        when(projectRepository.findById(dto.projectId())).thenReturn(Optional.empty());
//
//        ApiResponse<Project> response = projectService.updateProjectType(dto);
//
//        assertEquals(ErrorCode.INVALID_PROJECT_ID.getStatus(), response.getStatus());
//    }
//
//    @Test
//    @DisplayName("프로젝트 삭제 성공")
//    void DeleteProjectSuccess() {
//        Long projectId = 1L;
//        Project project = new Project("Test Department", "Entertainment", "Test Name", "Test Client",
//                "2024-01-01", "Test Link", "Test Overview", mockFile.getName(), null, 0, 0, "main", true);
//
//        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
//
//        ApiResponse<String> response = projectService.deleteProject(projectId);
//
//        assertEquals("프로젝트를 성공적으로 삭제했습니다.", response.getMessage());
//        verify(projectRepository).delete(project);
//    }
//
//    @Test
//    @DisplayName("프로젝트 삭제 실패 - 유효하지 않은 ID")
//    void DeleteProjectFail() {
//        Long projectId = 999L; // 유효하지 않은 ID
//
//        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());
//
//        ApiResponse<String> response = projectService.deleteProject(projectId);
//
//        assertEquals(ErrorCode.INVALID_PROJECT_ID.getStatus(), response.getStatus());
//    }
//
//    @Test
//    @DisplayName("프로젝트 전체 조회 성공")
//    void RetrieveAllArtworkProjectSuccess() {
//        List<Project> projects = new ArrayList<>();
//        projects.add(new Project("Test Department", "Entertainment", "Test Name", "Test Client",
//                "2024-01-01", "Test Link", "Test Overview", mockFile.getName(), null, 0, 0, "main", true));
//
//        when(projectRepository.findAllWithImagesAndOrderBySequenceAsc()).thenReturn(projects);
//
//        ApiResponse<List<Project>> response = projectService.retrieveAllArtworkProject();
//
//        assertEquals("프로젝트 목록을 성공적으로 조회했습니다.", response.getMessage());
//        assertEquals(projects, response.getData()); // 프로젝트 목록이 반환되었는지 확인
//    }
//
//    @Test
//    @DisplayName("프로젝트 전체 조회 실패 - 프로젝트가 없는 경우")
//    void RetrieveAllArtworkProjectFail() {
//        when(projectRepository.findAllWithImagesAndOrderBySequenceAsc()).thenReturn(new ArrayList<>());
//
//        ApiResponse<List<Project>> response = projectService.retrieveAllArtworkProject();
//
//        assertEquals("프로젝트가 존재하지 않습니다.", response.getMessage());
//    }
//
//    @Test
//    @DisplayName("메인 프로젝트 전체 조회 성공")
//    void RetrieveAllMainProjectSuccess() {
//        List<Project> projects = new ArrayList<>();
//        projects.add(new Project("Test Department", "Entertainment", "Test Name", "Test Client",
//                "2024-01-01", "Test Link", "Test Overview", mockFile.getName(), null, 0, 0, "main", true));
//        List<Project> topProjects = new ArrayList<>();
//        topProjects.add(new Project("Test Department", "Entertainment", "Test Name", "Test Client",
//                "2024-01-01", "Test Link", "Test Overview", mockFile.getName(), null, 0, 0, "main", true));
//
//        when(projectRepository.findAllWithImagesAndOrderByMainSequenceAsc()).thenReturn(projects);
//        when(projectRepository.findByProjectType("top")).thenReturn(topProjects);
//
//        ApiResponse<List<Project>> response = projectService.retrieveAllMainProject();
//
//        assertEquals("프로젝트 목록을 성공적으로 조회했습니다.", response.getMessage());
//        assertEquals(1 + projects.size(), response.getData().size());
//    }
//
//    @Test
//    @DisplayName("메인 프로젝트 전체 조회 실패 - 프로젝트가 없는 경우")
//    void RetrieveAllMainProjectFail() {
//        when(projectRepository.findAllWithImagesAndOrderByMainSequenceAsc()).thenReturn(new ArrayList<>());
//
//        ApiResponse<List<Project>> response = projectService.retrieveAllMainProject();
//
//        assertEquals("프로젝트가 존재하지 않습니다.", response.getMessage());
//    }
//
//    @Test
//    @DisplayName("단일 프로젝트 조회 성공")
//    void RetrieveProjectSuccess() {
//        Long projectId = 1L;
//        Project project = new Project("Test Department", "Entertainment", "Test Name", "Test Client",
//                "2024-01-01", "Test Link", "Test Overview", mockFile.getName(), null, 0, 0, "main", true);
//
//        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
//
//        ApiResponse<Project> response = projectService.retrieveProject(projectId);
//
//        assertEquals("프로젝트를 성공적으로 조회했습니다.", response.getMessage());
//        assertEquals(project, response.getData());
//    }
//
//    @Test
//    @DisplayName("단일 프로젝트 조회 실패 - 유효하지 않은 ID")
//    void RetrieveProjectFail() {
//        Long projectId = 999L; // 유효하지 않은 ID
//
//        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());
//
//        ApiResponse<Project> response = projectService.retrieveProject(projectId);
//
//        assertEquals(ErrorCode.INVALID_PROJECT_ID.getStatus(), response.getStatus());
//    }
//
}
