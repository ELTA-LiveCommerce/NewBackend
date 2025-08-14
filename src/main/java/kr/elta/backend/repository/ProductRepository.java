package kr.elta.backend.repository;

import kr.elta.backend.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    Optional<ProductEntity> findByUserUuidAndUuid(Long userUuid, Long uuid);

    Page<ProductEntity> findAllByUserUuid(Long userUuid, Pageable pageable);

    List<ProductEntity> findAllByIsPublicAndUserUuid(Boolean isPublic, Long userUuid);
    
    @Modifying
    @Transactional
    void deleteAllByUserUuid(Long userUuid);

    List<ProductEntity> findByUserUuid(Long userUuid);
    
    /**
     * 특정 사용자의 여러 상품을 UUID 리스트로 조회 (N+1 쿼리 방지)
     */
    List<ProductEntity> findByUserUuidAndUuidIn(Long userUuid, List<Long> uuids);

    List<ProductEntity> findAllByUuidIn(Collection<Long> uuids);

    List<ProductEntity> findByUserUuidAndIsPublicIsTrue(Long userUuid);
}
