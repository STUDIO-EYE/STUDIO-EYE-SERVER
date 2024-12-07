package studio.studioeye.domain.project.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import studio.studioeye.domain.project.dao.ProjectRepository;
import studio.studioeye.domain.project.domain.Project;
import studio.studioeye.domain.project.domain.ProjectImage;
import studio.studioeye.domain.project.dto.request.*;
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
class ProjectServiceTest {

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

    @ParameterizedTest
    @CsvSource({
            "main, main",
            "top, top",
            "others, others"
    })
    @DisplayName("Project 생성 성공 테스트")
    void createProjectSuccess(String inputType, String expectedType) throws IOException {
        // given
        CreateProjectServiceRequestDto requestDto = new CreateProjectServiceRequestDto(
                "Test Department",
                "Entertainment",
                "Test Name",
                "Test Client",
                "2024-01-01",
                "Test Link",
                "Test Overview",
                inputType,
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
                .projectType(expectedType)
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
    void createProjectFail() throws IOException {
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
    @DisplayName("Project 생성 실패 테스트 - 유효하지 않은 projectType인 경우")
    void createProjectFail_invalidProjectType() throws IOException {
        // given
        CreateProjectServiceRequestDto requestDto = new CreateProjectServiceRequestDto(
                "Test Department",
                "Entertainment",
                "Test Name",
                "Test Client",
                "2024-01-01",
                "Test Link",
                "Test Overview",
                "invalidType",
                true
        );

        // stub - S3 upload 실패
        when(s3Adapter.uploadFile(any(MultipartFile.class)))
                .thenReturn(ApiResponse.ok("프로젝트를 성공적으로 등록하였습니다.", "http://example.com/testImage.jpg"));

        // when
        List<MultipartFile> projectImages = List.of(mockFile);
        ApiResponse<Project> response = projectService.createProject(requestDto, mockFile, mockFile, projectImages);

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.INVALID_PROJECT_TYPE.getStatus(), response.getStatus()); // 적절한 상태 코드 수정
        assertEquals(ErrorCode.INVALID_PROJECT_TYPE.getMessage(), response.getMessage()); // 예외 메시지 수정
        Mockito.verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("Project 생성 실패 테스트 - TOP 프로젝트가 이미 존재하는 경우")
    void createProjectFail_alreadyExistedTop() throws IOException {
        // given
        CreateProjectServiceRequestDto requestDto = new CreateProjectServiceRequestDto(
                "Test Department",
                "Entertainment",
                "Test Name",
                "Test Client",
                "2024-01-01",
                "Test Link",
                "Test Overview",
                "top",
                true
        );

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

        // stub - S3 upload 실패
        when(s3Adapter.uploadFile(any(MultipartFile.class)))
                .thenReturn(ApiResponse.ok("프로젝트를 성공적으로 등록하였습니다.", "http://example.com/testImage.jpg"));
        when(projectRepository.findByProjectType(requestDto.projectType())).thenReturn(List.of(mockProject));

        // when
        List<MultipartFile> projectImages = List.of(mockFile);
        ApiResponse<Project> response = projectService.createProject(requestDto, mockFile, mockFile, projectImages);

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.TOP_PROJECT_ALREADY_EXISTS.getStatus(), response.getStatus()); // 적절한 상태 코드 수정
        assertEquals(ErrorCode.TOP_PROJECT_ALREADY_EXISTS.getMessage(), response.getMessage()); // 예외 메시지 수정
        Mockito.verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("Project 생성 실패 테스트 - isPosted가 false인 TOP 프로젝트의 경우")
    void createProjectFail_isPostedFalseTop() throws IOException {
        // given
        CreateProjectServiceRequestDto requestDto = new CreateProjectServiceRequestDto(
                "Test Department",
                "Entertainment",
                "Test Name",
                "Test Client",
                "2024-01-01",
                "Test Link",
                "Test Overview",
                "top",
                false
        );


        // stub - S3 upload 실패
        when(s3Adapter.uploadFile(any(MultipartFile.class)))
                .thenReturn(ApiResponse.ok("프로젝트를 성공적으로 등록하였습니다.", "http://example.com/testImage.jpg"));
        when(projectRepository.findByProjectType(requestDto.projectType())).thenReturn(List.of());

        // when
        List<MultipartFile> projectImages = List.of(mockFile);
        ApiResponse<Project> response = projectService.createProject(requestDto, mockFile, mockFile, projectImages);

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.PROJECT_TYPE_AND_IS_POSTED_MISMATCH.getStatus(), response.getStatus()); // 적절한 상태 코드 수정
        assertEquals(ErrorCode.PROJECT_TYPE_AND_IS_POSTED_MISMATCH.getMessage(), response.getMessage()); // 예외 메시지 수정
        Mockito.verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("Project 생성 실패 테스트 - main인 프로젝트가 이미 5개 이상인 경우")
    void createProjectFail_overMainProjectCount() throws IOException {
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


        // stub - S3 upload 실패
        when(s3Adapter.uploadFile(any(MultipartFile.class)))
                .thenReturn(ApiResponse.ok("프로젝트를 성공적으로 등록하였습니다.", "http://example.com/testImage.jpg"));
        when(projectRepository.findByProjectType(requestDto.projectType())).thenReturn(List.of(mockProject, mockProject, mockProject, mockProject, mockProject));

        // when
        List<MultipartFile> projectImages = List.of(mockFile);
        ApiResponse<Project> response = projectService.createProject(requestDto, mockFile, mockFile, projectImages);

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.MAIN_PROJECT_LIMIT_EXCEEDED.getStatus(), response.getStatus()); // 적절한 상태 코드 수정
        assertEquals(ErrorCode.MAIN_PROJECT_LIMIT_EXCEEDED.getMessage(), response.getMessage()); // 예외 메시지 수정
        Mockito.verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("Project 생성 실패 테스트 - main인데 isPosted를 false로 할 경우")
    void createProjectFail_isPostedFalseMain() throws IOException {
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
                false
        );

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


        // stub - S3 upload 실패
        when(s3Adapter.uploadFile(any(MultipartFile.class)))
                .thenReturn(ApiResponse.ok("프로젝트를 성공적으로 등록하였습니다.", "http://example.com/testImage.jpg"));
        when(projectRepository.findByProjectType(requestDto.projectType())).thenReturn(List.of(mockProject, mockProject, mockProject, mockProject));

        // when
        List<MultipartFile> projectImages = List.of(mockFile);
        ApiResponse<Project> response = projectService.createProject(requestDto, mockFile, mockFile, projectImages);

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.PROJECT_TYPE_AND_IS_POSTED_MISMATCH.getStatus(), response.getStatus()); // 적절한 상태 코드 수정
        assertEquals(ErrorCode.PROJECT_TYPE_AND_IS_POSTED_MISMATCH.getMessage(), response.getMessage()); // 예외 메시지 수정
        Mockito.verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("Project 수정 성공 테스트 - top으로 수정하는 경우")
    void updateProjectSuccess_toTopType() throws IOException {
        // given
        Long id = 1L;
        UpdateProjectServiceRequestDto requestDto = new UpdateProjectServiceRequestDto(
                id, "Updated Department", "Entertainment", "Updated Name", "Updated Client", "2024-01-02", "Updated Link", "Updated Overview", "top", true);

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

        List<Project> mockProjectList = new ArrayList<>();
        mockProjectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(1)
                .sequence(0)
                .build());
        mockProjectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(2)
                .sequence(0)
                .build());
        mockProjectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(3)
                .sequence(0)
                .build());
        mockProjectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(4)
                .sequence(0)
                .build());

        // stub
        when(projectRepository.findById(requestDto.projectId())).thenReturn(Optional.of(mockProject));
        when(projectRepository.findAllByMainSequenceGreaterThanAndMainSequenceNot(mockProject.getMainSequence(), 999))
                .thenReturn(mockProjectList);
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
    @DisplayName("Project 수정 성공 테스트 - main으로 수정하는 경우")
    void updateProjectSuccess_toMainType() throws IOException {
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
                .projectType("top")
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
    @DisplayName("Project 수정 성공 테스트 - main에서 main으로 수정하는 경우")
    void updateProjectSuccess_mainToMainType() throws IOException {
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
    @DisplayName("Project 수정 성공 테스트 - others으로 수정하는 경우")
    void updateProjectSuccess_toOthersType() throws IOException {
        // given
        Long id = 1L;
        UpdateProjectServiceRequestDto requestDto = new UpdateProjectServiceRequestDto(
                id, "Updated Department", "Entertainment", "Updated Name", "Updated Client", "2024-01-02", "Updated Link", "Updated Overview", "others", true);

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

        List<Project> mockProjectList = new ArrayList<>();
        mockProjectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(1)
                .sequence(0)
                .build());
        mockProjectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(2)
                .sequence(0)
                .build());
        mockProjectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(3)
                .sequence(0)
                .build());
        mockProjectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(4)
                .sequence(0)
                .build());

        // stub
        when(projectRepository.findById(requestDto.projectId())).thenReturn(Optional.of(mockProject));
        when(projectRepository.findAllByMainSequenceGreaterThanAndMainSequenceNot(mockProject.getMainSequence(), 999))
                .thenReturn(mockProjectList);
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
    @DisplayName("Project 수정 실패 테스트 - mainImgFile이 비어있는 경우")
    void updateProjectFail_emptyMainImgFile() throws IOException {
        // given
        Long id = 1L;
        UpdateProjectServiceRequestDto requestDto = new UpdateProjectServiceRequestDto(
                id, "Updated Department", "Entertainment", "Updated Name", "Updated Client", "2024-01-02", "Updated Link", "Updated Overview", "main", true);

        // when
        ApiResponse<Project> response = projectService.updateProject(requestDto, null, mockFile, List.of(mockFile));

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.NOT_EXIST_IMAGE_FILE.getStatus(), response.getStatus());
        assertEquals(ErrorCode.NOT_EXIST_IMAGE_FILE.getMessage(), response.getMessage());
        Mockito.verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("Project 수정 실패 테스트 - responsiveMainImgFile이 비어있는 경우")
    void updateProjectFail_emptyResponsiveMainImgFile() throws IOException {
        // given
        Long id = 1L;
        UpdateProjectServiceRequestDto requestDto = new UpdateProjectServiceRequestDto(
                id, "Updated Department", "Entertainment", "Updated Name", "Updated Client", "2024-01-02", "Updated Link", "Updated Overview", "main", true);

        // when
        ApiResponse<Project> response = projectService.updateProject(requestDto, mockFile, null, List.of(mockFile));

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.NOT_EXIST_IMAGE_FILE.getStatus(), response.getStatus());
        assertEquals(ErrorCode.NOT_EXIST_IMAGE_FILE.getMessage(), response.getMessage());
        Mockito.verify(projectRepository, never()).save(any(Project.class));
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
    @DisplayName("Project 수정 실패 테스트 - 유효하지 projectType인 경우")
    void updateProjectFail_invalidProjectType() throws IOException {
        // given
        Long id = 1L;
        UpdateProjectServiceRequestDto requestDto = new UpdateProjectServiceRequestDto(
                id, "Updated Department", "Entertainment", "Updated Name", "Updated Client", "2024-01-02", "Updated Link", "Updated Overview", "invalidValue", true);

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


        // stub
        when(projectRepository.findById(requestDto.projectId())).thenReturn(Optional.of(mockProject));

        // when
        ApiResponse<Project> response = projectService.updateProject(requestDto, mockFile, mockFile, List.of(mockFile));

        assertEquals(ErrorCode.INVALID_PROJECT_TYPE.getStatus(), response.getStatus());
        assertEquals(ErrorCode.INVALID_PROJECT_TYPE.getMessage(), response.getMessage());
    }

    @Test
    @DisplayName("Project 수정 실패 테스트 - 이미 top이 존재하는 경우")
    void updateProjectFail_alreadyTopExisted() throws IOException {
        // given
        Long id = 1L;
        UpdateProjectServiceRequestDto requestDto = new UpdateProjectServiceRequestDto(
                id, "Updated Department", "Entertainment", "Updated Name", "Updated Client", "2024-01-02", "Updated Link", "Updated Overview", "top", true);

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

        // 리플렉션으로 ID 설정
        ReflectionTestUtils.setField(mockProject, "id", 0L);

        Project mockProject2 = Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("top")
                .build();

        // 리플렉션으로 ID 설정
        ReflectionTestUtils.setField(mockProject2, "id", 1L);


        // stub
        when(projectRepository.findById(requestDto.projectId())).thenReturn(Optional.of(mockProject2));
        when(projectRepository.findByProjectType(requestDto.projectType())).thenReturn(List.of(mockProject));

        // when
        ApiResponse<Project> response = projectService.updateProject(requestDto, mockFile, mockFile, List.of(mockFile));

        assertEquals(ErrorCode.TOP_PROJECT_ALREADY_EXISTS.getStatus(), response.getStatus());
        assertEquals(ErrorCode.TOP_PROJECT_ALREADY_EXISTS.getMessage(), response.getMessage());
    }

    @Test
    @DisplayName("Project 수정 실패 테스트 - 이미 main이 5개 이상인 경우")
    void updateProjectFail_alreadyMainMaxium() throws IOException {
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
                .projectType("top")
                .build();

        List<ProjectImage> mockProjectImages = new ArrayList<>();
        mockProjectImages.add(ProjectImage.builder()
                .project(mockProject)
                .fileName(mockProject.getName())
                .imageUrlList(mockProject.getMainImg())
                .build());

        mockProject.setProjectImages(mockProjectImages);

        List<Project> mockProjectList = new ArrayList<>();
        mockProjectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(1)
                .sequence(0)
                .build());
        mockProjectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(2)
                .sequence(0)
                .build());
        mockProjectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(3)
                .sequence(0)
                .build());
        mockProjectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(4)
                .sequence(0)
                .build());
        mockProjectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(5)
                .sequence(0)
                .build());

        // stub
        when(projectRepository.findById(requestDto.projectId())).thenReturn(Optional.of(mockProject));
        when(projectRepository.findByProjectType(requestDto.projectType())).thenReturn(mockProjectList);

        // when
        ApiResponse<Project> response = projectService.updateProject(requestDto, mockFile, mockFile, existingImages);

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.MAIN_PROJECT_LIMIT_EXCEEDED.getStatus(), response.getStatus());
        assertEquals(ErrorCode.MAIN_PROJECT_LIMIT_EXCEEDED.getMessage(), response.getMessage());
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
    void UpdatePostingStatusFail_invalidID() {
        UpdatePostingStatusDto dto = new UpdatePostingStatusDto(999L, true); // 유효하지 않은 ID

        when(projectRepository.findById(dto.projectId())).thenReturn(Optional.empty());

        ApiResponse<Project> response = projectService.updatePostingStatus(dto);

        assertEquals(ErrorCode.INVALID_PROJECT_ID.getStatus(), response.getStatus());
    }

    @Test
    @DisplayName("프로젝트 타입 수정 성공 테스트 - top에서 main으로 수정하는 경우")
    void UpdateProjectTypeSuccess_topToMain() {
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
    @DisplayName("프로젝트 타입 수정 성공 테스트 - main에서 top으로 수정하는 경우")
    void UpdateProjectTypeSuccess_mainToTop() {
        Long projectId = 1L;
        String newType = "top"; // 변경할 타입
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
                .mainSequence(0)
                .build();

        List<Project> mockProjectList = new ArrayList<>();
        mockProjectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(1)
                .sequence(0)
                .build());
        mockProjectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(2)
                .sequence(0)
                .build());
        mockProjectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(3)
                .sequence(0)
                .build());
        mockProjectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(4)
                .sequence(0)
                .build());

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(mockProject));
        when(projectRepository.findAllByMainSequenceGreaterThanAndMainSequenceNot(mockProject.getMainSequence(), 999))
                .thenReturn(mockProjectList);

        ApiResponse<Project> response = projectService.updateProjectType(dto);

        assertEquals("프로젝트 타입을 성공적으로 변경하였습니다.", response.getMessage());
        assertEquals(newType, mockProject.getProjectType()); // 타입이 변경되었는지 확인
    }

    @Test
    @DisplayName("프로젝트 타입 수정 성공 테스트 - main에서 others으로 수정하는 경우")
    void UpdateProjectTypeSuccess_MainToOthers() {
        Long projectId = 1L;
        String newType = "others"; // 변경할 타입
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
                .mainSequence(0)
                .build();

        List<Project> mockProjectList = new ArrayList<>();
        mockProjectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(1)
                .sequence(0)
                .build());
        mockProjectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(2)
                .sequence(0)
                .build());
        mockProjectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(3)
                .sequence(0)
                .build());
        mockProjectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(4)
                .sequence(0)
                .build());

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(mockProject));
        when(projectRepository.findAllByMainSequenceGreaterThanAndMainSequenceNot(mockProject.getMainSequence(), 999))
                .thenReturn(mockProjectList);

        ApiResponse<Project> response = projectService.updateProjectType(dto);

        assertEquals("프로젝트 타입을 성공적으로 변경하였습니다.", response.getMessage());
        assertEquals(newType, mockProject.getProjectType()); // 타입이 변경되었는지 확인
    }

    @Test
    @DisplayName("프로젝트 타입 수정 성공 테스트 - main에서 main으로 수정하는 경우")
    void UpdateProjectTypeSuccess_MainToMain() {
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
    @DisplayName("프로젝트 타입 수정 성공 테스트 - others에서 main으로 수정하는 경우")
    void UpdateProjectTypeSuccess_otherToMain() {
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
                .projectType("others")
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(mockProject));
        when(projectRepository.findByProjectType(newType)).thenReturn(new ArrayList<>());

        ApiResponse<Project> response = projectService.updateProjectType(dto);

        assertEquals("프로젝트 타입을 성공적으로 변경하였습니다.", response.getMessage());
        assertEquals(newType, mockProject.getProjectType()); // 타입이 변경되었는지 확인
    }

    @Test
    @DisplayName("프로젝트 타입 수정 실패 테스트 - 유효하지 않은 ID")
    void UpdateProjectTypeFail_invalidID() {
        UpdateProjectTypeDto dto = new UpdateProjectTypeDto(999L, "top"); // 유효하지 않은 ID

        when(projectRepository.findById(dto.projectId())).thenReturn(Optional.empty());

        ApiResponse<Project> response = projectService.updateProjectType(dto);

        assertEquals(ErrorCode.INVALID_PROJECT_ID.getStatus(), response.getStatus());
        assertEquals(ErrorCode.INVALID_PROJECT_ID.getMessage(), response.getMessage());
    }

    @Test
    @DisplayName("프로젝트 타입 수정 실패 테스트 - 유효하지 projectType인 경우")
    void UpdateProjectTypeFail_invalidProjectType() {
        UpdateProjectTypeDto dto = new UpdateProjectTypeDto(1L, "invalidValue");

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

        when(projectRepository.findById(dto.projectId())).thenReturn(Optional.of(mockProject));

        ApiResponse<Project> response = projectService.updateProjectType(dto);

        assertEquals(ErrorCode.INVALID_PROJECT_TYPE.getStatus(), response.getStatus());
        assertEquals(ErrorCode.INVALID_PROJECT_TYPE.getMessage(), response.getMessage());
    }

    @Test
    @DisplayName("프로젝트 타입 수정 실패 테스트 - TOP 프로젝트가 이미 존재하고, 전달된 프로젝트 id가 이미 존재하는 TOP 프로젝트 id와 다른 경우")
    void UpdateProjectTypeFail_alreadyExistedTopAndinvalidTopProjectID() {
        UpdateProjectTypeDto dto = new UpdateProjectTypeDto(1L, "top");

        Project newProject = Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("top")
                .build();
        // 리플렉션으로 ID 설정
        ReflectionTestUtils.setField(newProject, "id", 1L);

        String newType = "top"; // 변경할 타입

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

        // 리플렉션으로 ID 설정
        ReflectionTestUtils.setField(mockProject, "id", 500L);

        when(projectRepository.findById(dto.projectId())).thenReturn(Optional.of(newProject));
        when(projectRepository.findByProjectType(newType)).thenReturn(List.of(mockProject));

        ApiResponse<Project> response = projectService.updateProjectType(dto);

        assertEquals(ErrorCode.TOP_PROJECT_ALREADY_EXISTS.getStatus(), response.getStatus());
        assertEquals(ErrorCode.TOP_PROJECT_ALREADY_EXISTS.getMessage(), response.getMessage());
    }

    @Test
    @DisplayName("프로젝트 타입 수정 실패 테스트 - main인 프로젝트가 이미 5개 이상인 경우")
    void UpdateProjectTypeFail_overMainProjectCount() {
        UpdateProjectTypeDto dto = new UpdateProjectTypeDto(1L, "main");

        Project newProject = Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("top")
                .build();
        String newType = "main"; // 변경할 타입

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

        when(projectRepository.findById(dto.projectId())).thenReturn(Optional.of(newProject));
        when(projectRepository.findByProjectType(newType)).thenReturn(List.of(mockProject, mockProject, mockProject, mockProject, mockProject));

        ApiResponse<Project> response = projectService.updateProjectType(dto);

        assertEquals(ErrorCode.MAIN_PROJECT_LIMIT_EXCEEDED.getStatus(), response.getStatus());
        assertEquals(ErrorCode.MAIN_PROJECT_LIMIT_EXCEEDED.getMessage(), response.getMessage());
    }

    @Test
    @DisplayName("프로젝트 삭제 성공 테스트")
    void DeleteProjectSuccess() {
        Long projectId = 1L;
        Project project = Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainSequence(0)
                .sequence(0)
                .build();

        List<ProjectImage> mockProjectImages = new ArrayList<>();
        mockProjectImages.add(ProjectImage.builder()
                .project(project)
                .fileName(project.getName())
                .imageUrlList(project.getMainImg())
                .build());

        project.setProjectImages(mockProjectImages);

        List<Project> mockProjectList = new ArrayList<>();
        mockProjectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(1)
                .sequence(1)
                .build());
        mockProjectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(2)
                .sequence(2)
                .build());
        mockProjectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(3)
                .sequence(3)
                .build());
        mockProjectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(4)
                .sequence(4)
                .build());

        // stub
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.findAllBySequenceGreaterThan(0))
                .thenReturn(mockProjectList);
        when(projectRepository.findAllByMainSequenceGreaterThanAndMainSequenceNot(project.getMainSequence(), 999))
                .thenReturn(mockProjectList);

        ApiResponse<String> response = projectService.deleteProject(projectId);

        assertEquals("프로젝트를 성공적으로 삭제했습니다.", response.getMessage());
        Mockito.verify(projectRepository, times(1)).delete(project);
    }

    @Test
    @DisplayName("프로젝트 삭제 실패 테스트 - 유효하지 않은 ID")
    void DeleteProjectFail_invalidId() {
        Long projectId = 999L; // 유효하지 않은 ID

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        ApiResponse<String> response = projectService.deleteProject(projectId);

        assertEquals(ErrorCode.INVALID_PROJECT_ID.getStatus(), response.getStatus());
        assertEquals(ErrorCode.INVALID_PROJECT_ID.getMessage(), response.getMessage());
        Mockito.verify(projectRepository, never()).delete(any(Project.class));
    }

    @Test
    @DisplayName("프로젝트 전체 조회 성공 테스트")
    void RetrieveAllArtworkProjectSuccess() {
        List<Project> projects = new ArrayList<>();
        projects.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("top")
                .build());

        when(projectRepository.findAllWithImagesAndOrderBySequenceAsc()).thenReturn(projects);

        ApiResponse<List<Project>> response = projectService.retrieveAllArtworkProject();

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals("프로젝트 목록을 성공적으로 조회했습니다.", response.getMessage());
        assertEquals(projects, response.getData()); // 프로젝트 목록이 반환되었는지 확인
        Mockito.verify(projectRepository, times(1)).findAllWithImagesAndOrderBySequenceAsc();
    }

    @Test
    @DisplayName("프로젝트 전체 조회 실패 테스트 - 프로젝트가 없는 경우")
    void RetrieveAllArtworkProjectFail() {
        when(projectRepository.findAllWithImagesAndOrderBySequenceAsc()).thenReturn(new ArrayList<>());

        ApiResponse<List<Project>> response = projectService.retrieveAllArtworkProject();

        assertNotNull(response);
        assertNull(response.getData());
        assertEquals("프로젝트가 존재하지 않습니다.", response.getMessage());
        Mockito.verify(projectRepository, times(1)).findAllWithImagesAndOrderBySequenceAsc();
    }

    @Test
    @DisplayName("메인 프로젝트 전체 조회 성공 테스트")
    void RetrieveAllMainProjectSuccess() {
        List<Project> projects = new ArrayList<>();
        projects.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .build());
        List<Project> topProjects = new ArrayList<>();
        topProjects.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .build());

        when(projectRepository.findAllWithImagesAndOrderByMainSequenceAsc()).thenReturn(projects);
        when(projectRepository.findByProjectType("top")).thenReturn(topProjects);

        ApiResponse<List<Project>> response = projectService.retrieveAllMainProject();

        assertEquals("프로젝트 목록을 성공적으로 조회했습니다.", response.getMessage());
        assertEquals(1 + projects.size(), response.getData().size());
    }

    @Test
    @DisplayName("메인 프로젝트 전체 조회 실패 테스트 - 프로젝트가 없는 경우")
    void RetrieveAllMainProjectFail() {
        when(projectRepository.findAllWithImagesAndOrderByMainSequenceAsc()).thenReturn(new ArrayList<>());

        ApiResponse<List<Project>> response = projectService.retrieveAllMainProject();

        assertEquals("프로젝트가 존재하지 않습니다.", response.getMessage());
    }

    @Test
    @DisplayName("단일 프로젝트 조회 성공 테스트")
    void RetrieveProjectSuccess() {
        Long projectId = 1L;
        Project project = Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(0)
                .sequence(0)
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        ApiResponse<Project> response = projectService.retrieveProject(projectId);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("프로젝트를 성공적으로 조회했습니다.", response.getMessage());
        assertEquals(project, response.getData());
        Mockito.verify(projectRepository, times(1)).findById(any(Long.class));
    }

    @Test
    @DisplayName("단일 프로젝트 조회 실패 테스트 - 유효하지 않은 ID")
    void RetrieveProjectFail() {
        Long projectId = 999L; // 유효하지 않은 ID

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        ApiResponse<Project> response = projectService.retrieveProject(projectId);

        assertEquals(ErrorCode.INVALID_PROJECT_ID.getStatus(), response.getStatus());
        assertEquals(ErrorCode.INVALID_PROJECT_ID.getMessage(), response.getMessage());
        Mockito.verify(projectRepository, times(1)).findById(any(Long.class));
    }

    @Test
    @DisplayName("Artwork Page 프로젝트 순서 변경 성공 테스트")
    void changeSequenceProjectSuccess() {
        // given
        Project project1 = Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(1)
                .sequence(0)
                .build();
        Project project2 = Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(1)
                .sequence(0)
                .build();
        Project project3 = Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(1)
                .sequence(0)
                .build();

        List<ChangeSequenceProjectReq> changeSequenceProjectReqList = new ArrayList<>();
        ChangeSequenceProjectReq req1 = new ChangeSequenceProjectReq();
        req1.setProjectId(0L);
        req1.setSequence(1);
        changeSequenceProjectReqList.add(req1);
        ChangeSequenceProjectReq req2 = new ChangeSequenceProjectReq();
        req2.setProjectId(1L);
        req2.setSequence(2);
        changeSequenceProjectReqList.add(req2);
        ChangeSequenceProjectReq req3 = new ChangeSequenceProjectReq();
        req3.setProjectId(2L);
        req3.setSequence(0);
        changeSequenceProjectReqList.add(req3);

        // stub
        when(projectRepository.findById(req1.getProjectId())).thenReturn(Optional.of(project1));
        when(projectRepository.findById(req2.getProjectId())).thenReturn(Optional.of(project2));
        when(projectRepository.findById(req3.getProjectId())).thenReturn(Optional.of(project3));

        // when
        ApiResponse<String> response = projectService.changeSequenceProject(changeSequenceProjectReqList);

        // then
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("아트워크 페이지에 보여질 프로젝트의 순서를 성공적으로 수정하였습니다.", response.getMessage());
        Mockito.verify(projectRepository, times(3)).findById(any(Long.class));
    }

    @Test
    @DisplayName("Artwork Page 프로젝트 순서 변경 실패 테스트 - 유효하지 않은 ID")
    void changeSequenceProjectFail_invalidID() {
        // given
        List<ChangeSequenceProjectReq> changeSequenceProjectReqList = new ArrayList<>();
        ChangeSequenceProjectReq req1 = new ChangeSequenceProjectReq();
        req1.setProjectId(0L);
        req1.setSequence(1);
        changeSequenceProjectReqList.add(req1);
        ChangeSequenceProjectReq req2 = new ChangeSequenceProjectReq();
        req2.setProjectId(1L);
        req2.setSequence(2);
        changeSequenceProjectReqList.add(req2);
        ChangeSequenceProjectReq req3 = new ChangeSequenceProjectReq();
        req3.setProjectId(2L);
        req3.setSequence(0);
        changeSequenceProjectReqList.add(req3);

        // stub
        when(projectRepository.findById(req1.getProjectId())).thenReturn(Optional.empty());

        // when
        ApiResponse<String> response = projectService.changeSequenceProject(changeSequenceProjectReqList);

        // then
        assertEquals(ErrorCode.INVALID_PROJECT_ID.getStatus(), response.getStatus());
        assertEquals(ErrorCode.INVALID_PROJECT_ID.getMessage(), response.getMessage());
    }

    @Test
    @DisplayName("Main Page 프로젝트 순서 변경 성공 테스트")
    void changeMainSequenceProjectSuccess() {
        // given
        Project project1 = Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(1)
                .sequence(0)
                .build();
        Project project2 = Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(1)
                .sequence(0)
                .build();
        Project project3 = Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("main")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(1)
                .sequence(0)
                .build();

        List<ChangeMainSequenceProjectReq> changeMainSequenceProjectReqList = new ArrayList<>();
        ChangeMainSequenceProjectReq req1 = new ChangeMainSequenceProjectReq();
        req1.setProjectId(0L);
        req1.setMainSequence(1);
        changeMainSequenceProjectReqList.add(req1);
        ChangeMainSequenceProjectReq req2 = new ChangeMainSequenceProjectReq();
        req2.setProjectId(1L);
        req2.setMainSequence(2);
        changeMainSequenceProjectReqList.add(req2);
        ChangeMainSequenceProjectReq req3 = new ChangeMainSequenceProjectReq();
        req3.setProjectId(2L);
        req3.setMainSequence(0);
        changeMainSequenceProjectReqList.add(req3);

        // stub
        when(projectRepository.findById(req1.getProjectId())).thenReturn(Optional.of(project1));
        when(projectRepository.findById(req2.getProjectId())).thenReturn(Optional.of(project2));
        when(projectRepository.findById(req3.getProjectId())).thenReturn(Optional.of(project3));

        // when
        ApiResponse<String> response = projectService.changeMainSequenceProject(changeMainSequenceProjectReqList);

        // then
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("메인 페이지에 보여질 프로젝트의 순서를 성공적으로 수정하였습니다.", response.getMessage());
        Mockito.verify(projectRepository, times(3)).findById(any(Long.class));
    }

    @Test
    @DisplayName("Main Page 프로젝트 순서 변경 실패 테스트 - 유효하지 않은 ID")
    void changeMainSequenceProjectFail_invalidID() {
        // given
        List<ChangeMainSequenceProjectReq> changeMainSequenceProjectReqList = new ArrayList<>();
        ChangeMainSequenceProjectReq req1 = new ChangeMainSequenceProjectReq();
        req1.setProjectId(0L);
        req1.setMainSequence(1);
        changeMainSequenceProjectReqList.add(req1);
        ChangeMainSequenceProjectReq req2 = new ChangeMainSequenceProjectReq();
        req2.setProjectId(1L);
        req2.setMainSequence(2);
        changeMainSequenceProjectReqList.add(req2);
        ChangeMainSequenceProjectReq req3 = new ChangeMainSequenceProjectReq();
        req3.setProjectId(2L);
        req3.setMainSequence(0);
        changeMainSequenceProjectReqList.add(req3);

        // stub
        when(projectRepository.findById(req1.getProjectId())).thenReturn(Optional.empty());

        // when
        ApiResponse<String> response = projectService.changeMainSequenceProject(changeMainSequenceProjectReqList);

        // then
        assertEquals(ErrorCode.INVALID_PROJECT_ID.getStatus(), response.getStatus());
        assertEquals(ErrorCode.INVALID_PROJECT_ID.getMessage(), response.getMessage());
    }

    @Test
    @DisplayName("Main Page 프로젝트 순서 변경 실패 테스트 - main type이 아닌 경우")
    void changeMainSequenceProjectFail_invalidProjectType() {
        // given
        Project project1 = Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("others")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(1)
                .sequence(0)
                .build();

        List<ChangeMainSequenceProjectReq> changeMainSequenceProjectReqList = new ArrayList<>();
        ChangeMainSequenceProjectReq req1 = new ChangeMainSequenceProjectReq();
        req1.setProjectId(0L);
        req1.setMainSequence(1);
        changeMainSequenceProjectReqList.add(req1);
        ChangeMainSequenceProjectReq req2 = new ChangeMainSequenceProjectReq();
        req2.setProjectId(1L);
        req2.setMainSequence(2);
        changeMainSequenceProjectReqList.add(req2);
        ChangeMainSequenceProjectReq req3 = new ChangeMainSequenceProjectReq();
        req3.setProjectId(2L);
        req3.setMainSequence(0);
        changeMainSequenceProjectReqList.add(req3);

        // stub
        when(projectRepository.findById(req1.getProjectId())).thenReturn(Optional.of(project1));

        // when
        ApiResponse<String> response = projectService.changeMainSequenceProject(changeMainSequenceProjectReqList);

        // then
        assertEquals(ErrorCode.INVALID_PROJECT_ID.getStatus(), response.getStatus());
        assertEquals(ErrorCode.INVALID_PROJECT_ID.getMessage(), response.getMessage());
    }

    @Test
    @DisplayName("프로젝트 페이지네이션 조회 성공 테스트")
    void retrieveArtworkProjectPageSuccess() {
        // given
        int page = 0;
        int size = 2;
        Pageable pageable = PageRequest.of(page, size);

        List<Project> projectList = new ArrayList<>();
        projectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("others")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(1)
                .sequence(0)
                .build());
        projectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("others")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(1)
                .sequence(0)
                .build());
        projectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("others")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(1)
                .sequence(0)
                .build());
        projectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("others")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(1)
                .sequence(0)
                .build());
        projectList.add(Project.builder()
                .name("Test Name")
                .category("Entertainment")
                .department("Test Department")
                .date("2024-01-01")
                .link("Test Link")
                .overView("Test Overview")
                .isPosted(true)
                .projectType("others")
                .mainImg("test url")
                .mainImgFileName(mockFile.getName())
                .responsiveMainImg("test url")
                .responsiveMainImgFileName(mockFile.getName())
                .mainSequence(1)
                .sequence(0)
                .build());


        Page<Project> projectPage = new PageImpl<>(projectList, pageable, projectList.size());

        // stub
        when(projectRepository.findAll(pageable)).thenReturn(projectPage);

        // when
        Page<Project> response = projectService.retrieveArtworkProjectPage(page, size);

        // then
        assertNotNull(response);
        assertEquals(response.getNumber(), page);
        assertEquals(response.getSize(), size);
        Mockito.verify(projectRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("프로젝트 페이지네이션 조회 실패 테스트 - 잘못된 page인 경우")
    void retrieveArtworkProjectPageFail_invalidPage() {
        // given
        int page = -1; // 잘못된 페이지 번호
        int size = 10;

        // when & then
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> projectService.retrieveArtworkProjectPage(page, size));
        assertEquals("Page index must not be less than zero", exception.getMessage());
    }

    @Test
    @DisplayName("프로젝트 페이지네이션 조회 실패 테스트 - 잘못된 size인 경우")
    void retrieveArtworkProjectPageFail_invalidSize() {
        // given
        int page = 0;
        int size = 0; // 잘못된 크기

        // when & then
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> projectService.retrieveArtworkProjectPage(page, size));
        assertEquals("Page size must not be less than one", exception.getMessage());
    }
}
