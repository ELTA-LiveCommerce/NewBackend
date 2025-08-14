package kr.elta.backend.controller.admin;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.elta.backend.dto.MessageDTO;
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
@RequestMapping("/admin/inquiry")
@Slf4j
@Tag(name = "/admin/inquiry", description = "어드민 문의 관리 API")
public class AdminInquiry {
    @Autowired
    private UserEntityRepository userEntityRepository;
    @Autowired
    private InquiryRepository inquiryRepository;
    @Autowired
    private MessageRepository messageRepository;
    
    @GetMapping("")
    public ResponseEntity<ResponseDTO> getAllInquiryList() {
        List<InquiryEntity> inquiryEntityList = inquiryRepository.findAllByOrderByCreateDateTimeDesc();
        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", inquiryEntityList), HttpStatus.OK);
    }
    
    @GetMapping("/{uuid}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "not exist inquiry", content = @Content(mediaType = ""))
    })
    @Transactional
    public ResponseEntity<ResponseDTO> getInquiryMessages(@PathVariable("uuid") Long uuid) {
        Optional<InquiryEntity> inquiryEntityOpt = inquiryRepository.findById(uuid);
        if(inquiryEntityOpt.isEmpty()){
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "not exist inquiry"), HttpStatus.CONFLICT);
        }
        
        List<MessageEntity> messages = messageRepository.findAllByChatUuidOrderByCreateDateTime(uuid);
        
        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", messages), HttpStatus.OK);
    }
    
    @PutMapping("")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "not exist inquiry", content = @Content(mediaType = ""))
    })
    public ResponseEntity<ResponseDTO> sendInquiryReply(@RequestBody MessageDTO messageDTO) {
        Long adminUuid = JwtHelper.getCurrentUserUuid();
        
        Optional<InquiryEntity> inquiryEntityOpt = inquiryRepository.findById(messageDTO.getUuid());
        if(inquiryEntityOpt.isEmpty()){
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "not exist inquiry"), HttpStatus.CONFLICT);
        }
        
        inquiryRepository.updateUnreadCountToZero(messageDTO.getUuid(), inquiryEntityOpt.get().getUserUuid());
        
        MessageEntity messageEntity = MessageEntity.builder()
                .chatUuid(messageDTO.getUuid())
                .senderUuid(adminUuid)
                .message(messageDTO.getMessage())
                .createDateTime(LocalDateTime.now())
                .isRead(false)
                .build();
        messageRepository.save(messageEntity);
        
        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", messageEntity.getUuid()), HttpStatus.OK);
    }
    
    @DeleteMapping("/{uuid}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "not exist inquiry", content = @Content(mediaType = ""))
    })
    public ResponseEntity<ResponseDTO> endInquiry(@PathVariable("uuid") Long uuid) {
        Optional<InquiryEntity> inquiryEntityOpt = inquiryRepository.findById(uuid);
        if(inquiryEntityOpt.isEmpty()){
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "not exist inquiry"), HttpStatus.CONFLICT);
        }
        
        inquiryRepository.updateIsEndToTrue(uuid);
        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success"), HttpStatus.OK);
    }

}
