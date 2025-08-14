package kr.elta.backend.repository;

import kr.elta.backend.entity.PointTransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointTransactionRepository extends JpaRepository<PointTransactionEntity, Long> {
    List<PointTransactionEntity> findBySellerUuidOrderByUuidDesc(Long sellerUuid);
    Page<PointTransactionEntity> findBySellerUuidOrderByUuidDesc(Long sellerUuid, Pageable pageable);
}