package kr.elta.backend.controller.seller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.elta.backend.dto.ResponseDTO;
import kr.elta.backend.entity.OrderEntity;
import kr.elta.backend.entity.OrderStatus;
import kr.elta.backend.entity.ReturnEntity;
import kr.elta.backend.entity.ReturnStatus;
import kr.elta.backend.repository.OrderRepository;
import kr.elta.backend.repository.ReturnRepository;
import kr.elta.backend.util.JwtHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/seller/return")
@Slf4j
@Tag(name = "/seller/return", description = "반품 관리 API")
public class SellerReturn {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ReturnRepository returnRepository;

    @GetMapping("")
    public ResponseEntity<ResponseDTO> getReturnList(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Long sellerUuid = JwtHelper.getCurrentUserUuid();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uuid"));
        Page<ReturnEntity> returnPage = returnRepository.findBySellerUuid(sellerUuid, pageable);
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("returns", returnPage.getContent());
        responseData.put("currentPage", returnPage.getNumber());
        responseData.put("totalPages", returnPage.getTotalPages());
        responseData.put("totalElements", returnPage.getTotalElements());
        responseData.put("pageSize", returnPage.getSize());
        responseData.put("hasNext", returnPage.hasNext());
        responseData.put("hasPrevious", returnPage.hasPrevious());
        
        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", responseData), HttpStatus.OK);
    }

    @PatchMapping("")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "not exist", content = @Content(mediaType = "")),
    })
    public ResponseEntity<ResponseDTO> changeReturnStatus(@RequestParam Long uuid, @RequestParam ReturnStatus status) {
        Long sellerUuid = JwtHelper.getCurrentUserUuid();
        Optional<ReturnEntity> returnEntity = returnRepository.findBySellerUuidAndUuid(sellerUuid, uuid);
        if (returnEntity.isEmpty()) {
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "not exist"), HttpStatus.CONFLICT);
        }

        if(status == ReturnStatus.CANCEL){
            // 반품 취소 시 주문 상태를 CANCEL로 변경
            Optional<OrderEntity> orderOpt = orderRepository.findById(returnEntity.get().getOrderUuid());
            if(orderOpt.isPresent()) {
                orderOpt.get().setStatus(OrderStatus.CANCEL_CANCEL);
                orderRepository.save(orderOpt.get());
            }
        } else if(status == ReturnStatus.DONE){
            // 반품 완료 시 주문 상태를 CANCEL_CANCEL로 변경
            Optional<OrderEntity> orderOpt = orderRepository.findById(returnEntity.get().getOrderUuid());
            if(orderOpt.isPresent()) {
                orderOpt.get().setStatus(OrderStatus.CANCEL);
                orderRepository.save(orderOpt.get());
            }
        }
        returnEntity.get().setStatus(status);
        returnRepository.save(returnEntity.get());
        
        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", returnEntity.get().getUuid()), HttpStatus.OK);
    }
}
