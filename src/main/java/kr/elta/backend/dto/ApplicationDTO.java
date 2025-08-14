package kr.elta.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationDTO {
    @NotNull
    private Long uuid;

    @NotNull(message = "name는 필수 입력 값입니다.")
    private Boolean result;

    @NotBlank(message = "businessName는 필수 입력 값입니다.")
    @Size(max = 40, message = "businessName는 40자 이하입니다")
    private String businessName;

    @NotBlank(message = "businessAddress는 필수 입력 값입니다.")
    @Size(min = 1, max = 100, message = "businessAddress는 1~100자입니다")
    private String businessAddress;

    @NotBlank(message = "businessNumber는 필수 입력 값입니다.")
    @Size(max = 10, message = "businessNumber는 10자 이하입니다")
    private String businessNumber;

    @NotBlank(message = "businessTime는 필수 입력 값입니다.")
    @Size(max = 13, message = "businessTime는 13자 이하입니다")
    private String businessTime;
}
