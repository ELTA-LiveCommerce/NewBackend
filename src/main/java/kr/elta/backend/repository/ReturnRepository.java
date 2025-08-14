package kr.elta.backend.repository;

import kr.elta.backend.entity.OrderEntity;
import kr.elta.backend.entity.ReturnEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReturnRepository extends JpaRepository<ReturnEntity, Long> {
    Optional<ReturnEntity> findBySellerUuidAndUuid(Long sellerUuid, Long uuid);
    Page<ReturnEntity> findBySellerUuid(Long sellerUuid, Pageable pageable);
}
