package kr.elta.backend.dto;

import jakarta.validation.constraints.NotBlank;
import kr.elta.backend.entity.BankType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSignInDTO {
    @NotBlank(message = "id는 필수 입력 값입니다.")
    private String id;

    @NotBlank(message = "pw는 필수 입력 값입니다.")
    private String pw;
}
