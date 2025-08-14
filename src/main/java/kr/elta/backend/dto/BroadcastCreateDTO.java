package kr.elta.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BroadcastCreateDTO {
    private Long uuid;
    
    @NotBlank(message = "방송 제목은 필수 입력 값입니다.")
    @Size(max = 255, message = "방송 제목은 255자 이하입니다.")
    private String title;
    
    @NotBlank(message = "썸네일 URL은 필수 입력 값입니다.")
    @Size(max = 255, message = "썸네일 URL은 255자 이하입니다.")
    private String thumbnailUrl;
    
    @NotNull(message = "방송 예정 시간은 필수 입력 값입니다.")
    @Future(message = "방송 예정 시간은 현재 시간보다 미래여야 합니다.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledDatetime;
    
    @Size(max = 1000, message = "방송 설명은 1000자 이하입니다.")
    private String description;

    @NotNull(message = "상품은 필수 입력 값입니다.")
    private List<Long> productUuidList;
    
    @NotNull(message = "배송비는 필수 입력 값입니다.")
    @PositiveOrZero(message = "배송비는 0 이상이어야 합니다.")
    private int shippingFee;
}