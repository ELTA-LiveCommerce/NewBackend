package kr.elta.backend.repository;

import kr.elta.backend.entity.DeliveryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliverRepository extends JpaRepository<DeliveryEntity, Long> {
    Optional<DeliveryEntity> findBySellerUuidAndUuid(Long sellerUuid, Long uuid);
    Page<DeliveryEntity> findBySellerUuid(Long sellerUuid, Pageable pageable);
    Optional<DeliveryEntity> findByOrderUuid(Long orderUuid);
}
