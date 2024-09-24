package studio.studioeye.global.adapter;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.error.ErrorCode;

import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class S3Adapter {

	private final AmazonS3Client amazonS3Client;

	@Value("${aws.s3.bucket}")
	private String bucket;


	public ApiResponse<String> uploadImage(MultipartFile multipartFile) {
		ObjectMetadata metadata = new ObjectMetadata();
		String fileName = UUID.randomUUID() + ".png";
		metadata.setContentLength(multipartFile.getSize());
		metadata.setContentType("image/png");
		try {
			amazonS3Client.putObject(bucket, fileName, multipartFile.getInputStream(), metadata);
			return ApiResponse.ok("S3 버킷에 이미지 업로드를 성공하였습니다.", amazonS3Client.getUrl(bucket, fileName).toString());
		} catch (IOException e) {
			return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
		}
	}

	public ApiResponse<String> uploadFile(MultipartFile multipartFile) throws IOException {
		String originalFilename = multipartFile.getOriginalFilename();

		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(multipartFile.getSize());
		metadata.setContentType(multipartFile.getContentType());

		try {
			amazonS3Client.putObject(bucket, originalFilename, multipartFile.getInputStream(), metadata);
			return ApiResponse.ok("S3 버킷에 이미지 업로드를 성공하였습니다.", amazonS3Client.getUrl(bucket, originalFilename).toString());
		}
		catch (IOException e) {
			return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
		}
	}



	public ApiResponse<String> deleteFile(String fileName){
		try{
			amazonS3Client.deleteObject(bucket, fileName);
			return ApiResponse.ok("S3 버킷에서 이미지를 성공적으로 삭제하였습니다.", amazonS3Client.getUrl(bucket, fileName).toString());

		}catch (SdkClientException e){
			return ApiResponse.withError(ErrorCode.ERROR_S3_DELETE_OBJECT);
		}
	}
}