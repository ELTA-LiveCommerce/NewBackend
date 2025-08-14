package kr.elta.backend.controller.viewer;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.elta.backend.dto.OrderDTO;
import kr.elta.backend.dto.ProductDTO;
import kr.elta.backend.dto.ResponseDTO;
import kr.elta.backend.entity.*;
import kr.elta.backend.repository.DeliverRepository;
import kr.elta.backend.repository.OrderRepository;
import kr.elta.backend.repository.ProductRepository;
import kr.elta.backend.repository.ReturnRepository;
import kr.elta.backend.repository.UserEntityRepository;
import kr.elta.backend.util.JwtHelper;
import kr.elta.backend.util.RedisUtil;
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
@RequestMapping("/viewer/order")
@Slf4j
@Tag(name = "/viewer/order", description = "주문 관리 API")
public class ViewerOrder {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserEntityRepository userEntityRepository;
    @Autowired
    private ReturnRepository returnRepository;
    @Autowired
    private DeliverRepository deliverRepository;
    @Autowired
    private RedisUtil redisUtil;

    @PostMapping("")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "not exist user", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "not exist product", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "insufficient stock", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "option not exist", content = @Content(mediaType = "")),
    })
    public ResponseEntity<ResponseDTO> orderProduct(@Valid @RequestBody OrderDTO orderDTO) {
        Long userUuid = JwtHelper.getCurrentUserUuid();
        Optional<ProductEntity> productEntity = productRepository.findById(orderDTO.getUuid());
        if(productEntity.isEmpty()){
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "not exist product"), HttpStatus.CONFLICT);
        }

        for (var orderOption : orderDTO.getOption()) {
            boolean optionExists = false;
            for (var productOption : productEntity.get().getOption()) {
                if (productOption.getName().equals(orderOption.getName())) {
                    optionExists = true;
                    if (productOption.getQuantity() < orderOption.getQuantity()) {
                        return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "insufficient stock"), HttpStatus.CONFLICT);
                    }
                    productOption.setQuantity(productOption.getQuantity() - orderOption.getQuantity());
                    break;
                }
            }
            if (!optionExists) {
                return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "option not exist"), HttpStatus.CONFLICT);
            }
        }
        productRepository.save(productEntity.get());

        if (orderDTO.getBroadcastUuid() != null) {
            updateBroadcastData(orderDTO);
        }

        Optional<UserEntity> userEntity = Optional.ofNullable(userEntityRepository.findByUuid(userUuid));
        if(userEntity.isEmpty()){
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "not exist user"), HttpStatus.CONFLICT);
        }

        // 총 주문 수량 계산
        int totalQuantity = orderDTO.getOption().stream()
                .mapToInt(option -> option.getQuantity())
                .sum();
        
        // 총 주문 금액 = 상품 가격 × 총 수량
        int totalPrice = productEntity.get().getPrice() * totalQuantity;
        
        OrderEntity orderEntity = OrderEntity.builder()
                .sellerUuid(productEntity.get().getUserUuid())
                .buyerUuid(userUuid)
                .productUuid(orderDTO.getUuid())
                .name(userEntity.get().getName())
                .phoneNum(userEntity.get().getPhoneNum())
                .productName(productEntity.get().getName())
                .option(orderDTO.getOption())
                .price(totalPrice)
                .address(orderDTO.getAddress() == "" ? userEntity.get().getAddress() : orderDTO.getAddress())
                .status(OrderStatus.WAITING)
                .broadcastUuid(orderDTO.getBroadcastUuid())
                .orderDateTime(LocalDateTime.now())
                .build();
        orderRepository.save(orderEntity);

        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", orderEntity.getUuid()), HttpStatus.OK);
    }

    @GetMapping("")
    public ResponseEntity<ResponseDTO> getMyOrder(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Long uuid = JwtHelper.getCurrentUserUuid();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDateTime"));
        Page<OrderEntity> orderPage = orderRepository.findAllByBuyerUuid(uuid, pageable);
        
        // OrderEntity를 Map으로 변환하면서 배송 정보 추가
        var orderList = orderPage.getContent().stream().map(order -> {
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("uuid", order.getUuid());
            orderMap.put("sellerUuid", order.getSellerUuid());
            orderMap.put("buyerUuid", order.getBuyerUuid());
            orderMap.put("productUuid", order.getProductUuid());
            orderMap.put("name", order.getName());
            orderMap.put("phoneNum", order.getPhoneNum());
            orderMap.put("productName", order.getProductName());
            orderMap.put("option", order.getOption());
            orderMap.put("price", order.getPrice());
            orderMap.put("address", order.getAddress());
            orderMap.put("status", order.getStatus());
            orderMap.put("broadcastUuid", order.getBroadcastUuid());
            orderMap.put("orderDateTime", order.getOrderDateTime());
            
            // status가 DONE인 경우 배송 정보 추가
            if (order.getStatus() == OrderStatus.DONE) {
                var deliveryOpt = deliverRepository.findByOrderUuid(order.getUuid());
                if (deliveryOpt.isPresent()) {
                    var delivery = deliveryOpt.get();
                    orderMap.put("courierCompany", delivery.getCourierCompany());
                    orderMap.put("courierCode", delivery.getCourierCode());
                    orderMap.put("deliveryStatus", delivery.getStatus());
                }
            }
            
            return orderMap;
        }).toList();
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("orders", orderList);
        responseData.put("currentPage", orderPage.getNumber());
        responseData.put("totalPages", orderPage.getTotalPages());
        responseData.put("totalElements", orderPage.getTotalElements());
        responseData.put("pageSize", orderPage.getSize());
        responseData.put("hasNext", orderPage.hasNext());
        responseData.put("hasPrevious", orderPage.hasPrevious());
        
        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", responseData), HttpStatus.OK);
    }

    @PostMapping("/cancel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "not exist order", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "not your order", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "already cancel", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "not exist user", content = @Content(mediaType = "")),
    })
    public ResponseEntity<ResponseDTO> cancelOrder(@RequestParam Long uuid, @RequestParam String reason) {
        Long buyerUuid = JwtHelper.getCurrentUserUuid();
        Optional<OrderEntity> orderEntity = orderRepository.findById(uuid);
        if (orderEntity.isEmpty()) {
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "not exist order"), HttpStatus.CONFLICT);
        }else if (!orderEntity.get().getBuyerUuid().equals(buyerUuid)) {
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "not your order"), HttpStatus.CONFLICT);
        }else if(orderEntity.get().getStatus() == OrderStatus.CANCEL || orderEntity.get().getStatus() == OrderStatus.CANCEL_REQUEST || orderEntity.get().getStatus() == OrderStatus.CANCEL_CANCEL){
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "already cancel"), HttpStatus.CONFLICT);
        }
        
        Optional<UserEntity> userEntity = Optional.ofNullable(userEntityRepository.findByUuid(buyerUuid));
        if (userEntity.isEmpty()) {
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "not exist user"), HttpStatus.CONFLICT);
        }
        
        ReturnEntity returnEntity = ReturnEntity.builder()
                .orderUuid(uuid)
                .sellerUuid(orderEntity.get().getSellerUuid())
                .name(userEntity.get().getName())
                .productName(orderEntity.get().getProductName())
                .option(orderEntity.get().getOption())
                .price(orderEntity.get().getPrice())
                .accountNum(userEntity.get().getAccountNum())
                .accountType(userEntity.get().getAccountType())
                .reason(reason)
                .status(ReturnStatus.REQUEST)
                .returnDateTime(LocalDateTime.now())
                .build();
        returnRepository.save(returnEntity);
        
        orderEntity.get().setStatus(OrderStatus.CANCEL_REQUEST);
        orderRepository.save(orderEntity.get());
        
        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", returnEntity.getUuid()), HttpStatus.OK);
    }
    
    private void updateBroadcastData(OrderDTO orderDTO) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> broadcastData = (Map<String, Object>) redisUtil.get("broadcast:" + orderDTO.getBroadcastUuid());
            if (broadcastData == null) return;
            
            updateProductQuantities(broadcastData.get("currentProduct"), orderDTO);
            updateProductsArrayQuantities((java.util.List<?>) broadcastData.get("product"), orderDTO);
            
            redisUtil.set("broadcast:" + orderDTO.getBroadcastUuid(), broadcastData, 24, java.util.concurrent.TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("Redis broadcast update failed: {}", e.getMessage());
        }
    }
    
    private void updateProductQuantities(Object productObj, OrderDTO orderDTO) {
        if (!(productObj instanceof Map)) return;
        @SuppressWarnings("unchecked")
        Map<String, Object> product = (Map<String, Object>) productObj;
        if (isTargetProduct(product, orderDTO.getUuid())) {
            updateOptionQuantities(product.get("option"), orderDTO);
        }
    }
    
    private void updateProductsArrayQuantities(java.util.List<?> products, OrderDTO orderDTO) {
        if (products == null) return;
        for (Object productObj : products) {
            if (productObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> product = (Map<String, Object>) productObj;
                if (isTargetProduct(product, orderDTO.getUuid())) {
                    updateOptionQuantities(product.get("option"), orderDTO);
                    break;
                }
            }
        }
    }
    
    private boolean isTargetProduct(Map<String, Object> product, Long targetUuid) {
        Object uuidObj = product.get("uuid");
        return (uuidObj instanceof Long && targetUuid.equals(uuidObj)) ||
               (uuidObj instanceof java.util.List && ((java.util.List<?>) uuidObj).size() >= 2 && 
                targetUuid.equals(((java.util.List<?>) uuidObj).get(1)));
    }
    
    private void updateOptionQuantities(Object optionObj, OrderDTO orderDTO) {
        if (!(optionObj instanceof java.util.List)) return;
        @SuppressWarnings("unchecked")
        java.util.List<ProductDTO.Option> options = (java.util.List<ProductDTO.Option>) optionObj;
        
        orderDTO.getOption().forEach(orderOption -> 
            options.stream()
                .filter(broadcastOption -> orderOption.getName().equals(broadcastOption.getName()))
                .findFirst()
                .ifPresent(broadcastOption -> {
                    int newQuantity = broadcastOption.getQuantity() - orderOption.getQuantity();
                    broadcastOption.setQuantity(newQuantity);
                })
        );
    }
}
