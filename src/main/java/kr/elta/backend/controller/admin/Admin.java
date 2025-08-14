package kr.elta.backend.controller.admin;

import io.swagger.v3.oas.annotations.tags.Tag;
import kr.elta.backend.dto.ResponseDTO;
import kr.elta.backend.util.JwtHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@Slf4j
@Tag(name = "Admin", description = "어드민 관련 API")
public class Admin {

//    @GetMapping("")
//    public ResponseEntity<ResponseDTO> test() {
//        Long userUuid = JwtHelper.getCurrentUserUuid();
//
//        return new ResponseEntity<>(new ResponseDTO(true, "Seller info retrieved successfully", userUuid), HttpStatus.OK);
//    }
}