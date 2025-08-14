package kr.elta.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BroadcastDiscountDTO {
    @NotNull(message = "방송 UUID는 필수 입력 값입니다.")
    private Long broadcastUuid;
    
    @NotNull(message = "상품 UUID는 필수 입력 값입니다.")
    private Long productUuid;
    
    @NotNull(message = "할인 가격은 필수 입력 값입니다.")
    @PositiveOrZero(message = "할인 가격은 0 이상이어야 합니다.")
    private Integer discountPrice;
}