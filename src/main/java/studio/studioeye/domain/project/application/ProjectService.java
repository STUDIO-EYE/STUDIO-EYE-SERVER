package studio.studioeye.domain.project.application;

import studio.studioeye.domain.project.dao.ProjectRepository;
import studio.studioeye.domain.project.domain.Project;
import studio.studioeye.domain.project.domain.ProjectImage;
import studio.studioeye.domain.project.dto.request.*;
import studio.studioeye.domain.views.application.ViewsService;
import studio.studioeye.infrastructure.s3.S3Adapter;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectService {

	private final ProjectRepository projectRepository;
	private final S3Adapter s3Adapter;
	private final ViewsService viewsService;
	private static final String TOP_PROJECT_TYPE = "top";
	private static final String MAIN_PROJECT_TYPE = "main";
	private static final String OTHERS_PROJECT_TYPE = "others";

	// CREATE
	public ApiResponse<Project> createProject(CreateProjectServiceRequestDto dto,
											  MultipartFile mainImgFile, MultipartFile responsiveMainImgFile,
											  List<MultipartFile> files) throws IOException {
		String mainImg = getImgUrl(mainImgFile);
		if (mainImg.isEmpty()) return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
		String mainImgFileName = mainImgFile.getOriginalFilename();

		// TODO 임시 코드
		String responsiveMainImg = null;
		String responsiveMainImgFileName = null;

		if(responsiveMainImgFile != null) {
			responsiveMainImg = getImgUrl(responsiveMainImgFile);
			if (responsiveMainImg.isEmpty()) return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
			responsiveMainImgFileName = responsiveMainImgFile.getOriginalFilename();
		}

		List<ProjectImage> projectImages = new LinkedList<>();
		if (files != null) {
			for (MultipartFile file : files) {
				String imageUrl = getImgUrl(file);
				if (imageUrl.isEmpty()) return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);

				String fileName = file.getOriginalFilename(); // 원본 파일 이름 가져오기
				ProjectImage projectImage = ProjectImage.builder()
						.imageUrlList(imageUrl)
						.fileName(fileName)
						.build();
				projectImages.add(projectImage);
			}
		}
		// 총 프로젝트 개수
		long projectCount = projectRepository.count();

		// 프로젝트 타입 결정
		int mainSequence = 999;
		String projectType = dto.projectType();
		switch (projectType) {
			// 받아온 projectType String 값이 유효한 경우
			case TOP_PROJECT_TYPE:
				List<Project> topProject = projectRepository.findByProjectType(projectType);
				// TOP 프로젝트가 이미 존재하는 경우
				if (!topProject.isEmpty()) {
					return ApiResponse.withError(ErrorCode.TOP_PROJECT_ALREADY_EXISTS);
				}
				// top인데 isPosted를 false로 할 경우 프로젝트 생성 안 됨
				if (!dto.isPosted()) {
					return ApiResponse.withError(ErrorCode.PROJECT_TYPE_AND_IS_POSTED_MISMATCH);
				}
				break;
			case MAIN_PROJECT_TYPE:
				List<Project> mainProjects = projectRepository.findByProjectType(MAIN_PROJECT_TYPE);
				// "main"인 프로젝트가 이미 5개 이상인 경우
				if (mainProjects.size() >= 5) {
					return ApiResponse.withError(ErrorCode.MAIN_PROJECT_LIMIT_EXCEEDED);
				}
				// main인데 isPosted를 false로 할 경우 프로젝트 생성 안 됨
				if (!dto.isPosted()) {
					return ApiResponse.withError(ErrorCode.PROJECT_TYPE_AND_IS_POSTED_MISMATCH);
				}
				mainSequence = mainProjects.size() + 1;
				break;
			case OTHERS_PROJECT_TYPE:
				break;
			default: // 유효하지 않은 값일 경우
				return ApiResponse.withError(ErrorCode.INVALID_PROJECT_TYPE);
		}

		Project project = dto.toEntity(mainImg, mainImgFileName, responsiveMainImg, responsiveMainImgFileName, projectImages, projectCount, mainSequence);

		// ProjectImage의 project 필드 설정
		for (ProjectImage projectImage : projectImages) {
			projectImage.setProject(project);
		}

		Project savedProject = projectRepository.save(project);
		return ApiResponse.ok("프로젝트를 성공적으로 등록하였습니다.", savedProject);
	}

	// RETRIEVE
	// for artwork page
	public ApiResponse<List<Project>> retrieveAllArtworkProject() {
		List<Project> projectList = projectRepository.findAllWithImagesAndOrderBySequenceAsc();
		if (projectList.isEmpty()){
			return ApiResponse.ok("프로젝트가 존재하지 않습니다.");
		}

		return ApiResponse.ok("프로젝트 목록을 성공적으로 조회했습니다.", projectList);
	}

	// for main page
	public ApiResponse<List<Project>> retrieveAllMainProject() {
		List<Project> projectList = projectRepository.findAllWithImagesAndOrderByMainSequenceAsc();
		List<Project> responseProject = new ArrayList<>();
		List<Project> topProject = projectRepository.findByProjectType(TOP_PROJECT_TYPE);
		Project top;
		if (!topProject.isEmpty()) {
			top = topProject.get(0);
			responseProject.add(top);
		}
		responseProject.addAll(projectList);

		if (projectList.isEmpty()){
			return ApiResponse.ok("프로젝트가 존재하지 않습니다.");
		}

		return ApiResponse.ok("프로젝트 목록을 성공적으로 조회했습니다.", responseProject);

	}

	public ApiResponse<Project> retrieveProject(Long projectId) {
		Optional<Project> optionalProject = projectRepository.findById(projectId);
		if(optionalProject.isEmpty()){
			return ApiResponse.withError(ErrorCode.INVALID_PROJECT_ID);
		}

		Project project = optionalProject.get();
		return ApiResponse.ok("프로젝트를 성공적으로 조회했습니다.", project);
	}

	public Page<Project> retrieveArtworkProjectPage(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return projectRepository.findAll(pageable);
	}

	// UPDATE
	public ApiResponse<Project> updateProject(UpdateProjectServiceRequestDto dto,
											  MultipartFile mainImgFile, MultipartFile responsiveMainImgFile,
											  List<MultipartFile> files) throws IOException {
		if(mainImgFile == null || mainImgFile.isEmpty()) {
			return ApiResponse.withError(ErrorCode.NOT_EXIST_IMAGE_FILE);
		}
		// 임시 주석 해제
		if(responsiveMainImgFile == null || responsiveMainImgFile.isEmpty()) {
			return ApiResponse.withError(ErrorCode.NOT_EXIST_IMAGE_FILE);
		}
		Optional<Project> optionalProject = projectRepository.findById(dto.projectId());
		if(optionalProject.isEmpty()){
			return ApiResponse.withError(ErrorCode.INVALID_PROJECT_ID);
		}

		Project project = optionalProject.get();

		String projectType = dto.projectType();
		switch (projectType) {
			// 받아온 projectType String 값이 유효한 경우
			case TOP_PROJECT_TYPE:
				List<Project> topProject = projectRepository.findByProjectType(projectType);
				// TOP 프로젝트가 이미 존재하고, 전달된 프로젝트 id가 이미 존재하는 TOP 프로젝트 id와 다른 경우
				if (!topProject.isEmpty() && !project.getId().equals(topProject.get(0).getId())) {
					return ApiResponse.withError(ErrorCode.TOP_PROJECT_ALREADY_EXISTS);
				}
				// 기존의 프로젝트 타입이 main이었을 경우, 다른 main 프로젝트들의 mainSequence 수정
				if (project.getProjectType().equals(MAIN_PROJECT_TYPE)) {
					List<Project> findByMainSequenceGreaterThan
							= projectRepository.findAllByMainSequenceGreaterThanAndMainSequenceNot(project.getMainSequence(), 999);
					for (Project findMainProject : findByMainSequenceGreaterThan) {
						findMainProject.updateMainSequence(findMainProject.getMainSequence() - 1);
					}
				}
				project.updateProjectType(projectType);
				project.updateMainSequence(999);
				break;
			case MAIN_PROJECT_TYPE:
				// 원래 프로젝트 타입이 main이었으면 종료
				if (project.getProjectType().equals(MAIN_PROJECT_TYPE))
					break;
				List<Project> mainProjects = projectRepository.findByProjectType(MAIN_PROJECT_TYPE);
				// "main"인 프로젝트가 이미 5개 이상이고 전달된 프로젝트가 원래 main이 아닌 경우
				if (mainProjects.size() >= 5) {
					return ApiResponse.withError(ErrorCode.MAIN_PROJECT_LIMIT_EXCEEDED);
				}
				project.updateProjectType(projectType);
				Integer mainSequence = projectRepository.countByProjectType(projectType);
				project.updateMainSequence(mainSequence);
				break;
			case OTHERS_PROJECT_TYPE:
				// 기존의 프로젝트 타입이 main이었을 경우, 다른 main 프로젝트들의 mainSequence 수정
				if (project.getProjectType().equals(MAIN_PROJECT_TYPE)) {
					List<Project> findByMainSequenceGreaterThan
							= projectRepository.findAllByMainSequenceGreaterThanAndMainSequenceNot(project.getMainSequence(), 999);
					for (Project findMainProject : findByMainSequenceGreaterThan) {
						findMainProject.updateMainSequence(findMainProject.getMainSequence() - 1);
					}
				}
				project.updateProjectType(projectType);
				project.updateMainSequence(999);
				break;
			default: // 유효하지 않은 값일 경우
				return ApiResponse.withError(ErrorCode.INVALID_PROJECT_TYPE);
		}

		// 기존 메인 이미지 삭제
		String mainImgFileName = project.getMainImgFileName();
		if(mainImgFileName != null) s3Adapter.deleteFile(mainImgFileName);

		// 기존 반응형 메인이미지 삭제
		String responsiveMainImgFileName = project.getMainImgFileName();
		if(responsiveMainImgFileName != null) s3Adapter.deleteFile(responsiveMainImgFileName);

		// 기존 이미지들 전체 삭제
		List<ProjectImage> existingImages = project.getProjectImages();
		// S3에서 기존 이미지들 삭제
		for (ProjectImage image : existingImages) {
			String fileName = image.getFileName();
			// S3Adapter의 deleteFile 메소드를 호출하여 이미지를 삭제
			s3Adapter.deleteFile(fileName);
		}
		project.getProjectImages().clear();

		// 새로운 메인이미지 저장
		String mainImg = getImgUrl(mainImgFile);
		if (mainImg.isEmpty()) return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
		project.setMainImg(mainImg);
		project.setMainImgFileName(mainImgFile.getOriginalFilename());

		// 새로운 반응형 메인이미지 저장
		// TODO 임시 코드
		if(responsiveMainImgFile != null) {
			String responsiveMainImg = getImgUrl(responsiveMainImgFile);
			if (responsiveMainImg.isEmpty()) return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
			project.setResponsiveMainImg(responsiveMainImg);
			project.setResponsiveMainImgFileName(responsiveMainImgFile.getOriginalFilename());
		}

		// 기존 이미지 + 새로운 이미지들 저장
		List<ProjectImage> projectImages = new LinkedList<>();
		if (files != null) {
			for (MultipartFile file : files) {
				String imageUrl = getImgUrl(file);
				if (imageUrl.isEmpty()) return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);

				String fileName = file.getOriginalFilename(); // 원본 파일 이름 가져오기
				ProjectImage projectImage = ProjectImage.builder()
						.project(project)
						.imageUrlList(imageUrl)
						.fileName(fileName)
						.build();
				projectImages.add(projectImage);
			}
			project.getProjectImages().addAll(projectImages);
		}

		Project updatedProject = projectRepository.save(project);
		updatedProject.update(dto);
		return ApiResponse.ok("프로젝트를 성공적으로 수정했습니다.", updatedProject);
	}

	// 프로젝트 순서 변경 : artwork page
	public ApiResponse<String> changeSequenceProject(List<ChangeSequenceProjectReq> changeSequenceProjectReqList) {

		for (ChangeSequenceProjectReq changeSequenceProjectReq : changeSequenceProjectReqList) {
			Optional<Project> findProject = projectRepository.findById(changeSequenceProjectReq.getProjectId());
			if (findProject.isEmpty())
				return ApiResponse.withError(ErrorCode.INVALID_PROJECT_ID);
			Project project = findProject.get();
			project.updateSequence(changeSequenceProjectReq.getSequence());
		}

		return ApiResponse.ok("아트워크 페이지에 보여질 프로젝트의 순서를 성공적으로 수정하였습니다.");
	}

	// 프로젝트 순서 변경 : main page
	public ApiResponse<String> changeMainSequenceProject(List<ChangeMainSequenceProjectReq> changeMainSequenceProjectReqList) {

		for (ChangeMainSequenceProjectReq changeMainSequenceProjectReq : changeMainSequenceProjectReqList) {
			Optional<Project> findProject = projectRepository.findById(changeMainSequenceProjectReq.getProjectId());
			if (findProject.isEmpty())
				return ApiResponse.withError(ErrorCode.INVALID_PROJECT_ID);
			Project project = findProject.get();
			if (!project.getProjectType().equals(MAIN_PROJECT_TYPE))
				return ApiResponse.withError(ErrorCode.INVALID_PROJECT_ID);
			project.updateMainSequence(changeMainSequenceProjectReq.getMainSequence());
		}

		return ApiResponse.ok("메인 페이지에 보여질 프로젝트의 순서를 성공적으로 수정하였습니다.");
	}

	public ApiResponse<Project> updatePostingStatus(UpdatePostingStatusDto dto) {
		Optional<Project> optionalProject = projectRepository.findById(dto.projectId());
		if(optionalProject.isEmpty()){
			return ApiResponse.withError(ErrorCode.INVALID_PROJECT_ID);
		}

		Project project = optionalProject.get();
		Project updatedProject = project.updatePostingStatus(dto.isPosted());

		return ApiResponse.ok("프로젝트 게시 여부를 성공적으로 변경하였습니다.", updatedProject);

	}

	public ApiResponse<Project> updateProjectType(UpdateProjectTypeDto dto) {
		String projectType = dto.projectType();
		Optional<Project> optionalProject = projectRepository.findById(dto.projectId());
		if(optionalProject.isEmpty()){
			return ApiResponse.withError(ErrorCode.INVALID_PROJECT_ID);
		}
		Project project = optionalProject.get();

		switch (projectType) {
			// 받아온 projectType String 값이 유효한 경우
			case TOP_PROJECT_TYPE:
				List<Project> topProject = projectRepository.findByProjectType(projectType);
				// TOP 프로젝트가 이미 존재하고, 전달된 프로젝트 id가 이미 존재하는 TOP 프로젝트 id와 다른 경우
				if (!topProject.isEmpty() && !project.getId().equals(topProject.get(0).getId())) {
					return ApiResponse.withError(ErrorCode.TOP_PROJECT_ALREADY_EXISTS);
				}
				// 기존의 프로젝트 타입이 main이었을 경우, 다른 main 프로젝트들의 mainSequence 수정
				if (project.getProjectType().equals(MAIN_PROJECT_TYPE)) {
					List<Project> findByMainSequenceGreaterThan
							= projectRepository.findAllByMainSequenceGreaterThanAndMainSequenceNot(project.getMainSequence(), 999);
					for (Project findMainProject : findByMainSequenceGreaterThan) {
						findMainProject.updateMainSequence(findMainProject.getMainSequence() - 1);
					}
				}
				Project updatedTopProject = project.updateProjectType(projectType);
				updatedTopProject.updateMainSequence(999);
				updatedTopProject.setIsPosted(true);
				return ApiResponse.ok("프로젝트 타입을 성공적으로 변경하였습니다.", updatedTopProject);
			case MAIN_PROJECT_TYPE:
				// 원래 프로젝트 타입이 main이었으면 종료
				if (project.getProjectType().equals(MAIN_PROJECT_TYPE))
					return ApiResponse.ok("프로젝트 타입을 성공적으로 변경하였습니다.", project);
				List<Project> mainProjects = projectRepository.findByProjectType(MAIN_PROJECT_TYPE);
				// "main"인 프로젝트가 이미 5개 이상인 경우
				if (mainProjects.size() >= 5) {
					return ApiResponse.withError(ErrorCode.MAIN_PROJECT_LIMIT_EXCEEDED);
				}
				Project updatedMainProject = project.updateProjectType(projectType);
				// mainSequence 수정
				Integer mainSequence = projectRepository.countByProjectType(projectType);
				updatedMainProject.updateMainSequence(mainSequence);
				updatedMainProject.setIsPosted(true);
				return ApiResponse.ok("프로젝트 타입을 성공적으로 변경하였습니다.", updatedMainProject);
			case OTHERS_PROJECT_TYPE:
				// 기존의 프로젝트 타입이 main이었을 경우, 다른 main 프로젝트들의 mainSequence 수정
				if (project.getProjectType().equals(MAIN_PROJECT_TYPE)) {
					List<Project> findByMainSequenceGreaterThan
							= projectRepository.findAllByMainSequenceGreaterThanAndMainSequenceNot(project.getMainSequence(), 999);
					for (Project findMainProject : findByMainSequenceGreaterThan) {
						findMainProject.updateMainSequence(findMainProject.getMainSequence() - 1);
					}
				}
				Project updatedProject = project.updateProjectType(projectType);
				updatedProject.updateMainSequence(999);
				return ApiResponse.ok("프로젝트 타입을 성공적으로 변경하였습니다.", updatedProject);
			default: // 유효하지 않은 값일 경우
				return ApiResponse.withError(ErrorCode.INVALID_PROJECT_TYPE);
		}
	}

	// DELETE
	public ApiResponse<String> deleteProject(Long projectId) {
		Optional<Project> optionalProject = projectRepository.findById(projectId);
		if(optionalProject.isEmpty()){
			return ApiResponse.withError(ErrorCode.INVALID_PROJECT_ID);
		}

		Project project = optionalProject.get();
		Integer sequence = project.getSequence();
		Integer mainSequence = project.getMainSequence();

		// 메인 이미지 삭제
		String mainImgFileName = project.getMainImgFileName();
		if(mainImgFileName != null) s3Adapter.deleteFile(mainImgFileName);

		// 반응형 메인이미지 삭제
		String responsiveMainImgFileName = project.getMainImgFileName();
		if(responsiveMainImgFileName != null) s3Adapter.deleteFile(responsiveMainImgFileName);

		// 이미지들 전체 삭제
		List<ProjectImage> existingImages = project.getProjectImages();
		// S3에서 이미지들 삭제
		for (ProjectImage image : existingImages) {
			String fileName = image.getFileName();
			// S3Adapter의 deleteFile 메소드를 호출하여 이미지를 삭제
			if(fileName != null) s3Adapter.deleteFile(fileName);
		}
		project.getProjectImages().clear();

		projectRepository.delete(project);

		List<Project> findBySequenceGreaterThan = projectRepository.findAllBySequenceGreaterThan(sequence);
		List<Project> findByMainSequenceGreaterThan
				= projectRepository.findAllByMainSequenceGreaterThanAndMainSequenceNot(mainSequence, 999);
		for (Project findArtworkProject : findBySequenceGreaterThan) {
			findArtworkProject.updateSequence(findArtworkProject.getSequence() - 1);
		}

		if (project.getProjectType().equals(MAIN_PROJECT_TYPE)) {
			for (Project findMainProject : findByMainSequenceGreaterThan) {
				findMainProject.updateMainSequence(findMainProject.getMainSequence() - 1);
			}
		}
		return ApiResponse.ok("프로젝트를 성공적으로 삭제했습니다.");
	}

	// UTILITY
	private String getImgUrl(MultipartFile file) throws IOException {
		ApiResponse<String> updateFileResponse = s3Adapter.uploadFile(file);

		if(updateFileResponse.getStatus().is5xxServerError()){

			return "";
		}
		return updateFileResponse.getData();
	}
}
