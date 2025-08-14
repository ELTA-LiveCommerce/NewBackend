package kr.elta.backend.controller.seller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.elta.backend.dto.ProductDTO;
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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/seller/order")
@Slf4j
@Tag(name = "/seller/order", description = "주문 관리 API")
public class SellerOrder {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private DeliverRepository deliverRepository;
    @Autowired
    private UserEntityRepository userEntityRepository;

    @GetMapping("")
    public ResponseEntity<ResponseDTO> getOrderList(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Long sellerUuid = JwtHelper.getCurrentUserUuid();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDateTime"));
        Page<OrderEntity> orderPage = orderRepository.findAllBySellerUuid(sellerUuid, pageable);
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("orders", orderPage.getContent());
        responseData.put("currentPage", orderPage.getNumber());
        responseData.put("totalPages", orderPage.getTotalPages());
        responseData.put("totalElements", orderPage.getTotalElements());
        responseData.put("pageSize", orderPage.getSize());
        responseData.put("hasNext", orderPage.hasNext());
        responseData.put("hasPrevious", orderPage.hasPrevious());
        
        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", responseData), HttpStatus.OK);
    }

    @PatchMapping("")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "not exist", content = @Content(mediaType = "")),
    })
    public ResponseEntity<ResponseDTO> changeOrderStatus(@RequestParam Long uuid, @RequestParam OrderStatus status) {
        Long sellerUuid = JwtHelper.getCurrentUserUuid();
        Optional<OrderEntity> orderEntity = orderRepository.findBySellerUuidAndUuid(sellerUuid, uuid);
        if (orderEntity.isEmpty()) {
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "not exist"), HttpStatus.CONFLICT);
        }

        if(status == OrderStatus.DONE){
            DeliveryEntity deliveryEntity = DeliveryEntity.builder()
                    .orderUuid(orderEntity.get().getUuid())
                    .sellerUuid(sellerUuid)
                    .productName(orderEntity.get().getProductName())
                    .option(orderEntity.get().getOption())
                    .orderDateTime(LocalDateTime.now())
                    .name(orderEntity.get().getName())
                    .phoneNum(orderEntity.get().getPhoneNum())
                    .address(orderEntity.get().getAddress())
                    .status(DeliveryStatus.READY)
                    .build();
            deliverRepository.save(deliveryEntity);
        }

        orderEntity.get().setStatus(status);
        orderRepository.save(orderEntity.get());
        
        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", orderEntity.get().getUuid()), HttpStatus.OK);
    }
}
