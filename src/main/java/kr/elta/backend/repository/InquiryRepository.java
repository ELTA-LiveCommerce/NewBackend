package kr.elta.backend.repository;

import kr.elta.backend.entity.InquiryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface InquiryRepository extends JpaRepository<InquiryEntity, Long> {
    List<InquiryEntity> findAllByUserUuidOrderByCreateDateTimeDesc(Long userUuid);
    
    Optional<InquiryEntity> findByUuidAndUserUuid(Long uuid, Long userUuid);
    
    @Modifying
    @Transactional
    @Query("UPDATE InquiryEntity i SET i.unreadCount = 0 WHERE i.uuid = :uuid AND i.userUuid = :userUuid")
    int updateUnreadCountToZero(@Param("uuid") Long uuid, @Param("userUuid") Long userUuid);
    
    @Modifying
    @Transactional
    @Query("UPDATE InquiryEntity i SET i.unreadCount = i.unreadCount + 1 WHERE i.uuid = :uuid")
    int incrementUnreadCount(@Param("uuid") Long uuid);
    
    List<InquiryEntity> findAllByOrderByCreateDateTimeDesc();
    
    @Modifying
    @Transactional
    @Query("UPDATE InquiryEntity i SET i.isEnd = true WHERE i.uuid = :uuid")
    int updateIsEndToTrue(@Param("uuid") Long uuid);
}
