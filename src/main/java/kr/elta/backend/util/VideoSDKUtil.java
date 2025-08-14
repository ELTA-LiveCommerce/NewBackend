package kr.elta.backend.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class VideoSDKUtil {
    
    @Value("${videosdk.api.key}")
    private String apiKey;
    
    @Value("${videosdk.secret.key}")
    private String secretKey;
    
    /**
     * VideoSDK 토큰 생성
     * @param roomId 방 ID (선택사항)
     * @param participantId 참가자 ID (선택사항)
     * @return JWT 토큰
     */
    public String generateToken(String roomId, String participantId) {
        // 토큰 생성 전 설정값 로깅
        log.info("VideoSDK 토큰 생성 - API Key: {}, Secret Key 길이: {}", 
                 apiKey != null ? apiKey.substring(0, 8) + "..." : "null", 
                 secretKey != null ? secretKey.length() : "null");
        
        // VideoSDK 공식 문서에 따른 토큰 구조
        Map<String, Object> payload = new HashMap<>();
        payload.put("apikey", apiKey);
        payload.put("permissions", Arrays.asList("allow_join"));
        payload.put("version", 2);
        
        if (roomId != null && !roomId.trim().isEmpty()) {
            payload.put("roomId", roomId);
            log.info("VideoSDK 토큰에 roomId 추가: {}", roomId);
        }

        if (participantId != null && !participantId.trim().isEmpty()) {
            payload.put("participantId", participantId);
            log.info("VideoSDK 토큰에 participantId 추가: {}", participantId);
        }
        
        // roles 추가: rtc는 미팅 실행용, crawler는 v2 API용
        payload.put("roles", Arrays.asList("crawler"));
        
        // 토큰 발급 및 만료 시간 설정 (초 단위 정렬)
        long currentTimeSeconds = System.currentTimeMillis() / 1000; // 초 단위로 변환
        Date issuedAt = new Date(currentTimeSeconds * 1000);
        Date expiration = new Date((currentTimeSeconds + 6 * 60 * 60) * 1000); // 6시간 후
        
        log.info("토큰 시간 - 발급시간: {}, 만료시간: {}, 현재시간: {}", 
                issuedAt, expiration, new Date());
        
        // VideoSDK 시크릿 키를 UTF-8로 변환하여 SecretKey 생성
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        
        String token = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setClaims(payload)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        
        // 생성된 토큰의 상세 정보 로그
        log.info("VideoSDK 토큰 생성 완료 - payload: {}", payload);
        log.info("VideoSDK 토큰: {}", token);
        
        // JWT 디코딩해서 실제 내용 확인 (디버깅용)
        try {
            String[] parts = token.split("\\.");
            String header = new String(java.util.Base64.getUrlDecoder().decode(parts[0]));
            String payloadDecoded = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            log.info("JWT 헤더: {}", header);
            log.info("JWT 페이로드 디코딩: {}", payloadDecoded);
        } catch (Exception e) {
            log.warn("JWT 디코딩 실패: {}", e.getMessage());
        }
        return token;
    }
    
    /**
     * 기본 VideoSDK 토큰 생성 (roomId, participantId 없음)
     * @return JWT 토큰
     */
    public String generateToken() {
        return generateToken(null, null);
    }
    
    /**
     * roomId만 포함한 VideoSDK 토큰 생성
     * @param roomId 방 ID
     * @return JWT 토큰
     */
    public String generateTokenWithRoomId(String roomId, String uuid) {
        return generateToken(roomId, uuid);
    }
    
    /**
     * Crawler용 VideoSDK 토큰 생성 (API 호출용 - 미팅 생성)
     * @return JWT 토큰
     */
    public String generateCrawlerToken() {
        log.info("VideoSDK Crawler 토큰 생성 (API용) - API Key: {}", 
                apiKey != null ? apiKey.substring(0, 8) + "..." : "null");
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("apikey", apiKey);
        payload.put("permissions", Arrays.asList("allow_join", "allow_mod"));
        payload.put("version", 2);
        payload.put("roles", Arrays.asList("crawler")); // API 호출용
        // roomId, participantId 없음 (미팅 생성용)
        
        return signToken(payload);
    }
    
    /**
     * RTC용 VideoSDK 토큰 생성 (미팅 참가용)
     * @param meetingId 실제 VideoSDK 미팅 ID
     * @param participantId 참가자 ID
     * @return JWT 토큰
     */
    public String generateRtcToken(String meetingId, String participantId) {
        log.info("VideoSDK RTC 토큰 생성 (미팅 참가용) - API Key: {}, 미팅ID: {}", 
                apiKey != null ? apiKey.substring(0, 8) + "..." : "null", meetingId);
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("apikey", apiKey);
        payload.put("permissions", Arrays.asList("allow_join", "allow_mod"));
        payload.put("version", 2);
        payload.put("roomId", meetingId);           // 실제 미팅 ID
        payload.put("participantId", participantId);
        payload.put("roles", Arrays.asList("rtc")); // SDK 연결용
        
        return signToken(payload);
    }
    
    /**
     * VideoSDK API를 통해 실제 미팅 생성
     * @param crawlerToken Crawler 토큰
     * @param preferredRoomId 선호하는 방 ID
     * @return 생성된 실제 미팅 ID
     */
    public String createVideoSDKMeeting(String crawlerToken, String preferredRoomId) {
        try {
            log.info("VideoSDK 미팅 생성 시도 - 선호 방ID: {}", preferredRoomId);
            
            // VideoSDK API 호출을 위한 HTTP 클라이언트 설정
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            
            // 요청 바디 (빈 JSON 객체)
            String requestBody = "{}";
            
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://api.videosdk.live/v2/rooms"))
                    .header("Authorization", crawlerToken) // Bearer 없이 토큰만
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            
            java.net.http.HttpResponse<String> response = client.send(request, 
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            
            log.info("VideoSDK API 응답 - 상태코드: {}, 본문: {}", response.statusCode(), response.body());
            
            if (response.statusCode() == 200) {
                // JSON 파싱하여 roomId 추출 (간단한 문자열 파싱)
                String responseBody = response.body();
                String roomId = extractRoomId(responseBody);
                
                if (roomId != null) {
                    log.info("VideoSDK 미팅 생성 성공 - 미팅ID: {}", roomId);
                    return roomId;
                } else {
                    log.error("응답에서 roomId를 찾을 수 없음: {}", responseBody);
                }
            } else {
                log.error("VideoSDK 미팅 생성 실패 - 상태코드: {}, 응답: {}", response.statusCode(), response.body());
            }
            
        } catch (Exception e) {
            log.error("VideoSDK 미팅 생성 중 오류 발생: ", e);
        }
        
        // 실패 시 기본 방 ID 반환 (fallback)
        String fallbackRoomId = preferredRoomId != null ? preferredRoomId : "default_room";
        log.warn("미팅 생성 실패, 기본 방ID 사용: {}", fallbackRoomId);
        return fallbackRoomId;
    }
    
    /**
     * JSON 응답에서 roomId 추출
     * @param jsonResponse JSON 응답 문자열
     * @return 추출된 roomId
     */
    private String extractRoomId(String jsonResponse) {
        try {
            // 간단한 JSON 파싱 (roomId 필드 찾기)
            if (jsonResponse.contains("\"roomId\"")) {
                int startIndex = jsonResponse.indexOf("\"roomId\"") + 9; // "roomId": 이후
                startIndex = jsonResponse.indexOf("\"", startIndex) + 1; // 첫 번째 " 이후
                int endIndex = jsonResponse.indexOf("\"", startIndex); // 두 번째 " 위치
                
                if (startIndex > 0 && endIndex > startIndex) {
                    return jsonResponse.substring(startIndex, endIndex);
                }
            }
        } catch (Exception e) {
            log.error("JSON에서 roomId 추출 실패: ", e);
        }
        return null;
    }
    
    /**
     * JWT 토큰 서명 공통 메서드
     * @param payload JWT 페이로드
     * @return 서명된 JWT 토큰
     */
    private String signToken(Map<String, Object> payload) {
        long currentTimeSeconds = System.currentTimeMillis() / 1000;
        Date issuedAt = new Date(currentTimeSeconds * 1000);
        Date expiration = new Date((currentTimeSeconds + 6 * 60 * 60) * 1000); // 6시간
        
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setClaims(payload)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        
        log.info("토큰 생성 완료: {}", token);
        return token;
    }
    
}