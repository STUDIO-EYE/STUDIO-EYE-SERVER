package studio.studioeye.global.exception.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// Common

	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "Request Body를 통해 전달된 값이 유효하지 않습니다."),

	// S3
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 문제로 S3 이미지 업로드에 실패하였습니다."),
	NOT_EXIST_IMAGE_FILE(HttpStatus.BAD_REQUEST, "업로드 할 이미지가 존재하지 않습니다."),

	ERROR_S3_DELETE_OBJECT(HttpStatus.INTERNAL_SERVER_ERROR, "서버 문제 S3 이미지 삭제에 실패하였습니다."),
	ERROR_S3_UPDATE_OBJECT(HttpStatus.INTERNAL_SERVER_ERROR, "서버 문제로 S3 이미지 업로드에 실패하였습니다."),

	// client
	INVALID_CLIENT_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 client 식별자입니다."),

	// notice board
	INVALID_NOTICE_BOARD_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 notice board 식별자입니다."),

	// project
	INVALID_PROJECT_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 project 식별자입니다."),
	INVALID_PROJECT_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 project type입니다."),
	TOP_PROJECT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "TOP PROJECT가 이미 존재합니다."),
	MAIN_PROJECT_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "MAIN PROJECT의 개수 제한을 초과했습니다."),
	PROJECT_TYPE_AND_IS_POSTED_MISMATCH(HttpStatus.BAD_REQUEST, "PROJECT TYPE과 IS POSTED가 상응하지 않습니다."),

	// partner information
	INVALID_PARTNER_INFORMATION_ID(HttpStatus.BAD_REQUEST,"유효하지 않은 partner information 식별자입니다."),

	// request
	INVALID_REQUEST_ID(HttpStatus.BAD_REQUEST,"유효하지 않은 request 식별자입니다."),
	INVALID_REQUEST_MONTH(HttpStatus.BAD_REQUEST, "유효하지 않은 월 형식입니다."),
	INVALID_REQUEST_PERIOD(HttpStatus.BAD_REQUEST, "기간은 2~12달이어야 합니다."),
	INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "유효하지 않은 이메일 형식입니다."),

	// views
	INVALID_VIEWS_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 views 식별자입니다."),
	INVALID_VIEWS_MONTH(HttpStatus.BAD_REQUEST, "유효하지 않은 월 형식입니다."),
	ALREADY_EXISTED_DATA(HttpStatus.BAD_REQUEST, "이미 존재하는 데이터입니다."),
	INVALID_VIEWS_PERIOD(HttpStatus.BAD_REQUEST, "기간은 2~12달이어야 합니다."),
	INVALID_PERIOD_FORMAT(HttpStatus.BAD_REQUEST, "종료점은 시작점보다 뒤에 있어야 합니다."),
	INVALID_VIEWS_CATEGORY(HttpStatus.BAD_REQUEST, "ARTWORK를 제외한 다른 메뉴들의 category는 ALL이어야 합니다."),

	// notification
	USER_IS_EMPTY(HttpStatus.BAD_REQUEST, "User가 존재하지 않습니다."),
	INVALID_NOTIFICATION_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 notification 식별자입니다."),

	// user notification
	INVALID_USER_NOTIFICATION_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 user 혹은 notification 식별자입니다."),
	FAILED_USER_NOTIFICATION_DELETE(HttpStatus.INTERNAL_SERVER_ERROR, "알림 삭제에 실패하였습니다."),

	// sseEmitter
	INVALID_SSE_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 sse 식별자입니다."),

	// FAQ
	INVALID_FAQ_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 views 식별자입니다."),
	FAQ_IS_EMPTY(HttpStatus.BAD_REQUEST, "입력된 내용이 없습니다"),

	// CompanyInformation
	INVALID_COMPANYINFORMATION_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 company information 식별자입니다."),
	COMPANYINFORMATION_IS_EMPTY(HttpStatus.BAD_REQUEST, "CompanyInformation가 존재하지 않습니다."),

	// Ceo
	CEO_IS_EMPTY(HttpStatus.BAD_REQUEST, "CEO 정보가 존재하지 않습니다."),

	// Mail
	EMAIL_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "E-mail 용량이 초과되었습니다."),

	// recruitment
	INVALID_RECRUITMENT_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 recruitment 식별자입니다."),
	RECRUITMENT_IS_EMPTY(HttpStatus.BAD_REQUEST, "recruitment가 존재하지 않습니다."),
	INVALID_RECRUITMENT_PAGE(HttpStatus.BAD_REQUEST, "유효하지 않은 recruitment page입니다."),
	INVALID_RECRUITMENT_SIZE(HttpStatus.BAD_REQUEST, "유효하지 않은 recruitment size입니다."),
	INVALID_RECRUITMENT_DATE(HttpStatus.BAD_REQUEST, "유효하지 않은 recruitment 시작일과 마감일입니다."),
	RECRUITMENT_TITLE_IS_EMPTY(HttpStatus.BAD_REQUEST, "제목이 없습니다."),

	// news
	NEWS_IS_EMPTY(HttpStatus.BAD_REQUEST, "입력된 내용이 없습니다" ),
	INVALID_NEWS_ID(HttpStatus.BAD_REQUEST,"유효하지 않은 news 식별자입니다." ),

	// benefit
	INVALID_BENEFIT_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 benefit 식별자입니다."),

	// menu
	INVALID_MENU_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 menu 식별자입니다.");

	private final HttpStatus status;
	private final String message;

}