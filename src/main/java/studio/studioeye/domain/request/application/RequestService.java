package studio.studioeye.domain.request.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import studio.studioeye.domain.email.service.EmailService;
import studio.studioeye.domain.notification.application.NotificationService;
import studio.studioeye.domain.request.dao.AnswerRepository;
import studio.studioeye.domain.request.dao.RequestCount;
import studio.studioeye.domain.request.dao.RequestRepository;
import studio.studioeye.domain.request.domain.Answer;
import studio.studioeye.domain.request.domain.Request;
import studio.studioeye.domain.request.domain.State;
import studio.studioeye.domain.request.dto.request.CreateRequestServiceDto;
import studio.studioeye.domain.request.dto.request.UpdateRequestCommentServiceDto;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;
import studio.studioeye.infrastructure.s3.S3Adapter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
@RequiredArgsConstructor
public class RequestService {

	private final RequestRepository requestRepository;
	private final AnswerRepository answerRepository;
	private final S3Adapter s3Adapter;

	private final NotificationService notificationService;
	private final EmailService emailService;

	private static final String EMAIL_REGEX =
			"^[a-zA-Z0-9_+&*-]+(?:\\." +
					"[a-zA-Z0-9_+&*-]+)*@" +
					"(?:[a-zA-Z0-9-]+\\.)+[a-z" +
					"A-Z]{2,7}$";

	public ApiResponse<Request> createRequest(CreateRequestServiceDto dto, List<MultipartFile> files) throws IOException {
		if(!isValidEmail(dto.email())) {
			return ApiResponse.withError(ErrorCode.INVALID_EMAIL_FORMAT);
		}
		List<String> fileUrlList = new LinkedList<>();
		if (files != null) {
			for (var file : files) {
				ApiResponse<String> updateFileResponse = s3Adapter.uploadFile(file);

				if (updateFileResponse.getStatus().is5xxServerError()) {
					return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
				}
				String fileUrl = updateFileResponse.getData();
				fileUrlList.add(fileUrl);
			}
		}

		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		Integer year = Integer.parseInt(new SimpleDateFormat("yyyy").format(new Date().getTime()));
		Integer month = Integer.parseInt(new SimpleDateFormat("MM").format(new Date().getTime()));

		Request request = dto.toEntity(fileUrlList, new ArrayList<>(), year, month, State.WAITING, new Date());
		Request savedRequest = requestRepository.saveAndFlush(request);

		String subject = "[STUDIO EYE] 문의가 완료되었습니다."; // 이메일 제목
		String text = "아래와 같이 작성하신 문의가 성공적으로 접수되었습니다.\n"
				+ "추후 담당자 배정 후 해당 메일로 결과를 전송드리겠습니다.\n\n\n"

				+ "의뢰인 성명: " + savedRequest.getClientName() + "\n"
				+ "기관 혹은 기업: " + savedRequest.getOrganization() + "\n"
//				+ "이메일 주소: " + savedRequest.getEmail() + "\n"
				+ "직책: " + savedRequest.getPosition() + "\n"
				+ "연락처: " + savedRequest.getContact() + "\n\n"

				+ "카테고리: " + savedRequest.getCategory() + "\n"
				+ "프로젝트명: " + savedRequest.getProjectName() + "\n"
				+ "문의 내용: " + savedRequest.getDescription() + "\n";

		boolean isExceeded = emailService.sendEmail(savedRequest.getEmail(), subject, text);
		if(!isExceeded) {
			return ApiResponse.withError(ErrorCode.EMAIL_SIZE_EXCEEDED);
		}
		notificationService.subscribe(request.getId());    // 문의 등록 알림 보내기
		return ApiResponse.ok("문의를 성공적으로 등록하였습니다.", savedRequest);
	}

	public ApiResponse<List<Request>> retlrieveAllRequest() {
		List<Request> requestList = requestRepository.findAll();

		if (requestList.isEmpty()){
			return ApiResponse.ok("문의가 존재하지 않습니다.");
		}
		return ApiResponse.ok("문의 목록을 성공적으로 조회했습니다.", requestList);
	}

	public ApiResponse<Request> retrieveRequest(Long requestId) {
		Optional<Request> optionalRequest = requestRepository.findById(requestId);
		if(optionalRequest.isEmpty()){
			return ApiResponse.withError(ErrorCode.INVALID_REQUEST_ID);
		}

		Request request = optionalRequest.get();
		return ApiResponse.ok("문의를 성공적으로 조회했습니다.", request);
	}

	public ApiResponse<List<Map<String, Object>>> retrieveRequestCountByCategoryAndState(String category, String state,
																						 Integer startYear,
																						 Integer startMonth,
																						 Integer endYear,
																						 Integer endMonth) {
		// State enum으로 변환
		State stateEnum;
		try {
			// 상태가 "all"일 경우 null로 설정
			stateEnum = (state == null || state.equalsIgnoreCase("all")) ? null : State.valueOf(state.toUpperCase());
		} catch (IllegalArgumentException e) {
			return ApiResponse.withError(ErrorCode.INVALID_INPUT_VALUE);
		}
		// 카테고리 변환
		if (category == null || category.equalsIgnoreCase("all")) {
			category = null; // "all"일 때 null로 설정
		}

		// 월 형식 검사 및 유효성 검사 통합
		if (startMonth < 1 || startMonth > 12 || endMonth < 1 || endMonth > 12) {
			return ApiResponse.withError(ErrorCode.INVALID_REQUEST_MONTH);
		}

		// 종료점이 시작점보다 앞에 있을 경우 제한
		if (startYear > endYear || (startYear.equals(endYear) && startMonth > endMonth)) {
			return ApiResponse.withError(ErrorCode.INVALID_PERIOD_FORMAT);
		}

		// 2~12달로 제한
		Integer months = (endYear - startYear) * 12 + (endMonth - startMonth) + 1;
		if (months < 2 || months > 12) {
			return ApiResponse.withError(ErrorCode.INVALID_REQUEST_PERIOD);
		}

		// 데이터 조회
		List<RequestCount> requestCountList = requestRepository.findReqNumByYearAndMonthBetweenWithCategoryAndState(
				startYear, startMonth, endYear, endMonth, category, stateEnum);

		// 응답 데이터 초기화
		List<Map<String, Object>> responseList = new ArrayList<>();
		for (int year = startYear; year <= endYear; year++) {
			int monthStart = (year == startYear) ? startMonth : 1;
			int monthEnd = (year == endYear) ? endMonth : 12;

			for (int month = monthStart; month <= monthEnd; month++) {
				// 해당 연도와 월에 대한 stateRequestCount 초기화
				Map<String, Long> stateRequestCount = new HashMap<>();
				for (RequestCount requestCount : requestCountList) {
					if (requestCount.getYear() == year && requestCount.getMonth() == month) {
						stateRequestCount.put(String.valueOf(requestCount.getState()), requestCount.getRequestCount());
					}
				}

				Map<String, Object> responseItem = new HashMap<>();
				responseItem.put("year", year);
				responseItem.put("month", month);
				responseItem.put("RequestCount", stateRequestCount);
				responseList.add(responseItem);
			}
		}

		return ApiResponse.ok("문의수 목록을 성공적으로 조회했습니다.", responseList);
	}

	public ApiResponse<List<Request>> retrieveWaitingRequest() {
		List<Request> requestList = requestRepository.findByState(State.WAITING);

		if (requestList.isEmpty()){
			return ApiResponse.ok("접수 대기 중인 문의가 존재하지 않습니다.");
		}
		return ApiResponse.ok("접수 대기 중인 문의 목록을 성공적으로 조회했습니다.", requestList);
	}

	public ApiResponse<String> updateRequestComment(Long requestId, UpdateRequestCommentServiceDto dto) {
		String answer = dto.answer().trim();
		State state = dto.state();

		// 빈 답변 처리
		if (answer.isEmpty()) {
			return ApiResponse.withError(ErrorCode.INVALID_INPUT_VALUE);
		}

		// 상태가 null인 경우 처리
		if (state == null) {
			return ApiResponse.withError(ErrorCode.INVALID_INPUT_VALUE);
		}

		Optional<Request> optionalRequest = requestRepository.findById(requestId);
		if(optionalRequest.isEmpty()){
			return ApiResponse.withError(ErrorCode.INVALID_REQUEST_ID);
		}
		Request request = optionalRequest.get();
		Answer updatedAnswer = Answer.builder()
				.text(answer)
				.state(state)
				.createdAt(LocalDateTime.now())
				.request(request)
				.build();
		request.getAnswers().add(updatedAnswer);
		request.updateState(state);
		Request updatedRequest = requestRepository.save(request);
		answerRepository.save(updatedAnswer);

		if(!answer.isEmpty() && state != null) {
			String subject = "[STUDIO EYE] " + updatedRequest.getClientName() + "님의 문의에 답변이 작성되었습니다."; // 이메일 제목
			String text = "[문의 내역]\n\n"

					+ "의뢰인 성명: " + updatedRequest.getClientName() + "\n"
					+ "기관 혹은 기업: " + updatedRequest.getOrganization() + "\n"
					+ "직책: " + updatedRequest.getPosition() + "\n"
					+ "연락처: " + updatedRequest.getContact() + "\n\n"

					+ "카테고리: " + updatedRequest.getCategory() + "\n"
					+ "프로젝트명: " + updatedRequest.getProjectName() + "\n"
					+ "문의 내용: " + updatedRequest.getDescription() + "\n\n\n"


					+ "[답변 내용]" + "\n"
					+ answer + "\n";
			emailService.sendEmail(updatedRequest.getEmail(), subject, text);
		}

		return ApiResponse.ok("답변을 성공적으로 작성했습니다.");
	}

	public ApiResponse<String> deleteRequest(Long requestId) {
		Optional<Request> optionalRequest = requestRepository.findById(requestId);
		if(optionalRequest.isEmpty()){
			return ApiResponse.withError(ErrorCode.INVALID_REQUEST_ID);
		}

		Request request = optionalRequest.get();
		requestRepository.delete(request);

		return ApiResponse.ok("문의를 성공적으로 삭제했습니다.");
	}

	public static boolean isValidEmail(String email) {
		Pattern pattern = Pattern.compile(EMAIL_REGEX);
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}
}
