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
}
