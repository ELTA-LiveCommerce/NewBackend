package kr.elta.backend.controller.seller;

import io.swagger.v3.oas.annotations.tags.Tag;
import kr.elta.backend.dto.ResponseDTO;
import kr.elta.backend.entity.PointTransactionEntity;
import kr.elta.backend.entity.UserEntity;
import kr.elta.backend.repository.PointTransactionRepository;
import kr.elta.backend.repository.UserEntityRepository;
import kr.elta.backend.util.JwtHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/seller/point")
@Slf4j
@Tag(name = "/seller/point", description = "포인트 관리 API")
public class SellerPoint {
    @Autowired
    private UserEntityRepository userEntityRepository;
    
    @Autowired
    private PointTransactionRepository pointTransactionRepository;

    @GetMapping("")
    public ResponseEntity<ResponseDTO> getTransaction(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit) {
        Long sellerUuid = JwtHelper.getCurrentUserUuid();
        
        Optional<UserEntity> userOpt = userEntityRepository.findById(sellerUuid);
        if (userOpt.isEmpty()) {
            return new ResponseEntity<>(new ResponseDTO(false, "사용자를 찾을 수 없습니다"), HttpStatus.NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<PointTransactionEntity> transactionPage = pointTransactionRepository.findBySellerUuidOrderByUuidDesc(sellerUuid, pageable);
        
        Map<String, Object> response = Map.of(
                "currentBalance", userOpt.get().getBalance(),
                "transactions", transactionPage.getContent(),
                "total", transactionPage.getTotalElements(),
                "page", page,
                "limit", limit,
                "totalPages", transactionPage.getTotalPages()
        );
        
        return new ResponseEntity<>(new ResponseDTO(true, "success", response), HttpStatus.OK);
    }
}
