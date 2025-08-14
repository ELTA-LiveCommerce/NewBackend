package kr.elta.backend.repository;

import kr.elta.backend.entity.ViewerStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ViewerStatusRepository extends JpaRepository<ViewerStatusEntity, Long> {
    Optional<ViewerStatusEntity> findBySellerUuidAndViewerUuid(Long sellerUuid, Long viewerUuid);
}
