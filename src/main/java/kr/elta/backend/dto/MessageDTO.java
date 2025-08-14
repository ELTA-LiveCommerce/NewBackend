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
public class MessageDTO {
    @NotNull(message = "uuid는 필수 입력 값입니다.")
    private Long uuid;

    @NotBlank(message = "message는 필수 입력 값입니다.")
    @Size(min = 1, max = 1000)
    private String message;
}
