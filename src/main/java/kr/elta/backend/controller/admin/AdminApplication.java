package kr.elta.backend.controller.admin;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import kr.elta.backend.dto.ApplicationDTO;
import kr.elta.backend.dto.ResponseDTO;
import kr.elta.backend.entity.Role;
import kr.elta.backend.entity.UserEntity;
import kr.elta.backend.repository.UserEntityRepository;
import kr.elta.backend.util.JwtHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin/application")
@Slf4j
@Tag(name = "/admin/application", description = "관리자 관리 API")
public class AdminApplication {
    @Autowired
    private UserEntityRepository userEntityRepository;

    @GetMapping("")
    public ResponseEntity<ResponseDTO> getReviewing() {
        List<UserEntity> userEntityList = userEntityRepository.findAllByIsReviewing(true);

        return new ResponseEntity<>(new ResponseDTO(true, "success", userEntityList), HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<ResponseDTO> setReviewing(@Valid @RequestBody ApplicationDTO applicationDTO) {
        Optional<UserEntity> userEntityOpt = userEntityRepository.findById(applicationDTO.getUuid());
        if(userEntityOpt.isEmpty()){
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "not exist account"), HttpStatus.CONFLICT);
        }

        UserEntity userEntity = userEntityOpt.get();
        userEntity.setIsReviewing(false);
        userEntity.setRole(applicationDTO.getResult() ? Role.SELLER : Role.VIEWER);
        
        if(applicationDTO.getResult()) {
            userEntity.setBusinessName(applicationDTO.getBusinessName());
            userEntity.setBusinessAddress(applicationDTO.getBusinessAddress());
            userEntity.setBusinessNumber(applicationDTO.getBusinessNumber());
            userEntity.setBusinessTime(applicationDTO.getBusinessTime());
        }
        
        userEntityRepository.save(userEntity);

        return new ResponseEntity<>(new ResponseDTO(true, "success"), HttpStatus.OK);
    }
}