package studio.studioeye.domain.project.api;

import studio.studioeye.domain.project.application.ProjectService;
import studio.studioeye.domain.project.domain.Project;
import studio.studioeye.domain.project.dto.request.*;
import studio.studioeye.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "프로젝트 API", description = "프로젝트 등록 / 수정 / 삭제 / 조회")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProjectController {

	private final ProjectService projectService;

	// POST
	@Operation(summary = "프로젝트 등록 API")
	@PostMapping("/projects")
	public ApiResponse<Project> createProject(@Valid @RequestPart("request") CreateProjectRequestDto dto,
											  @RequestPart(value = "file", required = false) MultipartFile mainImgFile,
											  @RequestPart(value = "responsiveFile", required = false) MultipartFile responsiveMainImgFile,
											  @RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {
		return projectService.createProject(dto.toServiceRequest(), mainImgFile, responsiveMainImgFile, files);
	}

	// GET
	@Operation(summary = "프로젝트 전체 조회 API (request 페이지, sequence 순)")
	@GetMapping("/projects")
	public ApiResponse<List<Project>> retrieveAllArtworkProject(){
		return projectService.retrieveAllArtworkProject();
	}

	@Operation(summary = "프로젝트 전체 조회 API (메인 페이지, top, main1, main2 .. 순)")
	@GetMapping("/projects/main")
	public ApiResponse<List<Project>> retrieveAllMainProject(){
		return projectService.retrieveAllMainProject();
	}

	@Operation(summary = "프로젝트 상세 조회 API")
	@GetMapping("/projects/{projectId}")
	public ApiResponse<Project> retrieveProject(@PathVariable Long projectId){
		return projectService.retrieveProject(projectId);
	}

	@Operation(summary = "프로젝트 페이지네이션 조회 API (request 페이지, sequence 순)")
	@GetMapping("/projects/page")
	public Page<Project> retrieveArtworkProjectPage(@RequestParam(defaultValue = "0") int page,
													@RequestParam(defaultValue = "10") int size){
		return projectService.retrieveArtworkProjectPage(page, size);
	}

	// PUT
	@Operation(summary = "프로젝트 수정 API")
	@PutMapping("/projects")
	public ApiResponse<Project> updateProject(@Valid @RequestPart("request") UpdateProjectRequestDto dto,
											  @RequestPart(value = "file", required = false) MultipartFile mainImgFile,
											  @RequestPart(value = "responsiveFile", required = false) MultipartFile responsiveMainImgFile,
											  @RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {
		return projectService.updateProject(dto.toServiceRequest(), mainImgFile, responsiveMainImgFile, files);
	}

	@Operation(summary = "Artwork Page 프로젝트 순서 변경 API")
	@PutMapping("/projects/sequence")
	public ApiResponse<String> changeSequenceProject(@RequestBody List<ChangeSequenceProjectReq> changeSequenceProjectReqList){
		return projectService.changeSequenceProject(changeSequenceProjectReqList);
	}

	@Operation(summary = "Main Page 프로젝트 순서 변경 API")
	@PutMapping("/projects/main/sequence")
	public ApiResponse<String> changeMainSequenceProject(@RequestBody List<ChangeMainSequenceProjectReq> changeMainSequenceProjectReqList){
		return projectService.changeMainSequenceProject(changeMainSequenceProjectReqList);
	}


	@Operation(summary = "게시 여부 변경 API")
	@PutMapping("/projects/is-posted")
	public ApiResponse<Project> updatePostingStatus(@Valid @RequestBody UpdatePostingStatusDto dto){
		return projectService.updatePostingStatus(dto);
	}

	@Operation(summary = "프로젝트 타입 변경 API")
	@PutMapping("/projects/project-type")
	public ApiResponse<Project> updateProjectType(@Valid @RequestBody UpdateProjectTypeDto dto){
		return projectService.updateProjectType(dto);
	}


	// DELETE
	@Operation(summary = "프로젝트 삭제 API")
	@DeleteMapping("/projects/{projectId}")
	public ApiResponse<String> deleteProject(@PathVariable Long projectId){
		return projectService.deleteProject(projectId);
	}

}
