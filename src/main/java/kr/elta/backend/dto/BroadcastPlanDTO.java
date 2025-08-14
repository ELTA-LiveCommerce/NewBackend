package kr.elta.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BroadcastPlanDTO {
    @NotBlank(message = "name은 필수 입력 값입니다.")
    @Size(max = 100, message = "name은 100자 이하입니다")
    private String name;

    @NotNull(message = "minute는 필수 입력 값입니다.")
    private Integer minute;

    @NotNull(message = "maxViewer는 필수 입력 값입니다.")
    private Integer maxViewer;

    @NotNull(message = "price는 필수 입력 값입니다.")
    private Double price;

    @NotNull(message = "isActive는 필수 입력 값입니다.")
    private Boolean isActive;

    @NotNull(message = "displayOrder는 필수 입력 값입니다.")
    private Integer displayOrder;
}