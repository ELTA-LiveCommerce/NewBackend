package kr.elta.backend.repository;

import kr.elta.backend.entity.SellerBroadcastPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerBroadcastPlanRepository extends JpaRepository<SellerBroadcastPlanEntity, Long> {
    Optional<SellerBroadcastPlanEntity> findBySellerUuid(Long sellerUuid);
}
