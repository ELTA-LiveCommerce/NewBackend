package kr.elta.backend.controller.viewer;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import kr.elta.backend.dto.ProductDTO;
import kr.elta.backend.dto.ResponseDTO;
import kr.elta.backend.entity.*;
import kr.elta.backend.repository.BroadcastRepository;
import kr.elta.backend.repository.FollowRepository;
import kr.elta.backend.repository.ProductRepository;
import kr.elta.backend.repository.UserEntityRepository;
import kr.elta.backend.util.JwtHelper;
import kr.elta.backend.util.KakaoNotificationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.scheduling.annotation.Async;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/viewer/seller")
@Slf4j
@Tag(name = "/viewer/seller", description = "판매자 관리 API")
public class ViewerSeller {
    @Autowired
    private FollowRepository followRepository;
    @Autowired
    private UserEntityRepository userEntityRepository;
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private KakaoNotificationUtil kakaoNotificationUtil;
    @Autowired
    private BroadcastRepository broadcastRepository;

    @GetMapping("")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "not exist", content = @Content(mediaType = "")),
    })
    public ResponseEntity<ResponseDTO> findSeller(@RequestParam(value = "urlName", required = false) String urlName, @RequestParam(value = "uuid", required = false) Long uuid) {
        Optional<UserEntity> userEntity = Optional.empty();
        // uuid가 있으면 uuid로 검색, 없으면 urlName으로 검색 (탈퇴한 사용자 제외)
        if (uuid != null) {
            userEntity = userEntityRepository.findByRoleAndUuidAndDeleteDateTimeIsNull(Role.SELLER, uuid);
        } else if (urlName != null) {
            userEntity = userEntityRepository.findByRoleAndUrlNameAndDeleteDateTimeIsNull(Role.SELLER, urlName);
        } else {
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "parameter was not provided"), HttpStatus.BAD_REQUEST);
        }
        
        if(userEntity.isEmpty()){
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", new ArrayList<>()), HttpStatus.OK);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> attribute = new HashMap<>();
        attribute.put("uuid", userEntity.get().getUuid());
        attribute.put("urlName", userEntity.get().getName());
        attribute.put("profileImg", userEntity.get().getProfileImg());
        attribute.put("bannerImg", userEntity.get().getBannerImg());
        attribute.put("businessName", userEntity.get().getBusinessName());
        attribute.put("businessAddress", userEntity.get().getBusinessAddress());
        attribute.put("businessNumber", userEntity.get().getBusinessNumber());
        attribute.put("businessTime", userEntity.get().getBusinessTime());
        attribute.put("description", userEntity.get().getDescription());
        attribute.put("follower", followRepository.countAllByFollowingUuid(userEntity.get().getUuid()));
        attribute.put("isFollow", followRepository.existsByFollowingUuidAndFollowerUuid(userEntity.get().getUuid(), Long.parseLong(auth.getName())));
        attribute.put("live", broadcastRepository.findAllBySellerUuidAndStartDatatimeIsNotNullAndEndDatetimeIsNull(userEntity.get().getUuid()));
        attribute.put("product", productRepository.findByUserUuidAndIsPublicIsTrue(userEntity.get().getUuid()));
        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", attribute), HttpStatus.OK);
    }

    @PostMapping("")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "not exist", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "already exist", content = @Content(mediaType = "")),
    })
    public ResponseEntity<ResponseDTO> reviewSeller() {
        Long userUuid = JwtHelper.getCurrentUserUuid();

        Optional<UserEntity> userEntity = userEntityRepository.findById(userUuid);
        if(userEntity.isEmpty()){
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "not exist"), HttpStatus.CONFLICT);
        }else if(userEntity.get().getIsReviewing()){
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "already exist"), HttpStatus.CONFLICT);
        }

        userEntity.get().setIsReviewing(true);
        userEntityRepository.save(userEntity.get());
        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success"), HttpStatus.OK);
    }

    @PostMapping("/follow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "not exist", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "already exist", content = @Content(mediaType = "")),
    })
    public ResponseEntity<ResponseDTO> followSeller(@RequestParam("uuid") Long uuid) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(!userEntityRepository.existsByRoleAndUuid(Role.SELLER, uuid)) {
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "not exist"), HttpStatus.CONFLICT);
        }else if(followRepository.existsByFollowingUuidAndFollowerUuid(uuid, Long.parseLong(auth.getName()))){
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "already exist"), HttpStatus.CONFLICT);
        }

        FollowEntity followEntity = new FollowEntity().builder()
                .followerUuid(Long.parseLong(auth.getName()))
                .followingUuid(uuid)
                .build();
        followRepository.save(followEntity);

        // 팔로우 알림톡 전송 - @Async로 비동기 처리
        sendFollowNotification(uuid, Long.parseLong(auth.getName()));

        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", followEntity.getUuid()), HttpStatus.OK);
    }

    @PostMapping("/unfollow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "not exist", content = @Content(mediaType = "")),
    })
    @Transactional
    public ResponseEntity<ResponseDTO> unFollowSeller(@RequestParam("uuid") Long uuid) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(!followRepository.existsByFollowingUuidAndFollowerUuid(uuid, Long.parseLong(auth.getName()))){
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "not exist"), HttpStatus.CONFLICT);
        }

        followRepository.deleteByFollowingUuidAndFollowerUuid(uuid, Long.parseLong(auth.getName()));
        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success"), HttpStatus.OK);
    }

    @GetMapping("/product")
    public ResponseEntity<ResponseDTO> getProductForSale(@RequestParam("uuid") Long uuid) {
        Optional<UserEntity> userEntity = userEntityRepository.findAllByRoleAndUuid(Role.SELLER, uuid);

        if(userEntity.isEmpty()){
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", new ArrayList<>()), HttpStatus.OK);
        }

        List<ProductEntity> productEntityList = productRepository.findAllByIsPublicAndUserUuid(true, uuid);
        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", productEntityList), HttpStatus.OK);
    }
    
    /**
     * 팔로우 알림톡 비동기 전송
     * 
     * @param sellerUuid 판매자 UUID
     * @param followerUuid 팔로워 UUID
     */
    @Async
    public void sendFollowNotification(Long sellerUuid, Long followerUuid) {
        try {
            Optional<UserEntity> sellerEntity = userEntityRepository.findById(sellerUuid);
            Optional<UserEntity> viewerEntity = userEntityRepository.findById(followerUuid);
            
            if (sellerEntity.isPresent() && viewerEntity.isPresent() && sellerEntity.get().getPhoneNum() != null) {
                boolean success = kakaoNotificationUtil.sendFollowNotification(
                    viewerEntity.get().getName(), 
                    sellerEntity.get().getName(), 
                    sellerEntity.get().getPhoneNum()
                );
                
                if (success) {
                    log.info("팔로우 알림톡 전송 완료 - 팔로워: {}, 판매자: {}", 
                            viewerEntity.get().getName(), sellerEntity.get().getName());
                } else {
                    log.error("팔로우 알림톡 전송 실패 - 팔로워: {}, 판매자: {}", 
                            viewerEntity.get().getName(), sellerEntity.get().getName());
                }
            } else {
                log.warn("팔로우 알림톡 전송 조건 미충족 - sellerUuid: {}, followerUuid: {}", sellerUuid, followerUuid);
            }
        } catch (Exception e) {
            log.error("팔로우 알림톡 전송 중 오류 발생 - sellerUuid: {}, followerUuid: {}", sellerUuid, followerUuid, e);
        }
    }
}