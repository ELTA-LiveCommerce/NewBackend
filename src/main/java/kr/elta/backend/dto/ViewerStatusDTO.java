package kr.elta.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.elta.backend.entity.ViewerStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViewerStatusDTO {
    @NotNull(message = "status는 필수 입력 값입니다.")
    private ViewerStatus status;

    @NotNull(message = "viewerUuid는 필수 입력 값입니다.")
    private Long viewerUuid;
}
