package kr.elta.backend.repository;

import kr.elta.backend.entity.BroadcastEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BroadcastRepository extends JpaRepository<BroadcastEntity, Long> {
    
    /**
     * 현재 진행 중이거나 시작하지 않은 방송 조회 (endDatetime이 null인 방송)
     * - 진행 중인 방송: startDatatime이 있고 endDatetime이 null
     * - 시작하지 않은 방송: startDatatime이 null이고 endDatetime이 null
     */
    List<BroadcastEntity> findAllByEndDatetimeIsNullAndSellerUuid(Long sellerUuid);
    
    /**
     * 특정 판매자의 모든 방송 목록 조회
     */
    List<BroadcastEntity> findAllBySellerUuid(Long sellerUuid);
    
    /**
     * 특정 판매자의 방송 목록을 페이지네이션으로 조회 (최신순)
     */
    Page<BroadcastEntity> findBySellerUuidOrderByUuidDesc(Long sellerUuid, Pageable pageable);
    
    /**
     * 모든 방송 목록을 페이지네이션으로 조회 (최신순)
     */
    Page<BroadcastEntity> findAllByOrderByUuidDesc(Pageable pageable);

    Optional<BroadcastEntity> findBySellerUuidAndUuid(Long sellerUuid, Long uuid);

    void deleteBySellerUuidAndUuid(Long sellerUuid, Long uuid);

    /**
     * 특정 판매자의 시작하지 않은 방송 조회 (startDatatime이 null인 방송)
     */
    Optional<BroadcastEntity> findBySellerUuidAndUuidAndStartDatatimeIsNull(Long sellerUuid, Long uuid);

    List<BroadcastEntity> findAllBySellerUuidAndStartDatatimeIsNotNullAndEndDatetimeIsNull(Long sellerUuid);
}
