package kr.elta.backend.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import kr.elta.backend.dto.ResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/aws")
@Slf4j
public class Aws {
    @Value("${aws.access-key-id}")
    private String accessKeyId;

    @Value("${aws.secret-access-key}")
    private String secretAccessKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @GetMapping("/presign-url")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Presign URL 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ResponseDTO> getPresignUrl(
            @RequestParam String fileName,
            @RequestParam(defaultValue = "upload") String type) {
        
        try {
            // AWS 자격증명 설정
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

            // S3 Presigner 클라이언트 생성
            S3Presigner presigner = S3Presigner.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                    .build();

            String presignedUrl;

            String objectKey = type + "/" + UUID.randomUUID();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10)) // 10분 유효
                    .putObjectRequest(putObjectRequest)
                    .build();

            presignedUrl = presigner.presignPutObject(presignRequest).url().toString();
            presigner.close();

            // 업로드용 응답에 추가 정보 포함
            java.util.Map<String, Object> uploadInfo = new java.util.HashMap<>();
            uploadInfo.put("uploadUrl", presignedUrl);
            uploadInfo.put("downloadUrl", "https://static.elta.kr/"+objectKey);
            return new ResponseEntity<>(new ResponseDTO(true, "success", uploadInfo), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Presign URL 생성 실패: ", e);
            return new ResponseEntity<>(new ResponseDTO(false, "Presign URL 생성 실패: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}