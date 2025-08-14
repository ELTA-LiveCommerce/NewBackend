package kr.elta.backend.controller.viewer;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/viewer")
@Slf4j
@Tag(name = "Viewer", description = "구매자 관련 API")
public class Viewer {

//    @GetMapping("")
//    public ResponseEntity<ResponseDTO> getSellerInfo() {
//        Long userUuid = JwtHelper.getCurrentUserUuid();
//
//        return new ResponseEntity<>(
//            new ResponseDTO(true, "Seller info retrieved successfully", userUuid),
//            HttpStatus.OK
//        );
//    }
}