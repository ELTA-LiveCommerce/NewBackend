package kr.elta.backend.controller.seller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import kr.elta.backend.dto.BroadcastAddProductDTO;
import kr.elta.backend.dto.BroadcastAnnouncementDTO;
import kr.elta.backend.dto.BroadcastCreateDTO;
import kr.elta.backend.dto.BroadcastDeleteDTO;
import kr.elta.backend.dto.BroadcastDiscountDTO;
import kr.elta.backend.dto.ProductDTO;
import kr.elta.backend.dto.ResponseDTO;
import kr.elta.backend.entity.BroadcastEntity;
import kr.elta.backend.entity.FollowEntity;
import kr.elta.backend.entity.ProductEntity;
import kr.elta.backend.entity.SellerBroadcastPlanEntity;
import kr.elta.backend.entity.UserEntity;
import kr.elta.backend.repository.BroadcastRepository;
import kr.elta.backend.repository.FollowRepository;
import kr.elta.backend.repository.OrderRepository;
import kr.elta.backend.repository.ProductRepository;
import kr.elta.backend.repository.SellerBroadcastPlanRepository;
import kr.elta.backend.repository.UserEntityRepository;
import kr.elta.backend.util.JwtHelper;
import kr.elta.backend.util.KakaoNotificationUtil;
import kr.elta.backend.util.RedisUtil;
import kr.elta.backend.util.VideoSDKUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.scheduling.annotation.Async;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import kr.elta.backend.entity.OrderEntity;
import kr.elta.backend.entity.BankType;

@RestController
@RequestMapping("/seller/live")
@Slf4j
@Tag(name = "/seller/live", description = "방송 관리 API")
public class SellerLive {
    @Autowired
    private UserEntityRepository userEntityRepository;
    
    @Autowired
    private BroadcastRepository broadcastRepository;
    
    @Autowired
    private SellerBroadcastPlanRepository sellerBroadcastPlanRepository;

    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private FollowRepository followRepository;
    
    @Autowired
    private KakaoNotificationUtil kakaoNotificationUtil;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private VideoSDKUtil videoSDKUtil;

    @GetMapping("")
    @Operation(summary = "방송 목록 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "방송 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content(mediaType = ""))
    })
    public ResponseEntity<ResponseDTO> getBroadcast(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        Long sellerUuid = JwtHelper.getCurrentUserUuid();
        Optional<UserEntity> userOpt = userEntityRepository.findById(sellerUuid);
        if (userOpt.isEmpty()) {
            return new ResponseEntity<>(new ResponseDTO(false, "사용자를 찾을 수 없습니다"), HttpStatus.CONFLICT);
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<BroadcastEntity> broadcastPage = broadcastRepository.findBySellerUuidOrderByUuidDesc(sellerUuid, pageable);

        Map<String, Object> response = Map.of(
                "broadcasts", broadcastPage.getContent(),
                "currentPage", broadcastPage.getNumber(),
                "totalPages", broadcastPage.getTotalPages(),
                "totalElements", broadcastPage.getTotalElements(),
                "pageSize", broadcastPage.getSize(),
                "hasNext", broadcastPage.hasNext(),
                "hasPrevious", broadcastPage.hasPrevious(),
                "isFirst", broadcastPage.isFirst(),
                "isLast", broadcastPage.isLast()
        );
        return new ResponseEntity<>(new ResponseDTO(true, "success", response), HttpStatus.OK);
    }

    @PostMapping("")
    @Operation(summary = "방송 생성", description = "새로운 방송을 생성합니다. 판매자는 활성화된 방송 플랜이 있어야 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "not exist plan", content = @Content(mediaType = "")),
    })
    public ResponseEntity<ResponseDTO> createBroadcast(@Valid @RequestBody BroadcastCreateDTO broadcastCreateDTO) {
        Long sellerUuid = JwtHelper.getCurrentUserUuid();
        Optional<SellerBroadcastPlanEntity> sellerPlanOpt = sellerBroadcastPlanRepository.findBySellerUuid(sellerUuid);
        if (sellerPlanOpt.isEmpty()) {
            return new ResponseEntity<>(new ResponseDTO(false, "not exist plan"), HttpStatus.CONFLICT);
        }
        
        BroadcastEntity newBroadcast = BroadcastEntity.builder()
                .title(broadcastCreateDTO.getTitle())
                .sellerUuid(sellerUuid)
                .thumbnailUrl(broadcastCreateDTO.getThumbnailUrl())
                .scheduledDatatime(broadcastCreateDTO.getScheduledDatetime())
                .description(broadcastCreateDTO.getDescription())
                .maxViewer(sellerPlanOpt.get().getMaxViewer())
                .shippingFee(broadcastCreateDTO.getShippingFee())
                .productUuidList(broadcastCreateDTO.getProductUuidList())
                .build();
        BroadcastEntity savedBroadcast = broadcastRepository.save(newBroadcast);
        return new ResponseEntity<>(new ResponseDTO(true, "success", savedBroadcast.getUuid()), HttpStatus.OK);
    }

    @PatchMapping("")
    @Operation(summary = "방송 수정", description = "방송을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "not exist", content = @Content(mediaType = "")),
    })
    public ResponseEntity<ResponseDTO> updateBroadcast(@Valid @RequestBody BroadcastCreateDTO broadcastCreateDTO) {
        Long sellerUuid = JwtHelper.getCurrentUserUuid();
        Optional<BroadcastEntity> broadcastEntity = broadcastRepository.findBySellerUuidAndUuid(sellerUuid, broadcastCreateDTO.getUuid());
        if (broadcastEntity.isEmpty()) {
            return new ResponseEntity<>(new ResponseDTO(false, "not exist"), HttpStatus.CONFLICT);
        }

        broadcastEntity.ifPresent(entity -> {
            entity.setTitle(broadcastCreateDTO.getTitle());
            entity.setThumbnailUrl(broadcastCreateDTO.getThumbnailUrl());
            entity.setScheduledDatatime(broadcastCreateDTO.getScheduledDatetime());
            entity.setDescription(broadcastCreateDTO.getDescription());
            entity.setProductUuidList(broadcastCreateDTO.getProductUuidList());
            entity.setShippingFee(broadcastCreateDTO.getShippingFee());
            broadcastRepository.save(entity);
        });
        return new ResponseEntity<>(new ResponseDTO(true, "success"), HttpStatus.OK);
    }

    @DeleteMapping("")
    @Operation(summary = "방송 다중 제거", description = "1개 이상의 방송을 제거합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
    })
    @Transactional
    public ResponseEntity<ResponseDTO> deleteBroadcast(@Valid @RequestBody BroadcastDeleteDTO broadcastDeleteDTO) {
        Long sellerUuid = JwtHelper.getCurrentUserUuid();

        for(int i = 0; i < broadcastDeleteDTO.getBroadcastUuidList().toArray().length; i++){
            broadcastRepository.deleteBySellerUuidAndUuid(sellerUuid, broadcastDeleteDTO.getBroadcastUuidList().get(i));
        }
        return new ResponseEntity<>(new ResponseDTO(true, "success"), HttpStatus.OK);
    }

    @PostMapping("/start")
    @Operation(summary = "방송 시작", description = "방송을 시작합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "not exist", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "not exist product", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "not broadcastPlan", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "not remainMinute", content = @Content(mediaType = "")),
    })
    @Transactional
    public ResponseEntity<ResponseDTO> startBroadcast(@RequestParam("uuid") Long uuid) {
        Long sellerUuid = JwtHelper.getCurrentUserUuid();

        Optional<SellerBroadcastPlanEntity> sellerBroadcastPlanEntity = sellerBroadcastPlanRepository.findBySellerUuid(sellerUuid);
        if(sellerBroadcastPlanEntity.isEmpty()){
            return new ResponseEntity<>(new ResponseDTO(false, "not exist broadcastPlan"), HttpStatus.CONFLICT);
        }else if(sellerBroadcastPlanEntity.get().getRemainMinute() < 1){
            return new ResponseEntity<>(new ResponseDTO(false, "insufficient remainMinute"), HttpStatus.CONFLICT);
        }

        Optional<BroadcastEntity> broadcastEntity = broadcastRepository.findBySellerUuidAndUuidAndStartDatatimeIsNull(sellerUuid, uuid);
        if(broadcastEntity.isEmpty()){
            return new ResponseEntity<>(new ResponseDTO(false, "not exist"), HttpStatus.CONFLICT);
        }
        broadcastEntity.get().setStartDatatime(LocalDateTime.now());
        broadcastRepository.save(broadcastEntity.get());

        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> productList = new ArrayList<>();
        
        // Product 조회 최적화 (N+1 쿼리 방지)
        List<Long> productUuids = broadcastEntity.get().getProductUuidList();
        List<ProductEntity> products = productRepository.findByUserUuidAndUuidIn(sellerUuid, productUuids);
        if (products.size() != productUuids.size()) {
            return new ResponseEntity<>(new ResponseDTO(false, "not exist product"), HttpStatus.CONFLICT);
        }
        
        for(ProductEntity productEntity : products){
            Map<String, Object> product = new HashMap<>();
            product.put("uuid", productEntity.getUuid());
            product.put("name", productEntity.getName());
            product.put("price", productEntity.getPrice());
            product.put("description", productEntity.getDescription());
            product.put("image", productEntity.getImage());
            product.put("userUuid", productEntity.getUserUuid());
            product.put("option", productEntity.getOption());
            product.put("discountPrice", 0);
            productList.add(product);

            if(productList.size() == 1){
                data.put("currentProduct", product);
            }
        }
        data.put("announcement", "");
        data.put("product", productList);
        data.put("shippingFee", broadcastEntity.get().getShippingFee());
        redisUtil.set(String.format("broadcast:%d", uuid), data, 24, TimeUnit.HOURS);

        // 방송 시작 알림 전송 (팔로워들에게) - @Async로 비동기 처리
        sendBroadcastStartNotificationToFollowers(broadcastEntity.get(), sellerUuid);

        // VideoSDK 올바른 흐름: 1. Crawler 토큰 생성 -> 2. 미팅 생성 -> 3. RTC 토큰 생성
        
        // 1. Crawler 토큰 생성 (API 호출용)
        String crawlerToken = videoSDKUtil.generateCrawlerToken();
        
        // 2. Crawler 토큰으로 실제 미팅 생성
        String preferredRoomId = "broadcast_" + uuid;
        String actualMeetingId = videoSDKUtil.createVideoSDKMeeting(crawlerToken, preferredRoomId);
        
        // 3. 생성된 실제 미팅 ID로 RTC 토큰 생성
        String participantId = "seller_" + sellerUuid;
        String rtcToken = videoSDKUtil.generateRtcToken(actualMeetingId, participantId);
        
        // 4. 실제 미팅 ID를 BroadcastEntity에 저장
        broadcastEntity.get().setActualMeetingId(actualMeetingId);
        broadcastRepository.save(broadcastEntity.get());
        
        // 응답 데이터 구성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("meetingId", actualMeetingId);    // 실제 VideoSDK 미팅 ID
        responseData.put("rtcToken", rtcToken);            // 해당 미팅용 RTC 토큰
        responseData.put("currentProduct", data.get("currentProduct"));
        responseData.put("broadcastData", data);
        return new ResponseEntity<>(new ResponseDTO(true, "success", responseData), HttpStatus.OK);
    }

    @PostMapping("/end")
    @Operation(summary = "방송 종료", description = "방송을 종료합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "not exist", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "not started", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "already ended", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "not exist broadcastPlan", content = @Content(mediaType = "")),
    })
    @Transactional
    public ResponseEntity<ResponseDTO> endBroadcast(@RequestParam("uuid") Long uuid) {
        Long sellerUuid = JwtHelper.getCurrentUserUuid();

        Optional<BroadcastEntity> broadcastOpt = broadcastRepository.findBySellerUuidAndUuid(sellerUuid, uuid);
        if (broadcastOpt.isEmpty()) {
            return new ResponseEntity<>(new ResponseDTO(false, "not exist"), HttpStatus.CONFLICT);
        }else if (broadcastOpt.get().getStartDatatime() == null) {
            return new ResponseEntity<>(new ResponseDTO(false, "not started"), HttpStatus.CONFLICT);
        }else if (broadcastOpt.get().getEndDatetime() != null) {
            return new ResponseEntity<>(new ResponseDTO(false, "already ended"), HttpStatus.CONFLICT);
        }

        Optional<SellerBroadcastPlanEntity> sellerBroadcastPlanEntity = sellerBroadcastPlanRepository.findBySellerUuid(sellerUuid);
        if(sellerBroadcastPlanEntity.isEmpty()){
            return new ResponseEntity<>(new ResponseDTO(false, "not exist broadcastPlan"), HttpStatus.CONFLICT);
        }
        sellerBroadcastPlanEntity.get().setRemainMinute(sellerBroadcastPlanEntity.get().getRemainMinute() - ((int)ChronoUnit.MINUTES.between(broadcastOpt.get().getStartDatatime(), LocalDateTime.now())));
        sellerBroadcastPlanRepository.save(sellerBroadcastPlanEntity.get());
        broadcastOpt.get().setEndDatetime(LocalDateTime.now());
        broadcastRepository.save(broadcastOpt.get());
        redisUtil.delete(String.format("broadcast:%d", uuid));
        sendReconciliationNotificationToOrderers(broadcastOpt.get(), sellerUuid, broadcastOpt.get().getShippingFee());

        //TODO: videoSDK 연동 해제

        return new ResponseEntity<>(new ResponseDTO(true, "success"), HttpStatus.OK);
    }

    @GetMapping("/product")
    @Operation(summary = "방송의 상품 목록 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "방송 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content(mediaType = ""))
    })
    public ResponseEntity<ResponseDTO> getBroadcastProduct(
            @RequestParam("uuid") Long uuid,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        //TODO: 페이지 네이션 적용 필요
        Long sellerUuid = JwtHelper.getCurrentUserUuid();
        Optional<BroadcastEntity> productList = broadcastRepository.findBySellerUuidAndUuid(sellerUuid, uuid);
        if(productList.isEmpty()){
            return new ResponseEntity<>(new ResponseDTO(false, "not exist"), HttpStatus.CONFLICT);
        }
        List<Long> productUuidList = productList.get().getProductUuidList();
        List<ProductEntity> productEntityList = productRepository.findAllByUuidIn(productUuidList);

        return new ResponseEntity<>(new ResponseDTO(true, "success", productEntityList), HttpStatus.OK);
    }

    @PostMapping("/product")
    @Operation(summary = "방송의 상품 추가")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상품 추가 성공"),
            @ApiResponse(responseCode = "not exist broadcast", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "not own broadcast", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "not own product", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "product already exists", content = @Content(mediaType = ""))
    })
    @Transactional
    public ResponseEntity<ResponseDTO> addBroadcastProduct(@Valid @RequestBody BroadcastAddProductDTO requestDTO) {
        Long sellerUuid = JwtHelper.getCurrentUserUuid();
        Long broadcastUuid = requestDTO.getBroadcastUuid();
        Long productUuid = requestDTO.getProductUuid();
        
        Optional<BroadcastEntity> broadcastOpt = broadcastRepository.findBySellerUuidAndUuid(sellerUuid, broadcastUuid);
        if(broadcastOpt.isEmpty()){
            return new ResponseEntity<>(new ResponseDTO(false, "not own broadcast"), HttpStatus.CONFLICT);
        }
        Optional<ProductEntity> productOpt = productRepository.findByUserUuidAndUuid(sellerUuid, productUuid);
        if(productOpt.isEmpty()){
            return new ResponseEntity<>(new ResponseDTO(false, "not own product"), HttpStatus.CONFLICT);
        }
        
        BroadcastEntity broadcast = broadcastOpt.get();
        ProductEntity product = productOpt.get();
        if(broadcast.getProductUuidList().contains(productUuid)){
            return new ResponseEntity<>(new ResponseDTO(false, "product already exists"), HttpStatus.CONFLICT);
        }
        
        List<Long> currentProductList = new ArrayList<>(broadcast.getProductUuidList());
        currentProductList.add(productUuid);
        broadcast.setProductUuidList(currentProductList);
        broadcastRepository.save(broadcast);
        Object redisData = redisUtil.get("broadcast:" + broadcastUuid);
        if(redisData != null){
            @SuppressWarnings("unchecked")
            Map<String, Object> broadcastData = (Map<String, Object>) redisData;
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> productList = (List<Map<String, Object>>) broadcastData.get("product");
            if(productList != null){
                Map<String, Object> newProduct = new HashMap<>();
                newProduct.put("uuid", product.getUuid());
                newProduct.put("name", product.getName());
                newProduct.put("price", product.getPrice());
                newProduct.put("description", product.getDescription());
                newProduct.put("image", product.getImage());
                newProduct.put("userUuid", product.getUserUuid());
                newProduct.put("option", product.getOption());
                newProduct.put("discountPrice", 0);
                productList.add(newProduct);
                broadcastData.put("product", productList);
                redisUtil.set("broadcast:" + broadcastUuid, broadcastData, 24, TimeUnit.HOURS);
            }
        }
        return new ResponseEntity<>(new ResponseDTO(true, "success"), HttpStatus.OK);
    }

    @DeleteMapping("/product")
    @Operation(summary = "방송의 상품 제거")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상품 제거 성공"),
            @ApiResponse(responseCode = "not exist broadcast", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "not own broadcast", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "product not in broadcast", content = @Content(mediaType = ""))
    })
    @Transactional
    public ResponseEntity<ResponseDTO> deleteBroadcastProduct(@Valid @RequestBody BroadcastAddProductDTO requestDTO) {
        Long sellerUuid = JwtHelper.getCurrentUserUuid();
        Long broadcastUuid = requestDTO.getBroadcastUuid();
        Long productUuid = requestDTO.getProductUuid();

        Optional<BroadcastEntity> broadcastOpt = broadcastRepository.findBySellerUuidAndUuid(sellerUuid, broadcastUuid);
        if(broadcastOpt.isEmpty()){
            return new ResponseEntity<>(new ResponseDTO(false, "not own broadcast"), HttpStatus.CONFLICT);
        }

        BroadcastEntity broadcast = broadcastOpt.get();
        if(!broadcast.getProductUuidList().contains(productUuid)){
            return new ResponseEntity<>(new ResponseDTO(false, "product not in broadcast"), HttpStatus.CONFLICT);
        }

        List<Long> currentProductList = new ArrayList<>(broadcast.getProductUuidList());
        currentProductList.remove(productUuid);
        broadcast.setProductUuidList(currentProductList);
        broadcastRepository.save(broadcast);
        Object redisData = redisUtil.get("broadcast:" + broadcastUuid);
        if(redisData != null){
            @SuppressWarnings("unchecked")
            Map<String, Object> broadcastData = (Map<String, Object>) redisData;
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> productList = (List<Map<String, Object>>) broadcastData.get("product");
            if(productList != null){
                productList.removeIf(product -> product.get("uuid").equals(productUuid));
                broadcastData.put("product", productList);

                @SuppressWarnings("unchecked")
                Map<String, Object> currentProduct = (Map<String, Object>) broadcastData.get("currentProduct");
                if(currentProduct != null && currentProduct.get("uuid").equals(productUuid)){
                    if(!productList.isEmpty()){
                        broadcastData.put("currentProduct", productList.get(0));
                    } else {
                        broadcastData.put("currentProduct", null);
                    }
                }
                redisUtil.set("broadcast:" + broadcastUuid, broadcastData, 24, TimeUnit.HOURS);
            }
        }
        return new ResponseEntity<>(new ResponseDTO(true, "success"), HttpStatus.OK);
    }

    @GetMapping("/liveProduct")
    @Operation(summary = "방송 중인 현재 상품 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "방송 목록 조회 성공"),
            @ApiResponse(responseCode = "not exist", content = @Content(mediaType = ""))
    })
    public ResponseEntity<ResponseDTO> getCurrentProduct(@RequestParam("uuid") Long uuid) {
        Long sellerUuid = JwtHelper.getCurrentUserUuid();
        Object redis = redisUtil.get("broadcast:"+uuid);
        if(redis == null){
            return new ResponseEntity<>(new ResponseDTO(false, "not exist"), HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(new ResponseDTO(true, "success", redis), HttpStatus.OK);
    }

    @PatchMapping("/liveProduct")
    @Operation(summary = "방송 중인 현재 상품 변경")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "현재 상품 변경 성공"),
            @ApiResponse(responseCode = "not exist broadcast", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "not exist product", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "not own product", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "product not in broadcast", content = @Content(mediaType = ""))
    })
    public ResponseEntity<ResponseDTO> changeCurrentProduct(@RequestParam("broadcastUuid") Long broadcastUuid, @RequestParam("productUuid") Long productUuid) {
        Long sellerUuid = JwtHelper.getCurrentUserUuid();
        Object redisData = redisUtil.get("broadcast:" + broadcastUuid);
        if(redisData == null){
            return new ResponseEntity<>(new ResponseDTO(false, "not exist broadcast"), HttpStatus.CONFLICT);
        }

        Optional<ProductEntity> productOpt = productRepository.findByUserUuidAndUuid(sellerUuid, productUuid);
        if(productOpt.isEmpty()){
            return new ResponseEntity<>(new ResponseDTO(false, "not own product"), HttpStatus.CONFLICT);
        }

        Optional<BroadcastEntity> broadcastOpt = broadcastRepository.findBySellerUuidAndUuid(sellerUuid, broadcastUuid);
        if(broadcastOpt.isEmpty() || !broadcastOpt.get().getProductUuidList().contains(productUuid)){
            return new ResponseEntity<>(new ResponseDTO(false, "product not in broadcast"), HttpStatus.CONFLICT);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> broadcastData = (Map<String, Object>) redisData;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> productList = (List<Map<String, Object>>) broadcastData.get("product");
        Map<String, Object> targetProduct = null;
        for (Map<String, Object> product : productList) {
            if (product.get("uuid").equals(productUuid)) {
                targetProduct = product;
                break;
            }
        }
        
        if (targetProduct == null) {
            return new ResponseEntity<>(new ResponseDTO(false, "product not found in broadcast data"), HttpStatus.CONFLICT);
        }
        broadcastData.put("currentProduct", targetProduct);
        redisUtil.set("broadcast:" + broadcastUuid, broadcastData, 24, TimeUnit.HOURS);
        return new ResponseEntity<>(new ResponseDTO(true, "success", targetProduct), HttpStatus.OK);
    }

    @PatchMapping("/announcement")
    @Operation(summary = "방송 중인 현재 공지사항 변경")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공지사항 변경 성공"),
            @ApiResponse(responseCode = "not exist broadcast", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "not own broadcast", content = @Content(mediaType = ""))
    })
    public ResponseEntity<ResponseDTO> changeAnnouncement(@Valid @RequestBody BroadcastAnnouncementDTO requestDTO) {
        Long sellerUuid = JwtHelper.getCurrentUserUuid();
        Long broadcastUuid = requestDTO.getBroadcastUuid();
        String announcement = requestDTO.getAnnouncement();
        Object redisData = redisUtil.get("broadcast:" + broadcastUuid);
        if(redisData == null){
            return new ResponseEntity<>(new ResponseDTO(false, "not exist broadcast"), HttpStatus.CONFLICT);
        }

        Optional<BroadcastEntity> broadcastOpt = broadcastRepository.findBySellerUuidAndUuid(sellerUuid, broadcastUuid);
        if(broadcastOpt.isEmpty()){
            return new ResponseEntity<>(new ResponseDTO(false, "not own broadcast"), HttpStatus.CONFLICT);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> broadcastData = (Map<String, Object>) redisData;
        broadcastData.put("announcement", announcement != null ? announcement : "");
        redisUtil.set("broadcast:" + broadcastUuid, broadcastData, 24, TimeUnit.HOURS);
        return new ResponseEntity<>(new ResponseDTO(true, "success"), HttpStatus.OK);
    }

    @PatchMapping("/discount")
    @Operation(summary = "방송 중인 방송의 상품 할인 가격 변경")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "할인 가격 변경 성공"),
            @ApiResponse(responseCode = "not exist broadcast", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "not own broadcast", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "product not in broadcast", content = @Content(mediaType = ""))
    })
    public ResponseEntity<ResponseDTO> changeDiscount(@Valid @RequestBody BroadcastDiscountDTO requestDTO) {
        Long sellerUuid = JwtHelper.getCurrentUserUuid();
        Long broadcastUuid = requestDTO.getBroadcastUuid();
        Long productUuid = requestDTO.getProductUuid();
        Integer discountPrice = requestDTO.getDiscountPrice();
        
        Optional<BroadcastEntity> broadcastOpt = broadcastRepository.findBySellerUuidAndUuid(sellerUuid, broadcastUuid);
        if(broadcastOpt.isEmpty()){
            return new ResponseEntity<>(new ResponseDTO(false, "not own broadcast"), HttpStatus.CONFLICT);
        }
        BroadcastEntity broadcast = broadcastOpt.get();
        if(!broadcast.getProductUuidList().contains(productUuid)){
            return new ResponseEntity<>(new ResponseDTO(false, "product not in broadcast"), HttpStatus.CONFLICT);
        }
        Object redisData = redisUtil.get("broadcast:" + broadcastUuid);
        if(redisData == null){
            return new ResponseEntity<>(new ResponseDTO(false, "not exist broadcast"), HttpStatus.CONFLICT);
        }

        HashMap<String, Object> broadcastData = (HashMap<String, Object>) redisData;
        List<Map<String, Object>> productList = (List<Map<String, Object>>) broadcastData.get("product");
        
        boolean productFound = false;
        for (Map<String, Object> product : productList) {
            if (product.get("uuid").equals(productUuid)) {
                product.put("discountPrice", discountPrice);
                productFound = true;
                break;
            }
        }
        
        if(!productFound){
            return new ResponseEntity<>(new ResponseDTO(false, "product not in broadcast"), HttpStatus.CONFLICT);
        }

        // currentProduct도 같은 상품이면 discountPrice 업데이트
        @SuppressWarnings("unchecked")
        Map<String, Object> currentProduct = (Map<String, Object>) broadcastData.get("currentProduct");
        if(currentProduct != null && currentProduct.get("uuid").equals(productUuid)){
            currentProduct.put("discountPrice", discountPrice);
        }
        
        redisUtil.set("broadcast:" + broadcastUuid, broadcastData, 24, TimeUnit.HOURS);
        return new ResponseEntity<>(new ResponseDTO(true, "success"), HttpStatus.OK);
    }

    @PatchMapping("/hls")
    @Operation(summary = "방송 HLS URL 업데이트")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "HLS URL 업데이트 성공"),
            @ApiResponse(responseCode = "not exist", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "not own broadcast", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "not started", content = @Content(mediaType = ""))
    })
    public ResponseEntity<ResponseDTO> updateHlsUrl(@RequestParam("broadcastUuid") Long broadcastUuid, @RequestParam("hlsUrl") String hlsUrl) {
        Long sellerUuid = JwtHelper.getCurrentUserUuid();
        
        Optional<BroadcastEntity> broadcastOpt = broadcastRepository.findBySellerUuidAndUuid(sellerUuid, broadcastUuid);
        if (broadcastOpt.isEmpty()) {
            return new ResponseEntity<>(new ResponseDTO(false, "not own broadcast"), HttpStatus.CONFLICT);
        }
        
        BroadcastEntity broadcast = broadcastOpt.get();
        if (broadcast.getStartDatatime() == null) {
            return new ResponseEntity<>(new ResponseDTO(false, "not started"), HttpStatus.CONFLICT);
        }
        if (broadcast.getEndDatetime() != null) {
            return new ResponseEntity<>(new ResponseDTO(false, "already ended"), HttpStatus.CONFLICT);
        }
        
        broadcast.setHlsUrl(hlsUrl);
        broadcastRepository.save(broadcast);
        
        return new ResponseEntity<>(new ResponseDTO(true, "success"), HttpStatus.OK);
    }
    
    /**
     * 방송 시작 시 팔로워들에게 알림 전송 (비동기 처리)
     * 
     * @param broadcast 방송 엔티티
     * @param sellerUuid 판매자 UUID
     */
    @Async
    public void sendBroadcastStartNotificationToFollowers(BroadcastEntity broadcast, Long sellerUuid) {
        try {
            // 판매자 정보 조회
            Optional<UserEntity> sellerOpt = userEntityRepository.findById(sellerUuid);
            if (sellerOpt.isEmpty()) {
                log.warn("판매자 정보를 찾을 수 없어 방송 시작 알림을 전송하지 않습니다. sellerUuid: {}", sellerUuid);
                return;
            }
            UserEntity seller = sellerOpt.get();

            // 팔로워 목록 조회
            List<FollowEntity> follows = followRepository.findAllByFollowingUuid(sellerUuid);
            if (follows.isEmpty()) {
                log.info("팔로워가 없어 방송 시작 알림을 전송하지 않습니다. sellerUuid: {}", sellerUuid);
                return;
            }

            // 팔로워 정보 수집 (N+1 쿼리 방지를 위한 일괄 조회)
            List<Long> followerUuids = follows.stream()
                .map(FollowEntity::getFollowerUuid)
                .toList();
            
            List<UserEntity> followerUsers = userEntityRepository.findAllById(followerUuids);
            
            List<KakaoNotificationUtil.FollowerInfo> followers = followerUsers.stream()
                .filter(follower -> follower.getPhoneNum() != null && !follower.getPhoneNum().trim().isEmpty())
                .map(follower -> new KakaoNotificationUtil.FollowerInfo(
                    follower.getPhoneNum(), 
                    follower.getName()
                ))
                .toList();
            
            if (followers.isEmpty()) {
                log.warn("유효한 전화번호를 가진 팔로워가 없어 방송 시작 알림을 전송하지 않습니다. sellerUuid: {}", sellerUuid);
                return;
            }
            
            // 방송 시작 대량 알림 전송
            boolean success = kakaoNotificationUtil.sendBroadcastStartNotificationMulti(
                seller.getName(),
                seller.getUrlName(),
                followers
            );
            
            if (success) {
                log.info("방송 시작 알림 전송 완료 - 방송: {}, 판매자: {}, 팔로워 수: {}", broadcast.getTitle(), seller.getName(), followers.size());
            } else {
                log.error("방송 시작 알림 전송 실패 - 방송: {}, 판매자: {}", broadcast.getTitle(), seller.getName());
            }
            
        } catch (Exception e) {
            log.error("방송 시작 알림 전송 중 오류 발생 - sellerUuid: {}, broadcastUuid: {}", sellerUuid, broadcast.getUuid(), e);
        }
    }
    
    /**
     * 방송 종료 시 주문자들에게 정산서 알림 전송 (비동기 처리)
     * 
     * @param broadcast 방송 엔티티
     * @param sellerUuid 판매자 UUID
     * @param shippingFee 배송비
     */
    @Async
    public void sendReconciliationNotificationToOrderers(BroadcastEntity broadcast, Long sellerUuid, int shippingFee) {
        try {
            // 판매자 정보 조회
            Optional<UserEntity> sellerOpt = userEntityRepository.findById(sellerUuid);
            if (sellerOpt.isEmpty()) {
                log.warn("판매자 정보를 찾을 수 없어 정산서 알림을 전송하지 않습니다. sellerUuid: {}", sellerUuid);
                return;
            }
            UserEntity seller = sellerOpt.get();
            
            // 계좌 정보가 없으면 알림 전송하지 않음
            if (seller.getAccountType() == null || seller.getAccountNum() == null || seller.getName() == null) {
                log.warn("판매자 계좌 정보가 불완전하여 정산서 알림을 전송하지 않습니다. sellerUuid: {}", sellerUuid);
                return;
            }
            
            // 해당 방송의 주문 목록 조회
            List<OrderEntity> orders = orderRepository.findBySellerUuidAndBroadcastUuid(sellerUuid, broadcast.getUuid());
            if (orders.isEmpty()) {
                log.info("해당 방송에 주문이 없어 정산서 알림을 전송하지 않습니다. broadcastUuid: {}", broadcast.getUuid());
                return;
            }
            
            // 구매자별 주문 그룹화 및 총액 계산
            Map<Long, List<OrderEntity>> ordersByBuyer = orders.stream()
                .collect(Collectors.groupingBy(OrderEntity::getBuyerUuid));
            
            // 구매자 정보 일괄 조회 (N+1 쿼리 방지)
            List<Long> buyerUuids = new ArrayList<>(ordersByBuyer.keySet());
            List<UserEntity> buyers = userEntityRepository.findAllById(buyerUuids);
            
            // 구매자별 정산서 알림 전송
            for (UserEntity buyer : buyers) {
                List<OrderEntity> buyerOrders = ordersByBuyer.get(buyer.getUuid());
                if (buyerOrders == null || buyerOrders.isEmpty()) {
                    continue;
                }
                
                // 구매자의 총 주문 금액 계산
                int totalAmount = buyerOrders.stream()
                    .mapToInt(OrderEntity::getPrice)
                    .sum();
                
                // 대표 주문 UUID (첫 번째 주문)
                Long representativeOrderUuid = buyerOrders.get(0).getUuid();
                
                // 은행명 변환
                String bankName = getBankNameFromType(seller.getAccountType());
                
                // 정산서 알림 전송
                boolean success = kakaoNotificationUtil.sendReconciliationNotification(
                    broadcast.getTitle(),
                    bankName,
                    seller.getAccountNum(),
                    seller.getName(),
                    totalAmount+shippingFee,
                    buyer.getName() != null ? buyer.getName() : "고객",
                    buyer.getPhoneNum(),
                    representativeOrderUuid,
                    seller.getUrlName()
                );
                
                if (success) {
                    log.info("정산서 알림 전송 완료 - 구매자: {}, 총액: {}원, 주문 수: {}", 
                        buyer.getName(), totalAmount, buyerOrders.size());
                } else {
                    log.error("정산서 알림 전송 실패 - 구매자: {}, 총액: {}원", 
                        buyer.getName(), totalAmount);
                }
            }
            
            log.info("방송 종료 정산서 알림 전송 완료 - 방송: {}, 총 구매자 수: {}", 
                broadcast.getTitle(), buyers.size());
            
        } catch (Exception e) {
            log.error("정산서 알림 전송 중 오류 발생 - sellerUuid: {}, broadcastUuid: {}", 
                sellerUuid, broadcast.getUuid(), e);
        }
    }
    
    /**
     * BankType 열거형을 한국어 은행명으로 변환
     * 
     * @param bankType 은행 타입
     * @return 한국어 은행명
     */
    private String getBankNameFromType(BankType bankType) {
        return switch (bankType) {
            case KB -> "국민은행";
            case SHINHAN -> "신한은행";
            case HANA -> "하나은행";
            case WOORI -> "우리은행";
            case SC -> "SC제일은행";
            case CITI -> "한국씨티은행";
            case DGB_DAEGU -> "대구은행";
            case BNK_BUSAN -> "부산은행";
            case GWANGJU -> "광주은행";
            case JEJU -> "제주은행";
            case JEONBUK -> "전북은행";
            case GYEONGNAM -> "경남은행";
            case K_BANK -> "케이뱅크";
            case KAKAO -> "카카오뱅크";
            case TOSS -> "토스뱅크";
            case NH -> "농협은행";
            case SUHYUP -> "수협은행";
            case IBK -> "기업은행";
            case KEB -> "외환은행";
            case MG -> "새마을금고";
            case CU -> "신협";
            case POST_OFFICE -> "우체국";
            case KDB -> "산업은행";
            default -> bankType.name();
        };
    }
}
