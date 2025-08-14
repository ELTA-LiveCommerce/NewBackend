package kr.elta.backend.controller.viewer;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.elta.backend.dto.ResponseDTO;
import kr.elta.backend.entity.BroadcastEntity;
import kr.elta.backend.repository.BroadcastRepository;
import kr.elta.backend.util.JwtHelper;
import kr.elta.backend.util.RedisUtil;
import kr.elta.backend.util.VideoSDKUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/viewer/live")
@Slf4j
@Tag(name = "/viewer/live", description = "시청자 라이브 방송 API")
public class ViewerLive {
    
    @Autowired
    private VideoSDKUtil videoSDKUtil;
    
    @Autowired
    private BroadcastRepository broadcastRepository;
    
    @Autowired
    private RedisUtil redisUtil;
    
    @GetMapping("/join/{broadcastUuid}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "not exist", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "not live", content = @Content(mediaType = "")),
    })
    public ResponseEntity<ResponseDTO> joinBroadcast(@PathVariable("broadcastUuid") Long broadcastUuid) {
        Long viewerUuid = JwtHelper.getCurrentUserUuid();
        
        // 방송 존재 여부 확인
        Optional<BroadcastEntity> broadcastOpt = broadcastRepository.findById(broadcastUuid);
        if (broadcastOpt.isEmpty()) {
            return new ResponseEntity<>(new ResponseDTO(false, "not exist"), HttpStatus.NOT_FOUND);
        }
        
        BroadcastEntity broadcast = broadcastOpt.get();
        
        // 방송 중인지 확인
        if (broadcast.getStartDatatime() == null || broadcast.getEndDatetime() != null) {
            return new ResponseEntity<>(new ResponseDTO(false, "not live"), HttpStatus.BAD_REQUEST);
        }
        
        // 실제 미팅 ID 확인
        String actualMeetingId = broadcast.getActualMeetingId();
        if (actualMeetingId == null || actualMeetingId.trim().isEmpty()) {
            return new ResponseEntity<>(new ResponseDTO(false, "meeting not ready"), HttpStatus.BAD_REQUEST);
        }
        
        // VideoSDK RTC 토큰 생성 (실제 미팅 ID 사용)
        String participantId = "viewer_" + viewerUuid;
        String rtcToken = videoSDKUtil.generateRtcToken(actualMeetingId, participantId);
        
        // Redis에서 방송 데이터 조회
        Object redisBroadcastData = redisUtil.get("broadcast:" + broadcastUuid);
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("rtcToken", rtcToken);
        responseData.put("meetingId", actualMeetingId);
        responseData.put("participantId", participantId);
        responseData.put("broadcastTitle", broadcast.getTitle());
        responseData.put("sellerUuid", broadcast.getSellerUuid());
        responseData.put("broadcastData", redisBroadcastData); // Redis 방송 데이터 추가
        responseData.put("hlsUrl", broadcast.getHlsUrl()); // HLS URL 추가
        
        return new ResponseEntity<>(new ResponseDTO(true, "success", responseData), HttpStatus.OK);
    }
}