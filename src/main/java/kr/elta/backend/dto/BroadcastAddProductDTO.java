package kr.elta.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BroadcastAddProductDTO {
    @NotNull(message = "방송 UUID는 필수 입력 값입니다.")
    private Long broadcastUuid;
    
    @NotNull(message = "상품 UUID는 필수 입력 값입니다.")
    private Long productUuid;
}