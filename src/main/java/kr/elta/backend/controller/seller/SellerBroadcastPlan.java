package kr.elta.backend.controller.seller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.elta.backend.dto.ResponseDTO;
import kr.elta.backend.entity.BroadcastEntity;
import kr.elta.backend.entity.BroadcastPlanEntity;
import kr.elta.backend.entity.SellerBroadcastPlanEntity;
import kr.elta.backend.entity.UserEntity;
import kr.elta.backend.repository.BroadcastPlanRepository;
import kr.elta.backend.repository.BroadcastRepository;
import kr.elta.backend.repository.SellerBroadcastPlanRepository;
import kr.elta.backend.repository.UserEntityRepository;
import kr.elta.backend.util.JwtHelper;
import kr.elta.backend.util.PointTransactionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/seller/broadcastPlan")
@Slf4j
@Tag(name = "/seller/broadcastPlan", description = "방송 플랜 관리 API")
public class SellerBroadcastPlan {
    @Autowired
    private BroadcastPlanRepository broadcastPlanRepository;
    
    @Autowired
    private SellerBroadcastPlanRepository sellerBroadcastPlanRepository;
    
    @Autowired
    private UserEntityRepository userEntityRepository;
    @Autowired
    private BroadcastRepository broadcastRepository;

    @GetMapping("")
    public ResponseEntity<ResponseDTO> getBroadcastPlan() {
        List<BroadcastPlanEntity> broadcastPlanEntityList = broadcastPlanRepository.findAll();

        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", broadcastPlanEntityList), HttpStatus.OK);
    }

    @PostMapping("")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "not exist plan", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "not exist user", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "insufficient balance", content = @Content(mediaType = "")),
    })
    public ResponseEntity<ResponseDTO> buyBroadcastPlan(@RequestParam Long broadcastPlanUuid) {
        Long sellerUuid = JwtHelper.getCurrentUserUuid();
        Optional<BroadcastPlanEntity> broadcastPlanOpt = broadcastPlanRepository.findById(broadcastPlanUuid);
        if (broadcastPlanOpt.isEmpty() || !broadcastPlanOpt.get().getIsActive()) {
            return new ResponseEntity<>(new ResponseDTO(false, "not exist plan"), HttpStatus.NOT_FOUND);
        }
        
        Optional<UserEntity> userOpt = userEntityRepository.findById(sellerUuid);
        if (userOpt.isEmpty()) {
            return new ResponseEntity<>(new ResponseDTO(false, "not exist user"), HttpStatus.NOT_FOUND);
        }else if (userOpt.get().getBalance() < broadcastPlanOpt.get().getPrice()) {
            return new ResponseEntity<>(new ResponseDTO(false, "insufficient balance"), HttpStatus.BAD_REQUEST);
        }

        BroadcastPlanEntity broadcastPlan = broadcastPlanOpt.get();
        userOpt.get().setBalance(userOpt.get().getBalance() - broadcastPlan.getPrice());
        userEntityRepository.save(userOpt.get());
        
        PointTransactionUtil.recordTransaction(sellerUuid, -broadcastPlan.getPrice(), "방송 플랜 구매: " + broadcastPlan.getName());
        Optional<SellerBroadcastPlanEntity> existingPlanOpt = sellerBroadcastPlanRepository.findBySellerUuid(sellerUuid);
        if (existingPlanOpt.isPresent()) {
            SellerBroadcastPlanEntity existingPlan = existingPlanOpt.get();
            existingPlan.setPlanUuid(broadcastPlanUuid);
            existingPlan.setMaxViewer(broadcastPlanOpt.get().getMaxViewer());
            existingPlan.setRemainMinute(existingPlan.getRemainMinute() + broadcastPlanOpt.get().getMinute());
            sellerBroadcastPlanRepository.save(existingPlan);
        } else {
            SellerBroadcastPlanEntity sellerBroadcastPlan = SellerBroadcastPlanEntity.builder()
                    .sellerUuid(sellerUuid)
                    .planUuid(broadcastPlanUuid)
                    .remainMinute(broadcastPlanOpt.get().getMinute())
                    .maxViewer(broadcastPlanOpt.get().getMaxViewer())
                    .build();
            
            sellerBroadcastPlanRepository.save(sellerBroadcastPlan);
        }

        List<BroadcastEntity> activeOrNotStartedBroadcasts = broadcastRepository.findAllByEndDatetimeIsNullAndSellerUuid(sellerUuid);
        int updatedBroadcastCount = 0;
        if (!activeOrNotStartedBroadcasts.isEmpty()) {
            int newMaxViewer = broadcastPlan.getMaxViewer();
            for (BroadcastEntity broadcast : activeOrNotStartedBroadcasts) {
                broadcast.setMaxViewer(newMaxViewer);
                broadcastRepository.save(broadcast);
                updatedBroadcastCount++;
            }
        }
        
        Map<String, Object> responseData = Map.of(
            "message", "방송 플랜 구매 완료",
            "hasActiveBroadcast", !activeOrNotStartedBroadcasts.isEmpty(),
            "activeBroadcastCount", activeOrNotStartedBroadcasts.size(),
            "updatedBroadcastCount", updatedBroadcastCount,
            "newMaxViewer", broadcastPlan.getMaxViewer()
        );

        return new ResponseEntity<>(new ResponseDTO(true, "success", responseData), HttpStatus.OK);
    }

    @GetMapping("/status")
    public ResponseEntity<ResponseDTO> getMyBroadcastPlan() {
        Long sellerUuid = JwtHelper.getCurrentUserUuid();
        
        Optional<SellerBroadcastPlanEntity> activePlanOpt = sellerBroadcastPlanRepository
                .findBySellerUuid(sellerUuid);
        
        if (activePlanOpt.isEmpty()) {
            Map<String, Object> response = Map.of(
                    "hasActivePlan", false,
                    "remainingMinutes", 0,
                    "maxViewers", 0,
                    "planName", ""
            );
            return new ResponseEntity<>(new ResponseDTO(true, "success", response), HttpStatus.OK);
        }
        
        Optional<BroadcastPlanEntity> broadcastPlanOpt = broadcastPlanRepository.findById(activePlanOpt.get().getPlanUuid());
        String planName = broadcastPlanOpt.map(BroadcastPlanEntity::getName).orElse("");
        
        Map<String, Object> response = Map.of(
                "hasActivePlan", true,
                "remainingMinutes", activePlanOpt.get().getRemainMinute(),
                "maxViewers", activePlanOpt.get().getMaxViewer(),
                "planName", planName
        );
        
        return new ResponseEntity<>(new ResponseDTO(true, "success", response), HttpStatus.OK);
    }
}
