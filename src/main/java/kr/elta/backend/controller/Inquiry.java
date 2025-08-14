package kr.elta.backend.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.elta.backend.dto.MessageDTO;
import kr.elta.backend.dto.ProductDTO;
import kr.elta.backend.dto.ResponseDTO;
import kr.elta.backend.entity.*;
import kr.elta.backend.repository.*;
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
@RequestMapping("/inquiry")
@Slf4j
@Tag(name = "/inquiry", description = "시스템 문의 관리 API")
public class Inquiry {
    @Autowired
    private UserEntityRepository userEntityRepository;
    @Autowired
    private InquiryRepository inquiryRepository;
    @Autowired
    private MessageRepository messageRepository;

    @PostMapping("")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "not exist", content = @Content(mediaType = ""))
    })
    public ResponseEntity<ResponseDTO> createInquiry(@RequestParam("title") String title) {
        Long userUuid = JwtHelper.getCurrentUserUuid();

        Optional<UserEntity> userEntity = userEntityRepository.findById(userUuid);
        if(userEntity.isEmpty()){
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "not exist"), HttpStatus.CONFLICT);
        }

        InquiryEntity inquiryEntity = InquiryEntity.builder()
                .userUuid(userUuid)
                .title(title)
                .isEnd(false)
                .userName(userEntity.get().getName())
                .userPhoneNum(userEntity.get().getPhoneNum())
                .unreadCount(0)
                .createDateTime(LocalDateTime.now())
                .build();
        inquiryRepository.save(inquiryEntity);
        
        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", inquiryEntity.getUuid()), HttpStatus.OK);
    }
    
    @GetMapping("")
    public ResponseEntity<ResponseDTO> getMyInquiryList() {
        Long userUuid = JwtHelper.getCurrentUserUuid();
        List<InquiryEntity> inquiryEntityList = inquiryRepository.findAllByUserUuidOrderByCreateDateTimeDesc(userUuid);
        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", inquiryEntityList), HttpStatus.OK);
    }
    
    @GetMapping("/{uuid}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "not exist inquiry", content = @Content(mediaType = ""))
    })
    @Transactional
    public ResponseEntity<ResponseDTO> getInquiryMessages(@PathVariable("uuid") Long uuid) {
        Long userUuid = JwtHelper.getCurrentUserUuid();
        
        Optional<InquiryEntity> inquiryEntityOpt = inquiryRepository.findByUuidAndUserUuid(uuid, userUuid);
        if(inquiryEntityOpt.isEmpty()){
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "not exist inquiry"), HttpStatus.CONFLICT);
        }
        
        messageRepository.updateIsReadTrueForInquiryMessages(uuid, userUuid);
        inquiryRepository.updateUnreadCountToZero(uuid, userUuid);
        
        List<MessageEntity> messages = messageRepository.findAllByChatUuidOrderByCreateDateTime(uuid);
        
        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", messages), HttpStatus.OK);
    }
    
    @PutMapping("")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "not exist inquiry", content = @Content(mediaType = ""))
    })
    public ResponseEntity<ResponseDTO> sendInquiryMessage(@RequestBody MessageDTO messageDTO) {
        Long userUuid = JwtHelper.getCurrentUserUuid();
        
        Optional<InquiryEntity> inquiryEntityOpt = inquiryRepository.findByUuidAndUserUuid(messageDTO.getUuid(), userUuid);
        if(inquiryEntityOpt.isEmpty()){
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "not exist inquiry"), HttpStatus.CONFLICT);
        }
        
        inquiryRepository.incrementUnreadCount(messageDTO.getUuid());
        
        MessageEntity messageEntity = MessageEntity.builder()
                .chatUuid(messageDTO.getUuid())
                .senderUuid(userUuid)
                .message(messageDTO.getMessage())
                .createDateTime(LocalDateTime.now())
                .isRead(false)
                .build();
        messageRepository.save(messageEntity);
        
        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", messageEntity.getUuid()), HttpStatus.OK);
    }

}
