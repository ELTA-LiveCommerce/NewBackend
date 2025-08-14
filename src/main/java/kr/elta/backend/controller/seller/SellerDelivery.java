package kr.elta.backend.controller.seller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.elta.backend.dto.DeliveryCourierDTO;
import kr.elta.backend.dto.ResponseDTO;
import kr.elta.backend.entity.*;
import kr.elta.backend.repository.DeliverRepository;
import kr.elta.backend.repository.OrderRepository;
import kr.elta.backend.repository.UserEntityRepository;
import kr.elta.backend.util.JwtHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/seller/delivery")
@Slf4j
@Tag(name = "/seller/delivery", description = "배송 관리 API")
public class SellerDelivery {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private DeliverRepository deliverRepository;

    @GetMapping("")
    public ResponseEntity<ResponseDTO> getDeliveryList(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Long sellerUuid = JwtHelper.getCurrentUserUuid();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDateTime"));
        Page<DeliveryEntity> deliveryPage = deliverRepository.findBySellerUuid(sellerUuid, pageable);
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("deliveries", deliveryPage.getContent());
        responseData.put("currentPage", deliveryPage.getNumber());
        responseData.put("totalPages", deliveryPage.getTotalPages());
        responseData.put("totalElements", deliveryPage.getTotalElements());
        responseData.put("pageSize", deliveryPage.getSize());
        responseData.put("hasNext", deliveryPage.hasNext());
        responseData.put("hasPrevious", deliveryPage.hasPrevious());
        
        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", responseData), HttpStatus.OK);
    }

    @PatchMapping("")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "not exist", content = @Content(mediaType = "")),
    })
    public ResponseEntity<ResponseDTO> changeDeliveryStatus(@RequestParam Long uuid, @RequestParam DeliveryStatus status) {
        Long sellerUuid = JwtHelper.getCurrentUserUuid();
        Optional<DeliveryEntity> deliveryEntity = deliverRepository.findBySellerUuidAndUuid(sellerUuid, uuid);
        if(deliveryEntity.isEmpty()){
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "not exist"), HttpStatus.CONFLICT);
        }

        deliveryEntity.get().setStatus(status);
        deliverRepository.save(deliveryEntity.get());
        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success"), HttpStatus.OK);
    }

    @PatchMapping("/courier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "not exist", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "partial success", content = @Content(mediaType = "")),
    })
    public ResponseEntity<ResponseDTO> changeDeliveryCourier(@Valid @RequestBody DeliveryCourierDTO deliveryCourierDTO) {
        Long sellerUuid = JwtHelper.getCurrentUserUuid();
        
        int successCount = 0;
        int failCount = 0;
        
        for (DeliveryCourierDTO.DeliveryUpdate update : deliveryCourierDTO.getDeliveries()) {
            Optional<DeliveryEntity> deliveryEntity = deliverRepository.findBySellerUuidAndUuid(sellerUuid, update.getUuid());
            
            if (deliveryEntity.isPresent()) {
                deliveryEntity.get().setCourierCompany(update.getCourierCompany());
                deliveryEntity.get().setCourierCode(update.getCourierCode());
                deliverRepository.save(deliveryEntity.get());
                successCount++;
            } else {
                failCount++;
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("totalCount", deliveryCourierDTO.getDeliveries().size());
        
        if (failCount == 0) {
            return new ResponseEntity<>(new ResponseDTO(true, "success", result), HttpStatus.OK);
        } else if (successCount > 0) {
            return new ResponseEntity<>(new ResponseDTO(true, "partial success", result), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseDTO(false, "all failed", result), HttpStatus.BAD_REQUEST);
        }
    }
}
