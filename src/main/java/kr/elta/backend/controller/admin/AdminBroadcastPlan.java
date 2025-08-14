package kr.elta.backend.controller.admin;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.elta.backend.dto.BroadcastPlanDTO;
import kr.elta.backend.dto.ResponseDTO;
import kr.elta.backend.entity.BroadcastPlanEntity;
import kr.elta.backend.repository.BroadcastPlanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/broadcastPlan")
@Slf4j
@Tag(name = "/admin/broadcastPlan", description = "어드민의 방송 플랜 관리 API")
public class AdminBroadcastPlan {
    @Autowired
    private BroadcastPlanRepository broadcastPlanRepository;

    @PostMapping("")
    public ResponseEntity<ResponseDTO> createBroadcastPlan(@Valid @RequestBody BroadcastPlanDTO broadcastPlanDTO) {
        BroadcastPlanEntity broadcastPlanEntity = BroadcastPlanEntity.builder()
                .name(broadcastPlanDTO.getName())
                .minute(broadcastPlanDTO.getMinute())
                .maxViewer(broadcastPlanDTO.getMaxViewer())
                .price(broadcastPlanDTO.getPrice())
                .isActive(broadcastPlanDTO.getIsActive())
                .displayOrder(broadcastPlanDTO.getDisplayOrder())
                .build();

        broadcastPlanRepository.save(broadcastPlanEntity);

        return new ResponseEntity<>(new ResponseDTO(true, "success"), HttpStatus.OK);
    }
}
