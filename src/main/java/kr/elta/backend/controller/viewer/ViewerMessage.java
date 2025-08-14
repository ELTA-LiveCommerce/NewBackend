package kr.elta.backend.controller.viewer;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.elta.backend.dto.MessageDTO;
import kr.elta.backend.dto.ResponseDTO;
import kr.elta.backend.entity.*;
import kr.elta.backend.repository.*;
import kr.elta.backend.util.JwtHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/viewer/message")
@Slf4j
@Tag(name = "/viewer/message", description = "메세지 관리 API")
public class ViewerMessage {
    @Autowired
    private UserEntityRepository userEntityRepository;
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private MessageRepository messageRepository;

    @GetMapping("")
    public ResponseEntity<ResponseDTO> getMyChatList() {
        Long userUuid = JwtHelper.getCurrentUserUuid();
        List<ChatEntity> chatEntityList = chatRepository.findAllByViewerUuid(userUuid);
        
        // ChatEntity를 Map으로 변환하면서 판매자 정보 추가
        var chatList = chatEntityList.stream().map(chat -> {
            Map<String, Object> chatMap = new HashMap<>();
            chatMap.put("uuid", chat.getUuid());
            chatMap.put("sellerUuid", chat.getSellerUuid());
            chatMap.put("viewerUuid", chat.getViewerUuid());
            chatMap.put("sellerUnreadCount", chat.getSellerUnreadCount());
            chatMap.put("viewerUnreadCount", chat.getViewerUnreadCount());
            chatMap.put("lastMessage", chat.getLastMessage());
            chatMap.put("lastMessageDateTime", chat.getLastMessageDateTime());
            chatMap.put("createDateTime", chat.getCreateDateTime());
            
            // 판매자 정보 추가 (관리자 문의인 경우 특별 처리)
            if (chat.getSellerUuid() == 0) {
                // 관리자 문의용 채팅방
                chatMap.put("sellerName", "관리자");
                chatMap.put("sellerProfileImg", null);
                chatMap.put("sellerBusinessTime", "24시간");
            } else {
                // 일반 판매자 채팅방
                Optional<UserEntity> sellerOpt = userEntityRepository.findById(chat.getSellerUuid());
                if (sellerOpt.isPresent()) {
                    UserEntity seller = sellerOpt.get();
                    chatMap.put("sellerName", seller.getName());
                    chatMap.put("sellerProfileImg", seller.getProfileImg());
                    chatMap.put("sellerBusinessTime", seller.getBusinessTime());
                }
            }
            
            return chatMap;
        }).toList();
        
        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", chatList), HttpStatus.OK);
    }

    @PostMapping("")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "already exist", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "not exist seller", content = @Content(mediaType = "")),
    })
    public ResponseEntity<ResponseDTO> createChat(@RequestParam("uuid") Long uuid) {
        Long userUuid = JwtHelper.getCurrentUserUuid();
        
        // uuid가 0인 경우 관리자 문의용 채팅방 생성
        if (uuid == 0) {
            if(chatRepository.existsBySellerUuidAndViewerUuid(0L, userUuid)){
                return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "already exist"), HttpStatus.CONFLICT);
            }
            
            ChatEntity chatEntity = new ChatEntity().builder()
                    .sellerUuid(0L) // 관리자 문의용은 0으로 설정
                    .viewerUuid(userUuid)
                    .sellerUnreadCount(0)
                    .viewerUnreadCount(0)
                    .createDateTime(LocalDateTime.now())
                    .build();
            chatRepository.save(chatEntity);
            
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", chatEntity.getUuid()), HttpStatus.OK);
        }
        
        // 일반 판매자와의 채팅방 생성
        if(chatRepository.existsBySellerUuidAndViewerUuid(uuid, userUuid)){
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "already exist"), HttpStatus.CONFLICT);
        }else if(!userEntityRepository.existsByRoleAndUuid(Role.SELLER, uuid)){
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "not exist seller"), HttpStatus.CONFLICT);
        }

        ChatEntity chatEntity = new ChatEntity().builder()
                .sellerUuid(uuid)
                .viewerUuid(userUuid)
                .sellerUnreadCount(0)
                .viewerUnreadCount(0)
                .createDateTime(LocalDateTime.now())
                .build();
        chatRepository.save(chatEntity);

        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", chatEntity.getUuid()), HttpStatus.OK);
    }

    @PutMapping("")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "not exist chat", content = @Content(mediaType = "")),
    })
    public ResponseEntity<ResponseDTO> sendChat(@Valid @RequestBody MessageDTO messageDTO) {
        Long userUuid = JwtHelper.getCurrentUserUuid();
        if(!chatRepository.existsByUuidAndViewerUuid(messageDTO.getUuid(), userUuid)){
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "not exist chat"), HttpStatus.CONFLICT);
        }

        ChatEntity chatEntity = chatRepository.findByUuid(messageDTO.getUuid());
        chatEntity.setSellerUnreadCount(chatEntity.getSellerUnreadCount()+1);
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
        
        // 하나의 쿼리로 채팅방 존재 여부 확인 및 조회
        Optional<ChatEntity> chatEntityOpt = chatRepository.findByUuidAndViewerUuid(uuid, userUuid);
        if(chatEntityOpt.isEmpty()){
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "not exist chat"), HttpStatus.CONFLICT);
        }

        ChatEntity chatEntity = chatEntityOpt.get();
        Long sellerUuid = chatEntity.getSellerUuid();
        
        // 판매자가 보낸 메시지들을 일괄적으로 읽음 처리
        messageRepository.updateIsReadTrueForSellerMessages(uuid, sellerUuid);
        
        // 시청자의 읽지 않은 메시지 개수를 직접 쿼리로 업데이트
        chatRepository.updateViewerUnreadCountToZero(uuid, userUuid);
        
        // 채팅 메시지 목록 조회
        List<MessageEntity> messages = messageRepository.findAllByChatUuidOrderByCreateDateTime(uuid);

        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", messages), HttpStatus.OK);
    }
}