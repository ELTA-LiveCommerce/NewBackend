package kr.elta.backend.controller.seller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.elta.backend.dto.MessageDTO;
import kr.elta.backend.dto.ResponseDTO;
import kr.elta.backend.entity.ChatEntity;
import kr.elta.backend.entity.MessageEntity;
import kr.elta.backend.entity.Role;
import kr.elta.backend.repository.ChatRepository;
import kr.elta.backend.repository.MessageRepository;
import kr.elta.backend.repository.UserEntityRepository;
import kr.elta.backend.util.JwtHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/seller/message")
@Slf4j
@Tag(name = "/seller/message", description = "메세지 관리 API")
public class SellerMessage {
    @Autowired
    private UserEntityRepository userEntityRepository;
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private MessageRepository messageRepository;

    @GetMapping("")
    public ResponseEntity<ResponseDTO> getMyChatList() {
        Long userUuid = JwtHelper.getCurrentUserUuid();
        List<ChatEntity> chatEntityList = chatRepository.findAllBySellerUuid(userUuid);
        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", chatEntityList), HttpStatus.OK);
    }

    @PutMapping("")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "not exist chat", content = @Content(mediaType = "")),
    })
    public ResponseEntity<ResponseDTO> sendChat(@Valid @RequestBody MessageDTO messageDTO) {
        Long userUuid = JwtHelper.getCurrentUserUuid();
        if(!chatRepository.existsByUuidAndSellerUuid(messageDTO.getUuid(), userUuid)){
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "not exist chat"), HttpStatus.CONFLICT);
        }

        ChatEntity chatEntity = chatRepository.findByUuid(messageDTO.getUuid());
        chatEntity.setViewerUnreadCount(chatEntity.getViewerUnreadCount()+1);
        chatEntity.setLastMessage(messageDTO.getMessage());
        chatEntity.setLastMessageDateTime(LocalDateTime.now());
        chatRepository.save(chatEntity);

        MessageEntity messageEntity = new MessageEntity().builder()
                .chatUuid(messageDTO.getUuid())
                .senderUuid(userUuid)
                .message(messageDTO.getMessage())
                .createDateTime(LocalDateTime.now())
                .isRead(false)
                .build();
        messageRepository.save(messageEntity);

        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", messageEntity.getUuid()), HttpStatus.OK);
    }

    @GetMapping("/{uuid}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "not exist chat", content = @Content(mediaType = "")),
    })
    @Transactional
    public ResponseEntity<ResponseDTO> getChatMessage(@PathVariable("uuid") Long uuid) {
        Long userUuid = JwtHelper.getCurrentUserUuid();
        
        Optional<ChatEntity> chatEntityOpt = chatRepository.findByUuidAndSellerUuid(uuid, userUuid);
        if(chatEntityOpt.isEmpty()){
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "not exist chat"), HttpStatus.CONFLICT);
        }

        ChatEntity chatEntity = chatEntityOpt.get();
        Long viewerUuid = chatEntity.getViewerUuid();
        
        // 시청자가 보낸 메시지들을 일괄적으로 읽음 처리
        messageRepository.updateIsReadTrueForViewerMessages(uuid, viewerUuid);
        
        // 판매자의 읽지 않은 메시지 개수를 직접 쿼리로 업데이트
        chatRepository.updateSellerUnreadCountToZero(uuid, userUuid);
        
        // 채팅 메시지 목록 조회
        List<MessageEntity> messages = messageRepository.findAllByChatUuidOrderByCreateDateTime(uuid);

        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", messages), HttpStatus.OK);
    }
}