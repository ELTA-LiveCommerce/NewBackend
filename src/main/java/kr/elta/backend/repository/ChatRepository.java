package kr.elta.backend.repository;

import jakarta.validation.constraints.NotNull;
import kr.elta.backend.entity.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<ChatEntity, Long> {
    List<ChatEntity> findAllByViewerUuid(Long viewerUuid);

    boolean existsByViewerUuid(Long viewerUuid);

    boolean existsBySellerUuidAndViewerUuid(Long sellerUuid, Long viewerUuid);

    boolean existsByUuid(Long uuid);

    boolean existsByUuidAndViewerUuid(Long uuid, Long viewerUuid);

    Optional<ChatEntity> findByUuidAndViewerUuid(Long uuid, Long viewerUuid);
    
    @Modifying
    @Transactional
    @Query("UPDATE ChatEntity c SET c.viewerUnreadCount = 0 WHERE c.uuid = :uuid AND c.viewerUuid = :viewerUuid")
    int updateViewerUnreadCountToZero(@Param("uuid") Long uuid, @Param("viewerUuid") Long viewerUuid);
    
    @Modifying
    @Transactional
    @Query("UPDATE ChatEntity c SET c.sellerUnreadCount = 0 WHERE c.uuid = :uuid AND c.sellerUuid = :sellerUuid")
    int updateSellerUnreadCountToZero(@Param("uuid") Long uuid, @Param("sellerUuid") Long sellerUuid);

    ChatEntity findByUuid(Long uuid);

    List<ChatEntity> findAllBySellerUuid(Long userUuid);

    boolean existsByUuidAndSellerUuid(@NotNull(message = "uuid는 필수 입력 값입니다.") Long uuid, Long userUuid);

    Optional<ChatEntity> findByUuidAndSellerUuid(Long uuid, Long userUuid);
}
