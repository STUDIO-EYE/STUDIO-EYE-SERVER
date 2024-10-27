package studio.studioeye.domain.project.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import studio.studioeye.domain.project.application.ProjectService;
import studio.studioeye.domain.project.dao.ProjectRepository;
import studio.studioeye.domain.project.domain.Project;
import studio.studioeye.domain.project.dto.request.CreateProjectServiceRequestDto;
import studio.studioeye.domain.project.dto.request.UpdatePostingStatusDto;
import studio.studioeye.domain.project.dto.request.UpdateProjectServiceRequestDto;
import studio.studioeye.domain.project.dto.request.UpdateProjectTypeDto;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;
import studio.studioeye.infrastructure.s3.S3Adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
    @DisplayName("Project 생성 성공")
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

        // Mock S3 upload 동작 설정
        when(s3Adapter.uploadFile(any(MultipartFile.class)))
                .thenReturn(ApiResponse.ok("프로젝트를 성공적으로 등록하였습니다.", "http://example.com/testImage.jpg"));

        // List<MultipartFile>로 변환
        List<MultipartFile> projectImages = List.of(mockFile);

        // when
        ApiResponse<Project> response = projectService.createProject(requestDto, mockFile, projectImages);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("프로젝트를 성공적으로 등록하였습니다.", response.getMessage());
        assertNotNull(response.getData());
    }

    @Test
    @DisplayName("Project 생성 실패 - 이미지 업로드 실패")
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
        when(s3Adapter.uploadFile(any(MultipartFile.class)))
                .thenReturn(null); // null을 반환하여 예외를 유발하도록 수정

        // when
        List<MultipartFile> projectImages = List.of(mockFile);
        ApiResponse<Project> response = projectService.createProject(requestDto, mockFile, projectImages);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus()); // 적절한 상태 코드 수정
        assertEquals("이미지 업로드 중 오류가 발생했습니다.", response.getMessage()); // 예외 메시지 수정
    }

    @Test
    @DisplayName("Project 수정 성공 테스트")
    void updateProjectSuccess() throws IOException {
        // given
        Long id = 1L;
        UpdateProjectServiceRequestDto requestDto = new UpdateProjectServiceRequestDto(
                id, "Updated Department", "Entertainment", "Updated Name", "Updated Client", "2024-01-02", "Updated Link", "Updated Overview", "main", true);

        Project savedProject = new Project("Test Department", "Entertainment", "Test Name", "Test Client",
                "2024-01-01", "Test Link", "Test Overview", mockFile.getName(), null, 0, 0, "main", true);

        // Mock existing images as MultipartFile
        List<MultipartFile> existingImages = List.of(mockFile);

        // stub
        when(projectRepository.findById(requestDto.projectId())).thenReturn(Optional.of(savedProject));
        when(s3Adapter.deleteFile(savedProject.getMainImg())).thenReturn(ApiResponse.ok("S3에서 파일 삭제 성공"));
        when(s3Adapter.uploadFile(any(MultipartFile.class)))
                .thenReturn(ApiResponse.ok("S3에 이미지 업로드 성공", "Updated Test ImageUrl"));
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        // when
        ApiResponse<Project> response = projectService.updateProject(requestDto, mockFile, existingImages);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("프로젝트를 성공적으로 수정했습니다.", response.getMessage());
        assertEquals("Updated Test ImageUrl", savedProject.getMainImg());
    }

    @Test
    @DisplayName("Project 수정 실패 - 유효하지 않은 ID")
    void updateProjectFail() throws IOException {
        // given
        Long id = 1L;
        UpdateProjectServiceRequestDto requestDto = new UpdateProjectServiceRequestDto(
                id, "Updated Department", "Entertainment", "Updated Name", "Updated Client", "2024-01-02", "Updated Link", "Updated Overview", "main", true);

        // stub
        when(projectRepository.findById(requestDto.projectId())).thenReturn(Optional.empty());

        // when
        ApiResponse<Project> response = projectService.updateProject(requestDto, mockFile, List.of(mockFile));

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals("유효하지 않은 project 식별자입니다.", response.getMessage());
    }


    @Test
    @DisplayName("프로젝트 게시 상태 수정 성공")
    void UpdatePostingStatusSuccess() {
        Long projectId = 1L;
        UpdatePostingStatusDto dto = new UpdatePostingStatusDto(projectId, true);
        Project project = new Project("Test Department", "Entertainment", "Test Name", "Test Client",
                "2024-01-01", "Test Link", "Test Overview", mockFile.getName(), null, 0, 0, "main", true);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        ApiResponse<Project> response = projectService.updatePostingStatus(dto);

        assertEquals("프로젝트 게시 여부를 성공적으로 변경하였습니다.", response.getMessage());
        assertTrue(project.getIsPosted()); // 게시 상태가 true로 변경되었는지 확인
    }

    @Test
    @DisplayName("프로젝트 게시 상태 수정 실패 - 유효하지 않은 ID")
    void UpdatePostingStatusFail() {
        UpdatePostingStatusDto dto = new UpdatePostingStatusDto(999L, true); // 유효하지 않은 ID

        when(projectRepository.findById(dto.projectId())).thenReturn(Optional.empty());

        ApiResponse<Project> response = projectService.updatePostingStatus(dto);

        assertEquals(ErrorCode.INVALID_PROJECT_ID.getStatus(), response.getStatus());
    }

    @Test
    @DisplayName("프로젝트 타입 수정 성공")
    void UpdateProjectTypeSuccess() {
        Long projectId = 1L;
        String newType = "main"; // 변경할 타입
        UpdateProjectTypeDto dto = new UpdateProjectTypeDto(projectId, newType);
        Project project = new Project("Test Department", "Entertainment", "Test Name", "Test Client",
                "2024-01-01", "Test Link", "Test Overview", mockFile.getName(), null, 0, 0, "main", true);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.findByProjectType(newType)).thenReturn(new ArrayList<>());

        ApiResponse<Project> response = projectService.updateProjectType(dto);

        assertEquals("프로젝트 타입을 성공적으로 변경하였습니다.", response.getMessage());
        assertEquals(newType, project.getProjectType()); // 타입이 변경되었는지 확인
    }

    @Test
    @DisplayName("프로젝트 타입 수정 실패 - 유효하지 않은 ID")
    void UpdateProjectTypeFail() {
        UpdateProjectTypeDto dto = new UpdateProjectTypeDto(999L, "top"); // 유효하지 않은 ID

        when(projectRepository.findById(dto.projectId())).thenReturn(Optional.empty());

        ApiResponse<Project> response = projectService.updateProjectType(dto);

        assertEquals(ErrorCode.INVALID_PROJECT_ID.getStatus(), response.getStatus());
    }

    @Test
    @DisplayName("프로젝트 삭제 성공")
    void DeleteProjectSuccess() {
        Long projectId = 1L;
        Project project = new Project("Test Department", "Entertainment", "Test Name", "Test Client",
                "2024-01-01", "Test Link", "Test Overview", mockFile.getName(), null, 0, 0, "main", true);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        ApiResponse<String> response = projectService.deleteProject(projectId);

        assertEquals("프로젝트를 성공적으로 삭제했습니다.", response.getMessage());
        verify(projectRepository).delete(project);
    }

    @Test
    @DisplayName("프로젝트 삭제 실패 - 유효하지 않은 ID")
    void DeleteProjectFail() {
        Long projectId = 999L; // 유효하지 않은 ID

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        ApiResponse<String> response = projectService.deleteProject(projectId);

        assertEquals(ErrorCode.INVALID_PROJECT_ID.getStatus(), response.getStatus());
    }

}
