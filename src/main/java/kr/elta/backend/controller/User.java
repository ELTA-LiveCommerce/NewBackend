package kr.elta.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import kr.elta.backend.configuration.JwtUtil;
import kr.elta.backend.dto.ResponseDTO;
import kr.elta.backend.dto.UserUpdateDTO;
import kr.elta.backend.entity.UserEntity;
import kr.elta.backend.repository.*;
import kr.elta.backend.util.JwtHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user")
@Slf4j
public class User {
    @Autowired
    private UserEntityRepository userEntityRepository;
    @Autowired
    private BCryptPasswordEncoder encoder;

    @GetMapping("")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "내 정보 조회 성공"),
            @ApiResponse(responseCode = "not exist", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "account deleted", content = @Content(mediaType = ""))
    })
    public ResponseEntity<ResponseDTO> getMyInfo(){
        Long userUuid = JwtHelper.getCurrentUserUuid();
        
        Optional<UserEntity> userEntityOpt = userEntityRepository.findById(userUuid);
        if(userEntityOpt.isEmpty()){
            return new ResponseEntity<>(new ResponseDTO(false, "not exist"), HttpStatus.CONFLICT);
        }

        UserEntity userEntity = userEntityOpt.get();

        // 탈퇴한 사용자인지 확인
        if(userEntity.getDeleteDateTime() != null){
            return new ResponseEntity<>(new ResponseDTO(false, "account deleted"), HttpStatus.CONFLICT);
        }
        
        // 비밀번호를 제외한 사용자 정보 반환
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("uuid", userEntity.getUuid());
        userInfo.put("id", userEntity.getId());
        userInfo.put("name", userEntity.getName());
        userInfo.put("phoneNum", userEntity.getPhoneNum());
        userInfo.put("profileImg", userEntity.getProfileImg());
        userInfo.put("bannerImg", userEntity.getBannerImg());
        userInfo.put("accountType", userEntity.getAccountType());
        userInfo.put("accountNum", userEntity.getAccountNum());
        userInfo.put("address", userEntity.getAddress());
        userInfo.put("description", userEntity.getDescription());
        userInfo.put("role", userEntity.getRole());
        userInfo.put("balance", userEntity.getBalance());
        userInfo.put("createDateTime", userEntity.getCreateDateTime());
        userInfo.put("isReviewing", userEntity.getIsReviewing());
        userInfo.put("businessName", userEntity.getBusinessName());
        userInfo.put("businessAddress", userEntity.getBusinessAddress());
        userInfo.put("businessNumber", userEntity.getBusinessNumber());
        userInfo.put("businessTime", userEntity.getBusinessTime());
        
        return new ResponseEntity<>(new ResponseDTO(true, "success", userInfo), HttpStatus.OK);
    }

    @DeleteMapping("")
    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자를 탈퇴 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "not exist", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "already deleted", content = @Content(mediaType = "")),
    })
    public ResponseEntity<ResponseDTO> delete(){
        Long userUuid = JwtHelper.getCurrentUserUuid();
        
        Optional<UserEntity> userEntityOpt = userEntityRepository.findById(userUuid);
        if(userEntityOpt.isEmpty()){
            return new ResponseEntity<>(new ResponseDTO(false, "not exist"), HttpStatus.CONFLICT);
        }

        UserEntity userEntity = userEntityOpt.get();

        // 이미 탈퇴한 사용자인지 확인
        if(userEntity.getDeleteDateTime() != null){
            return new ResponseEntity<>(new ResponseDTO(false, "already deleted"), HttpStatus.CONFLICT);
        }
        
        // Soft Delete: deleteDateTime 설정으로 탈퇴 처리
        userEntity.setDeleteDateTime(LocalDateTime.now());
        userEntityRepository.save(userEntity);
        
        return new ResponseEntity<>(new ResponseDTO(true, "success"), HttpStatus.OK);
    }

    @PatchMapping("")
    @Operation(summary = "내 정보 수정", description = "현재 로그인한 사용자의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "not exist", content = @Content(mediaType = "")),
    })
    public ResponseEntity<ResponseDTO> update(@Valid @RequestBody UserUpdateDTO userUpdateDTO){
        Long userUuid = JwtHelper.getCurrentUserUuid();
        Optional<UserEntity> userEntity = userEntityRepository.findById(userUuid);
        if(userEntity.isEmpty()){
            return new ResponseEntity<>(new ResponseDTO(false, "not exist"), HttpStatus.CONFLICT);
        }

        userEntity.ifPresent(entity -> {
            if(userUpdateDTO.getOldPassword() != null && userUpdateDTO.getNewPassword() != null 
               && encoder.matches(userUpdateDTO.getOldPassword(), userEntity.get().getPassword())){
                entity.setPassword(encoder.encode(userUpdateDTO.getNewPassword()));
            }
            if(userUpdateDTO.getPhoneNum() != null) entity.setPhoneNum(userUpdateDTO.getPhoneNum());
            if(userUpdateDTO.getName() != null) entity.setName(userUpdateDTO.getName());
            if(userUpdateDTO.getProfileImg() != null) entity.setProfileImg(userUpdateDTO.getProfileImg());
            if(userUpdateDTO.getBannerImg() != null) entity.setBannerImg(userUpdateDTO.getBannerImg());
            if(userUpdateDTO.getAccountNum() != null) entity.setAccountNum(userUpdateDTO.getAccountNum());
            if(userUpdateDTO.getAccountType() != null) entity.setAccountType(userUpdateDTO.getAccountType());
            if(userUpdateDTO.getAddress() != null) entity.setAddress(userUpdateDTO.getAddress());
            if(userUpdateDTO.getDescription() != null) entity.setDescription(userUpdateDTO.getDescription());
        });
        userEntityRepository.save(userEntity.get());

        return new ResponseEntity<>(new ResponseDTO(true, "success"), HttpStatus.OK);
    }
}