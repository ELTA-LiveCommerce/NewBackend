package kr.elta.backend.dto;

import jakarta.validation.constraints.NotBlank;
import kr.elta.backend.entity.BankType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSignUpDTO {
    @NotBlank(message = "id는 필수 입력 값입니다.")
    @Length(min = 4, max = 40, message = "4~40자 사이로 입력해주세요.")
    private String id;

    @NotBlank(message = "pw는 필수 입력 값입니다.")
    @Length(min = 4, message = "4자 이상 입력해주세요.")
    private String pw;

    @NotBlank(message = "name는 필수 입력 값입니다.")
    @Length(min = 2, max = 40, message = "2~40자 사이로 입력해주세요.")
    private String name;

    private BankType accountType;

    @Length(max = 20, message = "20자 이내로 입력해주세요.")
    private String accountNum;

    @Length(max = 11, message = "11자 이내로 입력해주세요.")
    private String phoneNum;
}
