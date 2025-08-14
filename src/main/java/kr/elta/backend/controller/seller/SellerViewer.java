package kr.elta.backend.controller.seller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.elta.backend.controller.User;
import kr.elta.backend.dto.ResponseDTO;
import kr.elta.backend.dto.ViewerStatusDTO;
import kr.elta.backend.entity.*;
import kr.elta.backend.repository.FollowRepository;
import kr.elta.backend.repository.PointTransactionRepository;
import kr.elta.backend.repository.UserEntityRepository;
import kr.elta.backend.repository.ViewerStatusRepository;
import kr.elta.backend.util.JwtHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/seller/viewer")
@Slf4j
@Tag(name = "/seller/viewer", description = "셀러의 사용자 관리 API")
public class SellerViewer {
    @Autowired
    private UserEntityRepository userEntityRepository;
    @Autowired
    private FollowRepository followRepository;
    @Autowired
    private ViewerStatusRepository viewerStatusRepository;

    @GetMapping("")
    public ResponseEntity<ResponseDTO> getViewerList(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit) {
        //TODO: 검색도 가능하게 수정
        Long sellerUuid = JwtHelper.getCurrentUserUuid();

        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<FollowEntity> transactionPage = followRepository.findAllByFollowingUuid(sellerUuid, pageable);
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> user = new HashMap<>();
        for(FollowEntity followEntity : transactionPage){
            UserEntity userEntity = userEntityRepository.findByUuid(followEntity.getFollowerUuid());
            user.put("user", userEntity);
            Optional<ViewerStatusEntity> viewerStatusEntity = viewerStatusRepository.findBySellerUuidAndViewerUuid(sellerUuid, followEntity.getFollowerUuid());
            if(viewerStatusEntity.isPresent()){
                user.put("status", viewerStatusEntity.get().getStatus());
            }else{
                user.put("status", "NORMAL");
            }
            result.add(user);
        }
        Map<String, Object> response = Map.of(
                "transactions", result,
                "total", transactionPage.getTotalElements(),
                "page", page,
                "limit", limit,
                "totalPages", transactionPage.getTotalPages()
        );

        return new ResponseEntity<>(new ResponseDTO(true, "success", response), HttpStatus.OK);
    }

    @PutMapping("")
    public ResponseEntity<ResponseDTO> updateViewerStatus(@Valid @RequestBody ViewerStatusDTO viewerStatusDTO) {
        Long sellerUuid = JwtHelper.getCurrentUserUuid();

        Optional<ViewerStatusEntity> viewerStatusEntity = viewerStatusRepository.findBySellerUuidAndViewerUuid(sellerUuid, viewerStatusDTO.getViewerUuid());
        if(viewerStatusEntity.isPresent()){
            if(viewerStatusDTO.getStatus() == ViewerStatus.NORMAL){
                viewerStatusRepository.delete(viewerStatusEntity.get());
            }else{
                viewerStatusEntity.get().setStatus(viewerStatusDTO.getStatus());
                viewerStatusRepository.save(viewerStatusEntity.get());
            }
        }else{
            viewerStatusEntity = Optional.ofNullable(ViewerStatusEntity.builder()
                    .sellerUuid(sellerUuid)
                    .viewerUuid(viewerStatusDTO.getViewerUuid())
                    .status(viewerStatusDTO.getStatus())
                    .build());
            viewerStatusRepository.save(viewerStatusEntity.get());
        }
        return new ResponseEntity<>(new ResponseDTO(true, "success"), HttpStatus.OK);
    }
}
