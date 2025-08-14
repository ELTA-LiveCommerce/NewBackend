package kr.elta.backend.controller.seller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.elta.backend.dto.ProductDTO;
import kr.elta.backend.dto.ResponseDTO;
import kr.elta.backend.entity.ProductEntity;
import kr.elta.backend.repository.ProductRepository;
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

import java.util.*;

@RestController
@RequestMapping("/seller/product")
@Slf4j
@Tag(name = "Seller Product", description = "판매자 상품 관리 API")
public class SellerProduct {
    @Autowired
    private ProductRepository productRepository;

    @GetMapping("")
    public ResponseEntity<ResponseDTO> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "uuid") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Long userUuid = JwtHelper.getCurrentUserUuid();
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ProductEntity> productPage = productRepository.findAllByUserUuid(userUuid, pageable);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("products", productPage.getContent());
        responseData.put("currentPage", productPage.getNumber());
        responseData.put("totalPages", productPage.getTotalPages());
        responseData.put("totalElements", productPage.getTotalElements());
        responseData.put("pageSize", productPage.getSize());
        responseData.put("hasNext", productPage.hasNext());
        responseData.put("hasPrevious", productPage.hasPrevious());
        
        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", responseData), HttpStatus.OK);
    }

    @GetMapping("/name")
    public ResponseEntity<ResponseDTO> getProductsName() {
        Long userUuid = JwtHelper.getCurrentUserUuid();
        List<ProductEntity> productEntityList = productRepository.findByUserUuid(userUuid);
        List<Map<String, Object>> response = new ArrayList<>();
        for(ProductEntity productEntity: productEntityList){
            Map<String, Object> data = new HashMap<>();
            data.put("uuid", productEntity.getUuid());
            data.put("name", productEntity.getName());
            response.add(data);
        }

        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", response), HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<ResponseDTO> createProduct(@Valid @RequestBody ProductDTO productDTO){
        Long userUuid = JwtHelper.getCurrentUserUuid();
        
        ProductEntity product = ProductEntity.builder()
                .name(productDTO.getName())
                .price(productDTO.getPrice())
                .description(productDTO.getDescription())
                .isPublic(productDTO.getIsPublic())
                .image(productDTO.getImage())
                .option(productDTO.getOption())
                .userUuid(userUuid)
                .build();
        
        productRepository.save(product);
        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", product.getUuid()), HttpStatus.OK);
    }

    @PatchMapping("")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "not exist", content = @Content(mediaType = "")),
    })
    public ResponseEntity<ResponseDTO> updateProduct(@Valid @RequestBody ProductDTO productDTO){
        Long userUuid = JwtHelper.getCurrentUserUuid();

        Optional<ProductEntity> productEntity = productRepository.findByUserUuidAndUuid(userUuid, productDTO.getId());
        if(productEntity.isEmpty()){
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "not exist"), HttpStatus.CONFLICT);
        }

        productEntity.ifPresent(entity -> {
            entity.setName(productDTO.getName());
            entity.setPrice(productDTO.getPrice());
            entity.setDescription(productDTO.getDescription());
            entity.setIsPublic(productDTO.getIsPublic());
            entity.setImage(productDTO.getImage());
            entity.setOption(productDTO.getOption());
            entity.setUserUuid(userUuid);
            productRepository.save(entity);
        });

        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", productEntity.get().getUuid()), HttpStatus.OK);
    }

    @DeleteMapping("")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "parameter was not provided", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "not exist", content = @Content(mediaType = "")),
    })
    public ResponseEntity<ResponseDTO> deleteProduct(@RequestBody Map<String, List<Long>> body){
        Long userUuid = JwtHelper.getCurrentUserUuid();
        List<Long> productIds = body.get("id");
        
        if (productIds == null || productIds.isEmpty()) {
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "parameter was not provided"), HttpStatus.BAD_REQUEST);
        }

        for (Long productId : productIds) {
            Optional<ProductEntity> productEntity = productRepository.findByUserUuidAndUuid(userUuid, productId);
            if (productEntity.isPresent()) {
                productRepository.delete(productEntity.get());
            }
        }

        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success"), HttpStatus.OK);
    }
}