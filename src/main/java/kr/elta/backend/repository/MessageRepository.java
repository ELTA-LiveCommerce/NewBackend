package kr.elta.backend.repository;

import kr.elta.backend.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    
    @Modifying
    @Transactional
    @Query("UPDATE MessageEntity m SET m.isRead = true WHERE m.chatUuid = :chatUuid AND m.senderUuid = :sellerUuid AND m.isRead = false")
    int updateIsReadTrueForSellerMessages(@Param("chatUuid") Long chatUuid, @Param("sellerUuid") Long sellerUuid);
    
    @Modifying
    @Transactional
    @Query("UPDATE MessageEntity m SET m.isRead = true WHERE m.chatUuid = :chatUuid AND m.senderUuid = :viewerUuid AND m.isRead = false")
    int updateIsReadTrueForViewerMessages(@Param("chatUuid") Long chatUuid, @Param("viewerUuid") Long viewerUuid);
    
    List<MessageEntity> findAllByChatUuidOrderByCreateDateTime(Long chatUuid);
    
    @Modifying
    @Transactional
    @Query("UPDATE MessageEntity m SET m.isRead = true WHERE m.chatUuid = :inquiryUuid AND m.senderUuid != :userUuid AND m.isRead = false")
    int updateIsReadTrueForInquiryMessages(@Param("inquiryUuid") Long inquiryUuid, @Param("userUuid") Long userUuid);
}
