package kr.elta.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import kr.elta.backend.configuration.JwtUtil;
import kr.elta.backend.dto.ResponseDTO;
import kr.elta.backend.dto.UserSignInDTO;
import kr.elta.backend.dto.UserSignUpDTO;
import kr.elta.backend.entity.LoginType;
import kr.elta.backend.entity.Role;
import kr.elta.backend.entity.UserEntity;
import kr.elta.backend.repository.UserEntityRepository;
import kr.elta.backend.util.KakaoNotificationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@Slf4j
public class Auth {
    @Autowired
    private UserEntityRepository userEntityRepository;
    @Autowired
    private BCryptPasswordEncoder encoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private KakaoNotificationUtil kakaoNotificationUtil;

    @PostMapping("/signUp")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "already exist", content = @Content(mediaType = "")),
    })
    public ResponseEntity<ResponseDTO> signUp(@Valid @RequestBody UserSignUpDTO userSignUpDTO){
        // phoneNum이 null이 아닌 경우에만 중복 체크
        if(userSignUpDTO.getPhoneNum() != null){
            if(!userEntityRepository.findByIdOrPhoneNum(userSignUpDTO.getId(), userSignUpDTO.getPhoneNum()).isEmpty()){
                return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "already exist"), HttpStatus.CONFLICT);
            }
        } else {
            // phoneNum이 null인 경우 id만으로 중복 체크
            if(userEntityRepository.findById(userSignUpDTO.getId()).isPresent()){
                return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "already exist"), HttpStatus.CONFLICT);
            }
        }

        UserEntity userEntity = new UserEntity().builder()
                .id(userSignUpDTO.getId())
                .role(Role.VIEWER)
                .password(encoder.encode(userSignUpDTO.getPw()))
                .phoneNum(userSignUpDTO.getPhoneNum())
                .name(userSignUpDTO.getName())
                .accountType(userSignUpDTO.getAccountType())
                .accountNum(userSignUpDTO.getAccountNum())
                .loginType(LoginType.NORMAL)
                .createDateTime(LocalDateTime.now())
                .isReviewing(false)
                .balance(0)
                .build();
        userEntityRepository.save(userEntity);

        HashMap<String, String> token = new HashMap<>();
        token.put("accessToken", jwtUtil.generateAccessToken(userEntity.getUuid().toString(), "VIEWER"));
        token.put("refreshToken", jwtUtil.generateRefreshToken(userEntity.getUuid().toString(), "VIEWER"));

        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", token), HttpStatus.OK);
    }

    @PostMapping("/signIn")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "not exist", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "incorrect password", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "account deleted", content = @Content(mediaType = "")),
    })
    public ResponseEntity<ResponseDTO> signIn(@Valid @RequestBody UserSignInDTO userSignInDTO){
        Optional<UserEntity> userEntity = userEntityRepository.findById(userSignInDTO.getId());
        if(userEntity.isEmpty()){
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "not exist"), HttpStatus.CONFLICT);
        }else if(userEntity.get().getDeleteDateTime() != null){
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "account deleted"), HttpStatus.CONFLICT);
        }else if(!encoder.matches(userSignInDTO.getPw(), userEntity.get().getPassword())){
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "incorrect password"), HttpStatus.CONFLICT);
        }

        HashMap<String, String> token = new HashMap<>();
        token.put("accessToken", jwtUtil.generateAccessToken(userEntity.get().getUuid().toString(), userEntity.get().getRole().toString()));
        token.put("refreshToken", jwtUtil.generateRefreshToken(userEntity.get().getUuid().toString(), userEntity.get().getRole().toString()));

        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", token), HttpStatus.OK);
    }

    @PostMapping("/refresh")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "invalid or expired refresh token", content = @Content(mediaType = "")),
    })
    public ResponseEntity<ResponseDTO> refresh(@RequestBody Map<String, String> body){
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null
                || !jwtUtil.validateToken(refreshToken)
                || jwtUtil.isTokenExpired(refreshToken)) {
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "invalid or expired refresh token"), HttpStatus.CONFLICT);
        }

        String username = jwtUtil.getUsername(refreshToken);
        String role     = jwtUtil.getRole(refreshToken);
        HashMap<String, String> token = new HashMap<>();
        token.put("accessToken", jwtUtil.generateAccessToken(username, role));
        token.put("refreshToken", jwtUtil.generateRefreshToken(username, role));

        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", token), HttpStatus.OK);
    }

    @PostMapping("/findId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "parameter was not provided", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "not exist", content = @Content(mediaType = "")),
    })
    public ResponseEntity<ResponseDTO> findId(@RequestBody Map<String, String> body){
        String phoneNum = body.get("phoneNum");
        if (phoneNum == null) {
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "parameter was not provided"), HttpStatus.CONFLICT);
        }

        List<UserEntity> usersByPhone = userEntityRepository.findByPhoneNumAndDeleteDateTimeIsNull(phoneNum);
        if (usersByPhone.isEmpty()) {
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "not exist"), HttpStatus.CONFLICT);
        }
        
        UserEntity userEntity = usersByPhone.get(0);
        String visible = userEntity.getId().substring(0, 4);
        int stars = userEntity.getId().length() - 4;
        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success", visible + "*".repeat(stars)), HttpStatus.OK);
    }

    @PostMapping("/findPw")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "parameter was not provided", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "not exist", content = @Content(mediaType = "")),
            @ApiResponse(responseCode = "notification failed", content = @Content(mediaType = "")),
    })
    public ResponseEntity<ResponseDTO> findPw(@RequestBody Map<String, String> body){
        String phoneNum = body.get("phoneNum");
        String id = body.get("id");
        if (phoneNum == null || id == null) {
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "parameter was not provided"), HttpStatus.CONFLICT);
        }

        List<UserEntity> matchingUsers = userEntityRepository.findByPhoneNumAndIdAndDeleteDateTimeIsNull(phoneNum, id);
        if (matchingUsers.isEmpty()) {
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "not exist"), HttpStatus.CONFLICT);
        }

        UserEntity userEntity = matchingUsers.get(0);
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvwxyz"
                + "0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        String tempPassword = sb.toString();
        userEntity.setPassword(encoder.encode(tempPassword));
        userEntityRepository.save(userEntity);
        boolean notificationSent = kakaoNotificationUtil.sendPasswordResetNotification(
            tempPassword,
            userEntity.getPhoneNum(),
            userEntity.getName()
        );
        
        if (!notificationSent) {
            log.error("비밀번호 재설정 알림톡 전송 실패 - 사용자: {}, 전화번호: {}", 
                userEntity.getId(), userEntity.getPhoneNum());
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "notification failed"), HttpStatus.CONFLICT);
        }
        return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "success"), HttpStatus.OK);
    }
}