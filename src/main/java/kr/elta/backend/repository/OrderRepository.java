package kr.elta.backend.repository;

import kr.elta.backend.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Page<OrderEntity> findAllByBuyerUuid(Long buyerUuid, Pageable pageable);
    Page<OrderEntity> findAllBySellerUuid(Long sellerUuid, Pageable pageable);
    Optional<OrderEntity> findBySellerUuidAndUuid(Long sellerUuid, Long orderUuid);
    
    /**
     * 특정 판매자의 특정 방송에 대한 주문 목록 조회
     */
    List<OrderEntity> findBySellerUuidAndBroadcastUuid(Long sellerUuid, Long broadcastUuid);
}
